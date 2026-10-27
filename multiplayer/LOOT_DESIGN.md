# M2 design: server-owned persistent monster loot

Status: **design proposal, not implemented.** Drafted 2026-10-27, Asia/Jerusalem. The ownership
window, the lifetime and the claim policy are **PROPOSED, pending owner approval** (see
[SCOPE_PROPOSAL.md](SCOPE_PROPOSAL.md) section 4). The implementation can be built with these values as
named constants, so the owner can change them without redesigning anything.

This is the general monster loot path that [MILESTONES.md](MILESTONES.md) M2 requires. It is kept
separate from owner corpse recovery (`online_corpses`, schema v8). It reuses that path's patterns
but not its table.

## 1. Offline behavior this preserves

- `DataMonster` builds one `LootTable` per definition, with `gold(goldMin,goldMax)` plus one
  `item(name,chance)` entry for each `MonsterDef.LootDrop`. On a player kill, `MainGameScreen`'s
  loot callback runs `lootTable.roll(random)` once. **Gold is credited directly to the killer.**
  Each rolled item becomes a `GroundItem` scattered within ±12 px of the monster's position.
- Loot names such as `"Leather gloves"` resolve with `ItemDefinition.normalizeKey` to
  `item.leather_gloves`. Every Skeleton entry resolves in `items-v1.json`.
- Pickup (`GroundItemClickHandler` and `GroundItemManager.pickUpAt`) requires a Chebyshev tile
  distance of at most `OBJECT_MAX_INTERACTION_DISTANCE` (8) and no line-of-sight check. Then
  `InventoryService.add(player,key,charges)` runs. The add is refused with `TOO_HEAVY` when
  `currentWeight + weight > strength*2000/(550+strength)`, using effective strength. A refused
  item stays on the ground. A newly dropped item gets full `nbCharges` unless it is `unlimitedUse`.
  `add` has no unique-item check today, even though `PickupStatus.UNIQUE_ITEM` exists.
- Offline ground items have no owner and no expiry. They live in memory for the session.

Online today: `OnlineMelee` rolls **gold only** (a gold-only `LootTable`) inside the lethal strike.
It **rejects every definition with non-empty `loot`**, so it never drops items silently.

## 2. Entity lifecycle

```
            lethal strike commit                    pickup commit
  (draft) ───────────────────────► ON_GROUND ─────────────────────► CLAIMED (terminal)
                                       │
                                       │ expiry sweep (DB time >= expires_at)
                                       └──────────────────────────► EXPIRED (terminal)
```

1. **Draft (simulation thread, inside `OnlineMelee`).** When the server resolves a lethal blow, it
   rolls `MonsterLootTables.of(definition).roll(random)` once. That single roll supplies the gold
   that is already credited and the items. It is the same table and the same order of random draws
   as offline. Each rolled name becomes a `LootDropDraft(id=UUID.randomUUID(), index, key, quantity=1,
   charges, world, x, y)`. The item ID is generated here and **becomes the inventory `Item.id`
   unchanged on pickup**. The position is the creature's authoritative absolute tile at death. Do not
   apply scatter, because pickup is by ID. Content that is unknown, unresolved, uses `oneOf` choices
   or carries `sourceEvents` fails closed with `unsupported_combat`, as it does now.
2. **ON_GROUND.** The drafts are inserted in the **same transaction** as the lethal
   `commitCreatureStrike`, after the `online_world_rewards` claim insert. If the strike loses the
   claim, it rolls back and no loot exists. The drop is part of the immutable payload: retrying the
   same life event reuses the same drafts, and the receipt fingerprint covers them. The database
   sets `owner_until = clock_timestamp() + OWNER_WINDOW` (proposed 60 s) and
   `expires_at = clock_timestamp() + LIFETIME` (proposed 15 min). Those timestamps are excluded from
   the fingerprint, so an identical retry cannot change them.
3. **Publication.** The region loot cache is updated on the simulation thread only after commit. An
   uncertain strike outcome disconnects the actor, as now. It also schedules a reload of that
   region's loot from the database, like the existing creature-cache reload. Loot is never
   published optimistically.
4. **CLAIMED.** See section 4. The row stays as an audit record, and its ID now lives in exactly one
   inventory.
5. **EXPIRED.** A single bounded sweep worker runs `UPDATE ... SET state='EXPIRED' WHERE state='ON_GROUND'
   AND expires_at<=clock_timestamp() ... LIMIT 256 RETURNING id` and publishes removals on the
   simulation thread. Expired rows are never brought back. Purging old `CLAIMED` and `EXPIRED` rows
   is a later, separate retention task. Until that exists, keep them, which follows the
   `online_world_rewards` retention rule.

## 3. Ownership and claim windows

| Phase | Who may claim |
| --- | --- |
| `now < owner_until` | Only `owner_character`, which is the lethal-strike claimant. |
| `owner_until <= now < expires_at` | Any character whose active binding is in the region that owns `world/x/y`. |
| `now >= expires_at` | Nobody. The claim is refused even if the sweep has not run yet. |

The database clock decides. The simulation pre-check uses its own clock only to reject requests
early. A pre-check that disagrees with the database at the boundary costs one refusal, never a
duplicate.

## 4. Atomic claim receipt

Command: the server generates `PickupDraft(eventId, lootId, expectedRevision, trusted world/x/y)` on
the simulation thread after these checks. All of them run on the simulation owner, in the same order
as the corpse and NPC checks.

1. Authenticated session, rate budget, `admit(token, epoch)`. A stale epoch is rejected before the
   loot ID is resolved.
2. No pending gameplay event (`binding.event == null`) and not `awaitingRegionReady`. The character
   is alive. `revision` equals the committed revision.
3. The loot ID is in **this region's** cache and is `ON_GROUND`. The owner-window and expiry
   pre-check passes.
4. Chebyshev tile distance from the trusted server position is at most 8, matching offline.
5. Capacity: `CarryCapacityRules.canAdd(committedProgress, itemContent, effectiveStrength, key)`.
   Effective strength comes from `OnlineEquipmentBonuses` on committed equipment. Active buffs
   already fail closed. Unique items: offline `add` has no unique check, so online matches it, but
   **a unique item in a loot table fails closed** until the owner decides. No in-scope table
   contains one.
6. One pickup is in flight per character. Freeze nothing: movement continues, because the
   transaction checks position again against the frozen trusted sample.

Transaction `OnlineStore.claimGroundLoot(Session, revision, eventId, lootId, world, x, y)`:

1. `lockSession` (character `FOR UPDATE`, lease/session fence).
2. Receipt lookup: `SELECT ... FROM online_loot_claims WHERE event_id=?`. If a receipt exists for
   the same character and loot with the same hash, commit and return `RewardResult(false,
   current)`. Any mismatch raises a `Conflict`. As in `applyReward`, this lookup comes before the
   stale-revision check.
3. `state.revision == revision`, otherwise `Conflict("Stale pickup")`.
4. `SELECT ... FROM online_ground_loot WHERE id=? FOR UPDATE`. The lock order is always character,
   then loot, which matches "character lock precedes spawn lock", so a deadlock cannot occur. Require
   `state='ON_GROUND'`, the same world, distance at most 8 tiles, `clock_timestamp()<expires_at`,
   and either `owner_character=character` or `clock_timestamp()>=owner_until`. Any failure raises
   `Conflict("Loot unavailable")`, `("Loot reserved")` or `("Loot out of reach")`.
5. Append `Item(lootId, key, quantity, charges)` to inventory. Reject this step if the ID already
   exists in inventory, equipment or storage, as a defensive check.
6. `UPDATE online_ground_loot SET state='CLAIMED', claimed_by=?, claim_event=?`.
7. `INSERT online_loot_claims(event_id, character_id, loot_id, payload_hash)`. `loot_id` is
   `UNIQUE`, which is the database-level guarantee against a double claim.
8. `UPDATE online_characters SET progress=?, revision=revision+1`. Commit and reload the state.

Two simultaneous claimants: B blocks on the loot row lock until A commits, then reads `CLAIMED`
and rolls back completely. B's inventory and revision are unchanged, and B gets `409
loot_unavailable`. A capacity refusal happens before any write, so the loot and the inventory are
both untouched.

Quantity conservation: this design creates only single-unit drops (`quantity=1`), because offline
loot entries are per-unit. If a stackable drop is added later, the whole stack is claimed in one
step. Partial stack pickup is out of scope and must be designed separately before it is enabled.

## 5. Schema v10 migration outline

Add a `version < 10` block in the existing serialized installer, and raise the supported ceiling
from 9 to 10:

```sql
CREATE TABLE online_ground_loot (
  id uuid PRIMARY KEY,
  source_event uuid NOT NULL REFERENCES online_world_rewards(event_id),
  drop_index smallint NOT NULL CHECK (drop_index >= 0),
  world integer NOT NULL CHECK (world >= 0),
  x double precision NOT NULL, y double precision NOT NULL,
  item_key varchar(128) NOT NULL CHECK (item_key ~ '^item\.[a-z0-9_]+$'),
  quantity integer NOT NULL CHECK (quantity > 0),
  charges integer NOT NULL CHECK (charges >= 0),
  owner_character uuid NOT NULL REFERENCES online_characters(id),
  owner_until timestamptz NOT NULL, expires_at timestamptz NOT NULL CHECK (expires_at >= owner_until),
  state varchar(10) NOT NULL DEFAULT 'ON_GROUND' CHECK (state IN ('ON_GROUND','CLAIMED','EXPIRED')),
  claimed_by uuid REFERENCES online_characters(id),
  claim_event uuid UNIQUE,
  created_at timestamptz NOT NULL DEFAULT clock_timestamp(),
  UNIQUE (source_event, drop_index),
  CHECK ((state = 'CLAIMED') = (claimed_by IS NOT NULL AND claim_event IS NOT NULL)));
CREATE INDEX online_ground_loot_live ON online_ground_loot(world, expires_at) WHERE state = 'ON_GROUND';
CREATE TABLE online_loot_claims (
  event_id uuid PRIMARY KEY,
  character_id uuid NOT NULL REFERENCES online_characters(id),
  loot_id uuid NOT NULL UNIQUE REFERENCES online_ground_loot(id),
  payload_hash varchar(64) NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now());
INSERT INTO online_schema(version) VALUES (10);
```

- The migration only adds tables. No existing row changes, and v9 data loads unchanged.
- Rollback needs schema-v10-compatible code or a separately restored v9 backup (BACKUP.md). Never
  delete receipts. A v9 server already rejects a v10 database through the existing version check.
- The strike fingerprint is extended **only when drops are non-empty**, with a new
  `"\nloot-drops-v1\n"` segment. Receipts from gold-only kills keep their existing hashes, so
  pre-upgrade receipts still replay.
- `verify_backup.py` discovers tables dynamically. Add a fixture row so the restore drill covers the
  new tables.

## 6. Protocol additions (protocol 5)

Raise `OnlineProtocol.VERSION` to 5. Native clients and the server ship together, and the browser
diagnostic client's version must match.

- `/state` gains `loot: [{id, x, y, key, ownerPlayerId|null, ownerRemainingMs}]` with region-local
  x/y. The list is capped at the 128 entries nearest the viewer within the viewer's region. It
  includes only the public player ID, never a character or account ID. Charges are omitted because
  the ground row is public and the private inventory shows charges after pickup.
- `POST /pickup` with body `REVISION EPOCH LOOT_UUID`, a cap of 96 bytes and the same parser rules
  as `/recover`. Responses: `200` with `{revision}` after commit. `409` with one of
  `revision`, `busy`, `dead`, `loot_unavailable`, `loot_reserved`, `out_of_range`, `too_heavy`,
  `unsupported_item`. `429` for the rate limit. `503 busy` when the queue is full.
- `OnlineCharacterView` needs no change. The new inventory instance arrives through the normal
  revision-advanced private refresh.

## 7. Reconnect, restart and lost acknowledgement

- **Lost `/pickup` response.** Native commands never auto-replay, matching the existing rule. An
  ambiguous storage outcome disconnects the actor (the existing `PersistentSessions` path). On
  rejoin, private state either contains the item instance with `id == lootId`, in which case the
  ground list no longer shows it, or it does not, in which case the loot is still `ON_GROUND` or has
  expired. The player retries manually with a new server event. The DB receipt and `UNIQUE(loot_id)`
  make a worker-level retry of the *same* draft idempotent, and make a second claim impossible.
- **Lost lethal-strike acknowledgement.** The existing rule applies: hide the life, then reload. The
  loot cache reload for that region runs at the same time, so committed drops appear and
  uncommitted drops never do.
- **Server restart.** Before sessions are admitted, after creature recovery, each region loads
  `ON_GROUND AND expires_at>clock_timestamp()` rows inside its rectangle. Owner windows and expiry
  continue on database time, so downtime counts against them. The loot is not given extra time.
- **Transfer or region change.** The epoch fence rejects a pickup aimed at the old region. Loot never
  moves between regions. Logout drains a pending pickup before the final checkpoint, like the other
  gameplay events.
- **Audit query (M2 done evidence).**
  `SELECT id FROM online_ground_loot g WHERE state='ON_GROUND' AND EXISTS (inventory containing g.id)`
  must return zero rows, and every `CLAIMED` row must appear in exactly one character's
  inventory, equipment or storage, or be traceable through a later event.

## 8. Reused patterns and known adjacent gap

Reused: `LootTable` (already in the domain build); `online_world_rewards`, the single-recipient
claim; `applyReward`'s receipt-before-revision ordering and payload fingerprinting;
`PersistentSessions`' single-flight gameplay queue, bounded `lookups` read pool and
simulation-thread completion; the corpse path's trusted-position recheck inside the transaction,
owner-only reads and reference-only intents.

Adjacent gap found while designing this, and outside this design's scope:
`OnlineStore.recoverCorpse` appends corpse items **without a carry-weight check**. Offline corpse
items are ground items picked up through the weight-checked `pickUpAt`. M2 says recovery must follow
shared rules. Task L11 covers it, but the owner should decide first whether corpse recovery may
exceed carry weight, because refusing could strand a player's own gear.

## 9. Implementation tasks (ordered; each independently shippable)

All are **cheap-tier coding** tasks (AGENTS.md section 6). Route RCA on any failure to the mid tier.
"Collides" means the task edits a file that has **uncommitted changes in the main checkout by
another agent**: HostedWorlds, PersistentSessions, OnlineStore, SimulationLoop, and possibly
PreviewServer/ServerMain. Those tasks must wait until that work merges. Run
`git status` on main to confirm the exact list first. Claim each task ID in TASKS.md when it starts.

| # | Task | Layer | Files owned | Tests | Collides? |
| --- | --- | --- | --- | --- | --- |
| L1 | Extract `MonsterLootTables.of(MonsterDef)` (gold plus item entries, the same builder calls as `DataMonster`). `DataMonster` delegates to it. Add it to the domain includes. | domain (shared desktop source) | new `src/main/java/com/perso/T4C/monster/loot/MonsterLootTables.java`; `monster/core/DataMonster.java`; `multiplayer/domain/pom.xml` | New `MonsterLootTablesTest`: equal entries/gold for every registry definition; seeded roll equals the `DataMonster` table roll. The existing monster tests pass. | No |
| L2 | Extract pure `CarryCapacityRules` (`maximumWeight(effectiveStrength)`, `weightOf(keys, weightLookup)`, `canAdd`) and pure `ItemKeys.normalize` from `InventoryService`/`ItemDefinition`. The offline classes delegate to them. | domain | new `src/main/java/com/perso/T4C/item/CarryCapacityRules.java`, `ItemKeys.java`; `item/InventoryService.java`; `item/ItemDefinition.java`; `multiplayer/domain/pom.xml` (serialize with L1) | New rules tests, including the boundary `current+w == max` allowed and `+1` refused; existing `InventoryService`/`ItemBalanceGuidelinesTest` pass. | No |
| L3 | Add records `GroundLootView` and `LootDropDraft`, with validation (normalized key, quantity > 0, charges ≥ 0). | domain model | new `src/main/java/com/perso/T4C/online/model/GroundLootView.java`, `LootDropDraft.java` | Record validation tests. | No |
| L4 | `OnlineLootDrafts.roll(definition, items, random, world, x, y)`: one roll that returns gold plus drafts. Resolve keys through `ItemContent`, compute charges like `InventoryService.add`, and fail closed for unknown, unique or `oneOf` entries. | server (pure, new file) | new `multiplayer/server/.../OnlineLootDrafts.java` + test | Seeded parity with offline gold and items; Skeleton keys resolve; unknown item rejected; charges for potions = 1. | No |
| L5 | `OnlinePickupRules`: Chebyshev range ≤ 8, the owner/expiry window check, capacity from committed progress plus `OnlineEquipmentBonuses` effective strength. | server (pure, new file) | new `multiplayer/server/.../OnlinePickupRules.java` + test | Range edges 8/9, window edges, `too_heavy` with an over-weight inventory, equipped weight counted once. | No |
| L6 | Schema v10 plus store APIs: insert drops inside `applyReward` for a lethal strike with drops; `claimGroundLoot`; `listGroundLoot(world, rect)`; `expireGroundLoot(limit)`; the fingerprint segment. | server persistence | `OnlineStore.java`, `OnlineStoreTest.java`, `OnlineStoreCrashTest.java` | PostgreSQL: v9→v10 migration with existing rows; drop commit/rollback with the claim; concurrent two-character claim (exactly one wins); receipt replay; stale revision; owner window; expiry; out of reach; old gold-only receipts still replay. | **Yes, wait** |
| L7 | Region loot cache: load before admission, publish after commit, reload after an uncertain outcome, single-flight `pickup` command on the existing queue, expiry sweep worker, logout drain. | server runtime | `PersistentSessions.java`, `HostedWorlds.java`, `SimulationLoop.java` (sweep cadence) | `PersistentSessionsTest`: pickup publish-after-commit, ambiguous outcome disconnect without replay, restart reload, epoch fence, logout drain. | **Yes, wait** |
| L8 | `OnlineMelee`: remove the `loot` rejection only for tables that L4 accepts; use the L4 single roll for gold and drops; pass drafts into the strike. | server | `OnlineMelee.java`, `OnlineMeleeTest.java` | Skeleton becomes attackable; a lethal kill creates its drops; gold distribution unchanged; a `sourceEvents` definition is still rejected. | No file collision, but depends on L6/L7 |
| L9 | Protocol 5: `/pickup` route and parser, `loot` in `/state` (bounded and nearest first), the browser diagnostic version. | server transport | `PreviewServer.java`, `OnlineProtocol.java`, server `client.js`, `PreviewServerTest.java` | Body cap/parse, 426 on old protocol, a region-isolated loot list, 409 codes. | **Probably (PreviewServer), check first** |
| L10 | Native: parse `loot`, render it with the existing `GroundItem` inventory sprite, a click/key pickup intent with single flight and no replay, and private refresh. | desktop native client | `online/OnlineConnection.java`, `online/OnlineClient.java`, `OnlineConnectionTest.java` | Adapter tests: parse/stage by epoch, one in flight, an ambiguous failure disconnects without replay. Rendering is checked visually later. | Possibly with pending T4C-0196 native account adapter; check first |
| L11 | **Done (T4C-0228), owner decision 2026-10-27:** corpse recovery follows the shared carry-weight rule, per item, with partial takes. `/recover` takes an optional fourth token naming one corpse stack; absent, every stack that fits is taken in order. A named stack that does not fit is refused (`too_heavy`) with the corpse unchanged; a stack not on the corpse is `item_unavailable`. Gold comes with the first successful call; the corpse closes when empty. Replays fail the revision fence. No schema change. | server persistence/runtime | `OnlineStore.java`, `PersistentSessions.java`, `PreviewServer.java` | PostgreSQL (written, not run locally): partial take, over-weight refusal with corpse unchanged, retry, wrong owner. | No |
| L12 | `NativeLootProbe`: two actual native adapters, kill/drop/observe, a simultaneous claim race, an over-weight refusal, a lost-response reconnect, then a database check that each item exists in exactly one place. | tools | new `multiplayer/tools/NativeLootProbe.java`, `NATIVE_LOOT_PROBE.md` | The probe itself; this is not rendered evidence. | No (needs L6–L10) |

Suggested parallel start, with no collisions: L1, L2 (serialized on `domain/pom.xml`) and L3.
Then L4, which needs L1 and L3, and L5, which needs L2.
L6, L7, L9 and L11 start only after the main checkout's server changes merge. Then do L8, L10 and
L12 in that order. Rendered two-client M2 evidence (MILESTONES.md "Done evidence") remains the
gate after L12.
