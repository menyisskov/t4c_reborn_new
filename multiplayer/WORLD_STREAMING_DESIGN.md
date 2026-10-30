# World hosting with area-of-interest streaming — T4C-0349

Design, 2026-10-30, Asia/Jerusalem. **Design only: no production code, schema, content, protocol or
port changed.** It is a candidate plan for the owner's decision below, not an approved scope, a
passed gate or an M3/Beta completion claim. [MILESTONES.md](MILESTONES.md) (M3),
[PARITY.md](PARITY.md), [HOSTED_STARTER_ROUTE_DESIGN.md](HOSTED_STARTER_ROUTE_DESIGN.md) and
[QUEST_ABILITY_DESIGN.md](QUEST_ABILITY_DESIGN.md) keep their own scope and acceptance rules.

**Owner decision (2026-10-30):** the transit approach is *raise the region bound with
area-of-interest (AOI) streaming*, so players can reach the rest of the world. Single-player's real
maps and real portals are the reference. This replaces the open choice in the T4C-0300 "Hosting
reach audit" (options (a) per-quest hubs with ferry NPCs, (b) bigger region plus AOI, (c) artificial
hub): (b) is chosen; (a) and (c) are not pursued, and no artificial portal is added.

Evidence labels used below. **Measured** = read from a file or a recorded run in this repository.
**Arithmetic** = computed from measured numbers. **Estimate** = reasoned from the code, not yet
measured; each estimate has a stage gate that measures it. Nothing here is a new live run: slice 1
ran no server, database or soak (the only tools used were read-only scripts over the shipped
content and collision files).

## 1. What the hosting model is today

One server process, one simulation thread at 34 Hz (29.41 ms), six hosted regions
(`HostedRegionPlan.startingSlice`): the world-0 `start` crop 249 x 117, and five world-1 pieces
(Lighthaven interior 165 x 166, skeleton cave 111 x 111, three sewer levels). Everything inside a
region is simulated every tick and replicated to every viewer of that region. A character's region
is **derived** from its persisted absolute coordinates (`HostedWorlds.require(world,x,y)`); region
keys are runtime-only (no column stores them), so region geometry can change without a character
migration. Spawn lives are keyed by `authored-v1:<hash of world+anchor+type>` in `online_spawns`,
independent of any region.

## 2. Measurements (measure first)

### 2.1 Collision maps and memory

| World | File (shipped) | Tiles | Compressed | Walkable (foot rule) | Hosted today |
|---|---|---|---|---|---|
| 0 world map | `assets/maps/worldmap/worldmap.colbin` | 6,144 x 3,072 = 18,874,368 | 348,691 B | 6,862,322 | 249 x 117 = 29,133 tiles (0.15%) |
| 1 dungeon | `assets/maps/dungeon/DungeonMap.colbin` | 3,072 x 3,072 = 9,437,184 | 165,301 B | 1,309,766 | five pieces, 149,881 tiles (1.6%) |
| 2 cavern | `assets/maps/cavern/CavernMap.colbin` | 3,072 x 3,072 = 9,437,184 | 208,532 B | 1,513,247 | none |

(Measured with `zlib` over the T4CBIN envelope; inflating world 0 takes about 16 ms. The portal
graph uses worlds 0, 1 and 2 only; `MapDefinition` also lists an underworld map that no portal
reaches. The npc-appearance export has 14 records tagged world 4; they have no map and stay
unhosted.)

Memory of the current loader, **arithmetic from the code**: `RegionMapLoader.load` builds four
`[h][w]` arrays (`blocked`, `terrain`, `foot`, `portalPassable`), then `PreviewMap`'s constructor
clones all four, so a region costs about 4 bytes per tile at rest and about 8 bytes per tile while
loading. Hosting world 0 through the same code would take about 75.5 MB resident and about 151 MB
transiently, and each further world 38 MB. A shared read-only grid at **1 byte per tile** (terrain
code in 4 bits, foot-blocked, above-foot-blocked and portal-passable in three more bits; the
shipped maps have at most 9 distinct codes) takes 18.9 MB for world 0 and 37.7 MB for all three
worlds, loaded once and never cloned. That is within the 654 MiB peak heap and 842 to 879 MiB
working set of the last two-hour soak.

### 2.2 Content to host

| World | Monster records (`monsters-v2.json`, ownership MONSTER) | NPC-owned monster records | NPC appearance records (`npc-appearances-v1.json`) | Interactive NPCs (`npc-interactions-v1.json`) |
|---|---|---|---|---|
| 0 | 7,614 | 20 | 1,010 | 3 (Kilhiam, Warden Aelric, Sigfried) |
| 1 | 2,728 | 58 | 148 | 0 |
| 2 | 3,897 | 76 | 1,402 | 0 |

All 14,239 monster records resolve to a definition; 91% of world 0's records are aggressive (6,917
of 7,634 including the 20 NPC-owned), 99.8% in world 1 and 98% in world 2, none stationary. Hosted
today: about 223 creatures (38 + 11 + 30 + 27 + 46 + 43 + 28) and 6 view-only NPC appearances, so
the three worlds are **64 times** (world 0 alone 34 times) today's creature population, and the
2,560 appearance records of worlds 0 to 2 are far above the client's 64-NPC snapshot cap.

Density matters more than totals: the busiest 128 x 128 block holds 144 monsters in world 0, 235
in world 1 and **652 in world 2**; the busiest 384 x 384 window holds 491, 399 and **959**. A
whole-world snapshot is never the right unit; a viewer's window is.

Spawn anchors that no creature can stand on, **arithmetic** from the foot rule
(`CollisionRules.blocksMovement`) against each spawn's anchor tile: world 0 has 153 of 7,614 (2.0%),
world 1 has 371 of 2,728 (13.6%), world 2 has 1,137 of 3,897 (29%). `PersistentCreatures.publish`
refuses startup on a blocked saved position, and today's guard is a five-entry explicit list
(`HostedRegionPlan.UNHOSTABLE_SPAWN_KEYS`). World 2's rate is high enough (while only 8 of 443
cavern portal targets are blocked) that it needs an audit before it is trusted, not a longer list
(possible coordinate or ownership mismatch for that world); see risk R6.

### 2.3 Cost today (from recorded runs)

| Quantity | Value | Source |
|---|---|---|
| Idle tick, six regions, about 223 creatures, no players | 1.09 ms whole tick; 0.003 to 0.008 ms per region | T4C-0304 (disposable PostgreSQL) |
| Idle tick, three regions (about 80 creatures) | 1.04 ms | T4C-0300 |
| 20 adapters, two hours, eight taking portal trips | tick mean 2.48 ms, max 35.9 ms, 1 of 244,928 ticks over budget, 0 failed; CPU 36.7% of one core; heap peak 654 MiB; working set 842 to 879 MiB; 0 persistence failures | T4C-0315 (`NATIVE_ADAPTER_SOAK.md`) |
| Application response bytes | about 2.44 MB/s total, 122 KB/s per adapter, `/state` 97% | T4C-0315 |
| One creature in `/state` | about 165 compact bytes (6,289 B for 38) | T4C-0214 serializer model |

**Arithmetic.** Native loops poll about every 75 ms (13.3 polls/s at most), so one `/state` is about
9 KB per adapter. Per creature that is 165 B x 13.3 = 2.2 KB/s per viewer.

* The idle tick bounds the *per-creature* work: 1.09 ms / 223 is at most **4.9 microseconds per
  idle creature** including every fixed cost (sessions, loop), and the per-region figures give
  **0.1 to 0.2 microseconds** of creature stepping alone. The 20-player soak adds about 1.4 ms over
  idle, so roughly 70 microseconds per player including `/state` assembly (which runs on the
  simulation thread through `onWorld`), pursuit and persistence hand-off.
* Simulating all 14,239 creatures every tick at the upper bound would cost about 70 ms (2.4x the
  budget); at the lower bound about 3 ms. Neither is the binding limit, which is persistence and
  pathing (2.4).
* A whole-world-0 `/state` with no AOI is 7,614 x 165 B = **1.26 MB** per poll, 16.7 MB/s per
  viewer and about **335 MB/s at 20 viewers**, against 2.44 MB/s measured today; it also exceeds
  the adapter's 256-creature and the client's response caps (2.2 below).

### 2.4 What breaks if the bound is simply raised

Each row is a hard limit found by reading the code. **None of this was run.**

| # | Where | Today | Consequence at 6144 x 3072 / 7.6k creatures |
|---|---|---|---|
| L1 | `OnlineRegion` (shared model) | width and height <= 256 | Descriptor construction throws; the server cannot even build the `Spec`. |
| L2 | `PreviewServer.mapJson` | publishes `blocked` as JSON `boolean[][]`, about 5.7 B per tile (166,878 B for 29,133 tiles, T4C-0214) | about **108 MB** for world 0; client caps `/map` at 2 MiB and 1024 per axis (`OnlineConnection`). |
| L3 | `PreviewWorld` constructor | `spawns.size() > 256` throws | Region cannot be built. |
| L4 | `HostedWorlds.step` | path-permit ordinal `regionIndex * 256L + id` | Ids above 255 collide across regions and corrupt the fair rotation. |
| L5 | `PersistentCreatures` constructor | one `loadOrCreateSpawn` round trip per spawn, serially, before listening | 7.6k to 14.2k sequential statements at startup (time not measured). |
| L6 | `PersistentCreatures.step` | iterates every entry each tick; `clanViews()` allocates a list of every live creature each tick; each clan-capable creature scans all of it | O(n) allocation and O(n x clan-capable) scans per tick; 7.6k clan scans each against 7.6k views is about 58 M tests per tick (Estimate). |
| L7 | patrol checkpoint | every creature patrols (radius 6 tiles) and checkpoints at most every 5 s while dirty; worker queue is 256 | a fully active world 0 is up to about **1,500 position writes per second** against about 45 per second for 223 creatures today (Estimate); the single bounded worker rejects at 256 queued. |
| L8 | path permit | one global A* solve per tick (up to 16 ms) | 7.6k patrollers share 34 solves per second; a patrol replan waits minutes (Estimate). |
| L9 | `/state` | every active creature, every NPC appearance, loot of the whole region | see 2.3; client accepts at most 256 creatures, 64 NPCs, 128 loot (`MAX_GROUND_LOOT`). |
| L10 | `RegionNpcs` | publishes every appearance in the region | world 0 has 1,010, world 2 has 1,402 against the 64-NPC client cap. |
| L11 | `RegionGroundLoot`/`OnlineStore.listGroundLoot` | one rectangle read of the whole region, capped at the 256 oldest rows | wrong set once a region is large; "excess rows must be paged or reported" (T4C-0214). |
| L12 | `PreviewMap` | 4 bytes per tile plus clone | see 2.1. |
| L13 | `PersistentCreatures.publish` | one blocked saved position refuses all startup | 153 / 371 / 1,137 unstandable anchors (2.2). |
| L14 | `HostedRegionPlan` | `ENABLED_PORTALS` allowlist; throws if a refused portal has a hosted destination | with a whole world hosted many more sources/targets coincide; the allowlist must become a derived rule. |
| L15 | native probes | `NativePursuitProbe`/`NativeLootProbe`/soak BFS over the *published* collision grid | `blocked` will not be published for a streamed region; they must read the shipped `.colbin`. |

## 3. Design

### 3.1 Principles

1. **The authority is unchanged.** One server process, one simulation thread, PostgreSQL as the only
   durable truth, `location_epoch` and the session lease as the fences. Streaming changes *what is
   simulated and sent*, not who decides.
2. **Single-player is the reference.** Real maps, real portals, authored spawns, unchanged combat and
   pursuit rules. Single-player code is not touched.
3. **Window, not region.** The *region* stays the unit of identity, epoch and combat isolation. The
   *window* is a per-viewer, server-derived view of it. The client never chooses its window (a client
   asking for a wider window would be a map-hack).
4. **Dormant is not deleted.** A creature outside every window keeps its durable life (health, death
   time, position, target) in `online_spawns` and costs no CPU, memory beyond a small index entry,
   or writes.
5. **Default off.** Everything in slice 2 and later is behind one server option; with it off, the
   current six-region plan, protocol behaviour and tests are byte-for-byte as shipped.

### 3.2 Region model: one region per world, shared read-only collision

* A **streamed region** is the whole world: world 0 = 6144 x 3072 at origin (0,0), key `start` (kept
  so evidence and logs line up), then worlds 1 and 2 at 3072 x 3072 in stage B. In a streamed region
  the descriptor origin is 0, so a region-local coordinate equals the absolute world coordinate and
  `ProgressMirror`, `RemoteWorldDriver` (`originX + local`) and every persisted coordinate keep
  working without conversion. Persisted character, respawn, corpse and loot coordinates are already
  absolute, so **no character migration is needed**; only the descriptor changes, which means a
  rolling restart (drain, stop, start, reconnect) exactly as T4C-0214 specified, never a live origin
  change under old snapshots.
* **`OnlineRegion` bound.** The record keeps validating an upper bound, raised from 256 to **8192**
  per axis (fits 6144 and any shipped map; `RegionMapLoader` already caps at 32768 and 64 M cells).
  The *hosting policy* stays in the server: `RegionMapLoader.Spec` keeps the 256-per-axis rule for
  every region that is not declared streamed, so a mistaken plan cannot silently host a huge region
  without AOI. The client's `OnlineConnection` map check (1024) is raised to the same constant.
* **`SharedCollision`** (new, server): per world, loaded once, immutable, 1 byte per tile with the
  four derived bits of 2.1, shared by every `PreviewMap` of that world. `PreviewMap` gains a
  window-backed constructor that reads through the shared grid with an origin and size; the existing
  array constructor stays for tests and the single-map seam. `PreviewMap.cells()` (used only by
  `/map`) must not be called for a streamed region.
* **Coordinates inside a window** stay region-local doubles exactly as today (creature, NPC, loot and
  player `x`,`y`). No per-window origin is invented: the client already adds the *region* origin
  (`RemoteWorldDriver.apply`: `originX + view.x()`), and a window rectangle is metadata, not a new
  coordinate system. This is simpler and safer than window-relative offsets, because every committed
  server interaction (`/attack`, `/interact`, pickup) already speaks region-local coordinates.
* **World 1 merging.** In stage B the five world-1 pieces merge into one streamed region. Region keys
  of the old pieces disappear; they are not persisted, so nothing to migrate. Portals inside the
  same region (cave mouth to cave) remain transfers with a `location_epoch` increment and the
  readiness freeze (3.8), so player-visible semantics do not change.

### 3.3 Windows

Two server-side rectangles per player, centred on its committed position, clamped to the region,
Chebyshev (square) in tile units. Initial constants (**tunables, my call, to be validated by the
stage A gate; owner can overrule**):

| Window | Half-extent | Purpose | Rationale |
|---|---|---|---|
| Snapshot window | 48 tiles (97 x 97) | what `/state` lists | the 1080p screen shows about 60 x 67 tiles at 32 x 16 px (`GRID_W/H`); 48 keeps a margin for zoom and latency |
| Simulation window | 64 tiles (129 x 129) | creatures that are active | snapshot window plus 16 so nothing visible is frozen, and well beyond natural aggro (8 tiles, `MONSTER_AGGRO_RANGE`) |

Ranges that exceed the window are handled by **stickiness**, not by radius: a creature that has a
target, a pending persistence operation or is still returning home stays active until it is
quiescent, so a chase that runs to the natural leash (30 tiles) or retaliation leash (60 tiles) is
never cut off. Patrol radius is 6 tiles (`MONSTER_PATROL_RADIUS`), so a quiescent dormant creature is
always within 6 tiles of its home anchor.

* **Cell grid.** Hosted creatures and NPCs are indexed in 32 x 32-tile cells by home anchor (a
  `long`-keyed map to `int[]`); the displaced-creature rule in 3.4 covers the exception. Coverage per
  tick is the union of cells touched by player simulation windows (a few dozen cells per player),
  recomputed every 8 ticks (4.25 Hz) and immediately when a player is placed, teleports or
  transfers.
* **Hysteresis.** A cell becomes covered immediately and uncovered only after 15 s (510 ticks)
  without coverage, so a player pacing along an edge does not thrash activation.
* **Caps (guard rails).** At most **1,024 active creatures** in the process (4.6x today's
  population; configurable) and **256 per snapshot** (the adapter limit), nearest first with a
  stable tie-break by id, plus an optional `truncated` hint. Reaching the active cap leaves the
  farthest covered cells dormant and increments a counter in the operator metrics; it is an
  explicit capacity condition, never silent.
* Other players: the player list in a snapshot is limited to players inside the snapshot window
  (20 maximum anyway). NPC appearances: nearest 64 in the window (a pure function of the static
  list, so it can be cached per cell). Loot: nearest 128 in the window, as the viewer cap already
  does, but fed by 3.6.

### 3.4 Creature lifecycle: dormant, active, persisted

States per hosted creature: **dormant** (index entry only), **loading** (batched read in flight),
**active** (full `Entry` and `OnlineMonsterPursuit` controller as today), **settling** (leaving, flush
in flight).

* **Activate** when its cell is covered: add to a bounded batch (at most 64 per tick so a teleport
  into a dense block does not stall the tick). The batch is one new store call (`INSERT ... ON
  CONFLICT DO NOTHING` then `SELECT ... WHERE spawn_key = ANY(?)`), executed by the existing
  persistence worker (its 256-deep queue is for single operations, so one batch counts as one). When
  the rows return, the simulation thread builds the `Entry` and controller from the *durable* row
  with the same code path as today (`publish(entry,true)`): saved HP, position, mode, target, life,
  `state_revision`. A row with `died_at` is run through the existing respawn read, which respawns
  only if the authored delay elapsed (the clock is the database's `clock_timestamp()`), so a creature
  that died while dormant respawns on the first activation after its time, and one still dead stays
  hidden exactly as an in-process dead creature does today. `world.creatureActive(id,false)` hides a
  creature until its row is loaded, an existing mechanism.
* **Deactivate** only when quiescent: not pending, recovering, reserved, `dirty`, not stunned, no
  target and mode IDLE, PATROL or RETURN at home. It first submits one final checkpoint through the
  existing path; only when that completion returns is the entry dropped. A failed checkpoint keeps
  the entry active and retries (the same "no guessed success" rule as `reload`). The dropped entry
  leaves the creature at its persisted position.
* **What dormant creatures keep:** health and death state (they do **not** regenerate or respawn
  early or late; respawn is derived from `died_at` on activation), position (resumes where it was
  last checkpointed, within 6 tiles of home), clan/target fields (always cleared before settling).
  Resuming exactly is deliberate and the simplest correct rule; a "snap home after N minutes"
  catch-up is possible later if testing shows drift is visible (open question Q3).
* **Startup.** No per-spawn round trip. One query reads the existing `online_spawns` rows with a
  non-null current position (only creatures that ever moved or were hurt have one); any whose
  persisted position is more than 8 tiles from home (a chase interrupted by shutdown) go into a
  small *displaced* list indexed by their actual position, and start in their persisted RETURN/mode
  when first covered. Everything else is indexed by home anchor. Rows for never-activated spawns are
  created lazily at first activation, not at boot.
* **Unhostable anchors (L13).** The five-entry constant list is replaced by a generated, versioned
  resource (`unhostable-spawns-v1.json`: persistence key + world + tile + reason) written by an
  audit tool and verified by a test that recomputes it from the real collision maps and the foot
  rule: the list is exactly the unstandable set, never a pattern, and a spawn missing from the list
  that is unstandable (or listed but standable) fails the build. World 2 waits for the audit in R6.
* **Path permit and checkpoints (L7, L8).** Both operate on the active set only, so their cost scales
  with active creatures (at most 1,024, in practice a few hundred), not with the world. The round
  robin ordinal becomes `((long) regionIndex << 32) | id`.
* **Clan scans (L6).** `clanViews()` is built once per tick from the active set only (already the
  case once dormant creatures have no controller) and the per-creature scan is bounded by the cell
  grid of active clan-capable creatures; the T4C-0225 clan authority (Goblin to Wild Horse accepted
  exclusion) is unchanged.

### 3.5 Snapshots and protocol

* `/state` builds the reply per viewer from the cell index, not from a region-wide list (cost O(cells
  in the window + creatures in them) instead of O(creatures in the region)); a streamed region has no
  global creature list. Absence-means-despawn already holds in `RemoteWorldDriver.applyCreatures`,
  so a creature leaving the window disappears and returns as a puppet with no client change; the
  snapshot window being larger than the screen hides the pop-in.
* New optional snapshot field `window` {`x`,`y`,`width`,`height`} (region-local tiles): the
  rectangle the lists are complete for (clamped to the region). Absent means "whole region", the
  current meaning. New optional `truncated` boolean. `/map` for a streamed region carries
  `streamed: true`, the real `width`/`height` and **no `blocked` grid**; the real client already
  derives collision from the shipped asset (`RemoteMovementRule` uses `CollisionManager`), so only
  the headless probes need the `.colbin` instead (L15).
* **Protocol version policy.** The repository's rule is an exact `OnlineProtocol.VERSION` match
  checked before credentials (`/map`, 426), and a bump whenever client and server must change
  together. Dimensions above the old 1024 cap, a missing `blocked` grid and the new `window` meaning
  are exactly that. Decision: **one bump, 8 to 9, in stage A**, covering `streamed`, `window` and
  `truncated` together. Later stages add *content*, not wire shape, and must use only optional
  fields (never a second bump for hosted content growth). An old client against a new server fails
  with the existing incompatible-version lobby text. With the feature flag off the server still
  advertises 9 (a client and server ship together, `CLIENT_DISTRIBUTION.md`), so the flag changes
  hosting, not the wire. The bump is the only unconditional change; it touches
  `OnlineProtocol.java` and every test that asserts the number.
* Bandwidth, **arithmetic**: with the 256-creature cap a worst-case `/state` is 42 KB (560 KB/s per
  viewer, 11 MB/s at 20), 4.6 times today's total, only in the densest places; a typical 97 x 97
  window is under 100 creatures (16 KB). No compression or delta format is part of this design; if
  stage A's gate shows the dense case matters, the follow-up is response compression and dropping
  rarely-needed creature fields, in a separate protocol-compatible task.

### 3.6 NPCs, loot, quests

* **NPC appearances** are static per region: build the cell index once; a snapshot lists the nearest
  64 inside the window. Interactive NPCs (`interactionSupported` true) are always preferred over
  appearance-only ones when truncating. `RegionNpcs.reachableShopkeeper/prepare` already take a
  region and a position; only their lookup moves from a list scan to the cell index.
* **Loot.** `RegionGroundLoot` today caches one rectangle. In a streamed region it caches **per
  covered loot cell** (the same 32-tile grid): the once-per-second read becomes one query with
  `ANY(cells)` (or one rectangle per cell), capped per cell, and the viewer list is the nearest 128
  in the window. A cell that reaches its row cap reports capacity explicitly (a metric and the
  `truncated` hint) rather than presenting a partial list as complete (the T4C-0214 rule). Pickup
  range (8 tiles Chebyshev) is far inside the window, so ownership and race rules are untouched.
* **Quests are not exposed by hosting.** Quests are offered only by NPCs in
  `npc-interactions-v1.json` (3 NPCs today: Kilhiam, Warden Aelric and Sigfried) and credited only through the quest catalog
  (`quests-v1.json`, one allowlisted quest). Hosting the whole world adds terrain, creatures and
  display-only NPCs, not new quests. Stage C adds quests one by one (3.9).

### 3.7 Memory and per-creature state

* Collision: 18.9 MB (world 0), 37.7 MB for three worlds (2.1), shared and read-only.
* Dormant creature: the content `Spawn` record (already resident today, shared with the immutable
  `MonsterContent`) plus one index entry of a few tens of bytes. **Estimate:** 14.2 k records is
  under 10 MB over today. Active creature: today's `Entry` plus `OnlineMonsterPursuit` (patrol
  `Random`, path list); at most 1,024 of them.
* `PreviewWorld` per-creature maps (`creatureHealth`, `creaturePositions`, `inactiveCreatures`) are
  `Map<Long,...>`; they stay valid but are sized by the active set in a streamed region (ids are the
  stable index into the region's sorted spawn list, as today, but they are only *stored* for active
  creatures).
* Target: working set below 1.5 GB with 20 adapters and the full three-world plan, against 879 MB
  today (**gate**, stage B).

### 3.8 Fencing, epochs, boundaries

* `location_epoch` and the session lease are **unchanged**. Walking inside a streamed region never
  changes the epoch and never changes the descriptor (the window is not part of
  `OnlineRegion.equals`, so `RemoteWorldDriver` does not reset). Portal, death and recall transfers
  keep their epoch increment, readiness freeze (`awaitingRegionReady`) and database fencing, **even
  when source and destination are in the same streamed region**; stage B proves `PortalTransfer`
  permits identical region keys (recall already may) and that stale input from the old position is
  refused by epoch, as in `HostedTransferTest`.
* Creature fencing: every durable creature mutation already carries `state_revision` and the life
  `event_id`; activation loads them, checkpoints compare them, and a conflict reloads (never replays).
  A late completion for an entry that has since been deactivated and reactivated carries the old
  `cacheGeneration` and is discarded, as `reload` does today. Dormant creatures are not simulated by
  anyone, so there is no second writer to fence; a stale second server process remains stopped by
  the same revision compare-and-set that protects it now.
* Combat, targeting and corpse/loot visibility stay region-scoped. Players in different regions still
  cannot interact; players in the same streamed region only interact when inside range and sight,
  which is already the rule, and are now additionally never *sent* beyond their snapshot window.
* **Region boundaries and refusals.** `unhosted_region` (409) stays the answer for any saved or
  requested position outside every hosted region. Stage A makes all of world 0 hosted, so a save at
  e.g. (3700,1450), refused today, joins; worlds 1 and 2 stay as shipped until their stages.
  Portals: `ENABLED_PORTALS` becomes a **derived** rule (every authored portal whose source and
  target are hosted and whose target is standable, minus an explicit short denylist) while the
  existing `refusedPortals` machinery keeps refusing the rest without moving the player or touching
  the epoch. The player-visible "route unavailable" feedback remains the separate protocol/native
  M3 item; this design does not change it.

### 3.9 Performance and soak budget

Fixed targets, unchanged from the Beta rules: 34 Hz, 20 player slots, 20 native adapters.

| Quantity | Budget | Basis |
|---|---|---|
| Idle tick, full world(s) hosted, no players | <= 2.0 ms mean | today 1.09 ms; dormant creatures cost nothing; the coverage pass is O(players) |
| 20 adapters, soak | tick mean <= 5 ms, over-budget ticks no worse than today's 1 in 2 h, 0 failed, 0 deadline resets | today 2.48 ms mean |
| CPU | <= 60% of one core | today 36.7% |
| Heap peak / working set | <= 1 GiB / <= 1.5 GB | today 654 MiB / 879 MB |
| `/state` | <= 42 KB worst case, <= 20 KB p95 | 3.5 |
| Activation burst | a teleport into the densest 97 x 97 window (world 2) activates in <= 1 s without a tick over 29.4 ms | 64 per tick |
| Checkpoint writes | <= 100 per second sustained, 0 worker rejections | today about 45 per second (Estimate) |

Stage soaks use the existing native adapter soak with a new mode that **spreads adapters over the
streamed world** (random walks and portal trips, not all in the temple) and records, in addition to
today's report, active creature count, activation batch times, dormant count and bytes per `/state`.

## 4. Staged plan with gates

Each stage is a separate PR (or a few), merged and re-branched from `main` before the next. A stage is
not done until its gate is recorded in `ACCEPTANCE.md`; the sections below are the gate, not a
promise of dates.

### Stage A: raise the bound and host all of world 0 with AOI simulation (flag, default off)

Scope: `OnlineRegion` bound; `SharedCollision` and window-backed `PreviewMap`; streamed `Spec`;
whole world 0 as region `start`; cell index, activation/deactivation, batched spawn load, per-viewer
windows for creatures, players, NPCs and loot; the 8 to 9 protocol bump; the generated
unhostable-spawn list for world 0 (153 anchors to start); derived portal enablement for
portals whose both endpoints are hosted (worlds 1 and 2 remain as shipped, so portals into them stay
refused except the currently enabled ones). Option name: `--stream-world0` (server) with the same
setting available to test fixtures. Flag off = today's plan.

Tests (no database unless noted):
* `OnlineRegion` accepts 6144 x 3072 and rejects 8193; a non-streamed `Spec` still rejects > 256.
* `SharedCollision` equals `RegionMapLoader`+`PreviewMap` on every tile of the starter crop and on
  random tiles of the full map (terrain, foot, above-foot, portal-passable).
* Cell index and coverage: union, hysteresis, clamping at the map edge, 20 windows, stable ids.
* Snapshot window: creature, NPC, loot and player lists equal the brute-force filter of the region
  list for random viewers; caps and nearest-first tie-break; `truncated`.
* Unhostable list test (3.4) for world 0.
* PostgreSQL: activation loads durable HP/position; a dead creature respawns only after its delay;
  deactivate flushes; failed flush keeps active; stale completion discarded; restart with a
  displaced creature; a character saved at (3700,1450) joins.
* Existing suites unchanged and green with the flag off: `HostedKillRegionTest`,
  `HostedTransferTest`, `HostedQuestCompletableTest`, `PersistentCreaturesTest`,
  `PreviewServerTest`, `PreviewWorldTest`, plus the client `RemoteWorldDriverTest` and
  `OnlineConnection` tests for the new fields.

Measurements/gates: idle tick with full world 0 and no players (slice 2 records it first); a 30 minute
native soak with 20 adapters spread over world 0 against the budgets in 3.9; one rendered client
walking from the temple along the shore to a far monster field, kills and returns, with the second
client on another continent (windows independent); server restart mid-chase. Evidence on a
disposable PostgreSQL cluster only.

### Stage B: worlds 1 and 2, every portal

Scope: worlds 1 and 2 as streamed regions (merging the five world-1 pieces), the world-2 spawn audit
(R6), derived portal enablement for the whole graph (1,118 portals), same-region transfers, and an
exact-denylist of anything single-player itself does not let a player use. Option `--stream-all`.

Tests: `HostedKillRegionTest` successor flooding each world's portal graph from the real maps;
`HostedTransferTest` walking one portal of every (source world, target world) pair in the graph
(0-0, 0-1, 0-2, 1-1, 1-0, 1-2, 2-2, 2-0, 2-1) through the real session transfer path; the denylist
and `refusedPortals` equal the computed set; stale-input refusal across a same-region transfer.
Gate: 2 h native soak, 20 adapters, with portal trips between all three worlds, against 3.9;
memory below 1.5 GB; the T4C-0315 failure classes (rejoin lockout, movement-rule guard) absent.

### Stage C: all NPC and quest content, quest by quest

Scope: nothing is switched on in bulk. Per quest: add its giver to the interaction catalog and its
definition to the exporter allowlist (`OnlineNpcContentExporter`, `OnlineQuestContentExporter`),
only after the quest passes `HostedQuestCompletableTest` (giver hosted and talkable, enough hosted
targets inside the objective circle, required items `item.<key>` normalised, rewards supported).
Quests whose mechanics online play lacks (unsupported scripts, spells, unique drops) stay out and
are listed. NPC appearance-only records stay display-only until their scripts are ported
individually. Gate per quest: the test above plus a native probe walking the quest end to end (the
M3 two-player rule); gate for the stage: the M3 bullets in `MILESTONES.md`, evidence in
`ACCEPTANCE.md`. Each quest is its own small PR and can run in parallel with stage B for worlds the
quest touches once those worlds are streamed.

## 5. Files per stage (to split work between Codex and Claude)

New files are marked `+`. All paths are under `multiplayer/server/src/main/java/com/perso/T4C/server/`
unless a root path is shown.

**Stage A**
* Shared model: `src/main/java/com/perso/T4C/online/model/OnlineRegion.java` (bound),
  `OnlineProtocol.java` (9), `src/main/java/com/perso/T4C/online/OnlineConnection.java` (`MapInfo`
  `streamed`, optional `blocked`, 8192 cap, snapshot `window`/`truncated`), and its client tests;
  `RemoteWorldDriver.java` only if the tests show a gap (expected none).
* Server core: `RegionMapLoader.java`, `PreviewMap.java`, `+SharedCollision.java`,
  `+AreaOfInterest.java` (windows, cells, coverage), `PreviewWorld.java` (active-only maps,
  windowed `snapshot`, the 256 cap), `PersistentCreatures.java` (lifecycle, batch load, startup
  scan), `HostedWorlds.java` (step order, permit ordinal), `HostedRegionPlan.java` (streamed
  specs, derived portals, flag), `PersistentSessions.java` (window per binding, target views,
  loot cells, activation hooks on placement and transfer), `PreviewServer.java` (`/map`, `/state`),
  `RegionNpcs.java`, `RegionGroundLoot.java`, `ServerMain.java` (option),
  `persistence/OnlineStore.java` (batch spawn load, displaced scan, per-cell loot read),
  `OnlineMonsterPursuit.java` only for the quiescence predicate.
* Content/tools: `+src/main/resources/content/unhostable-spawns-v1.json` and its audit tool under
  `src/main/java/com/perso/T4C/tools/`, `multiplayer/tools/NativeAdapterSoak.java` (spread mode,
  new counters), `NativePursuitProbe.java`/`NativeLootProbe.java` (collision from the `.colbin`).
* Tests: new `AreaOfInterestTest`, `SharedCollisionTest`, `StreamedSnapshotTest`,
  `PersistentCreaturesLifecycleTest`, `UnhostableSpawnsTest`, `HostedWorld0Test` plus edits to the
  suites named in stage A.
* Docs: this file, `PARITY.md`, `MILESTONES.md`, `LOAD_METRICS.md` (new metrics),
  `NATIVE_ADAPTER_SOAK.md`.

**Stage B**: `HostedRegionPlan.java`, `HostedWorlds.java`, `PortalCatalog.java`,
`PortalDestinations.java`, `PersistentSessions.java` (same-region transfer), `persistence/PortalTransfer.java`
(only if identical keys are rejected), the unhostable list generator for worlds 1 and 2, `HostedKillRegionTest`,
`HostedTransferTest`, the soak driver, `multiplayer/server/.../portals-v1.json` consumers
(`OnlinePortalContentExporter.java` only if the denylist needs a source marker).

**Stage C** (per quest): `src/main/java/com/perso/T4C/tools/OnlineNpcContentExporter.java`,
`OnlineQuestContentExporter.java`, the generated `npc-interactions-v1.json` and `quests-v1.json`
(with the matching client copies), `QuestCatalog.java`, `OnlineQuests.java`, `OnlineNpcInteractions.java`,
`HostedQuestCompletableTest.java`, a quest walk probe, `MILESTONES.md`/`ACCEPTANCE.md`.

**Suggested split.** Codex: stage A server core and its tests (`PersistentCreatures`,
`PreviewWorld`, `HostedWorlds`, `AreaOfInterest`, `SharedCollision`), since it owns the persistence
and pursuit lane. Claude: the shared model and client (`OnlineRegion`, `OnlineConnection`, protocol
bump, `RemoteWorldDriver` checks), `PreviewServer`/`PersistentSessions` window plumbing, the soak and
probe changes and the evidence runs, plus stage C quest by quest. The two lanes meet at the
`AreaOfInterest` API (window and cell value types), which should be merged first as its own small PR.
Both must check `PEER_WORK.md` for live claims on `HostedWorlds`, `OnlineRegion`, `PreviewServer`,
`PersistentSessions`, `PersistentCreatures` and the snapshot files before editing; a task name is not
a handoff.

## 6. Migrations and rollout

* **Schema: none required.** `online_spawns` rows are created on demand and keyed by authored
  persistence key; the new queries are read-only or use the existing insert-on-conflict. The only
  optional addition is an index to make the startup displaced scan cheap, which can be a normal
  forward migration with the usual schema-version bump if the measured query is slow.
* **Content:** one new generated resource (unhostable list). The monster and NPC exports are not
  rewritten.
* **Rollout:** drain and stop the old server, start the new one with the flag, reconnect clients with
  the matching protocol. Characters saved anywhere in a newly hosted world are admitted; characters
  saved in an unhosted place stay refused (`unhosted_region`), never warped.
* **Rollback:** flag off returns the six-region plan. A character saved outside the old crops while
  the flag was on is refused on rollback (explicitly, not repaired); the rollback note in
  `HOSTED_STARTER_ROUTE_DESIGN.md` applies: keep the larger plan available or restore a matching
  database, do not erase progress. Spawn rows written by streaming are valid under both plans.

## 7. Risks

| # | Risk | Mitigation |
|---|---|---|
| R1 | A refactor of `PreviewWorld`/`PersistentCreatures` regresses today's hosted behaviour (pursuit, retaliation, loot, clan combat) | flag off keeps the old path; the lifecycle wraps the existing `Entry`, it does not rewrite it; the full existing suites are a stage A gate |
| R2 | Activation storms (teleport into a dense block, 20 players at one hot spot) stall the tick or exhaust the persistence queue | 64-per-tick batches, single batch statement, 1,024 active cap with a metric, activation-burst gate |
| R3 | Visible pop-in or frozen monsters at window edges | snapshot window (48) inside the simulation window (64), 15 s hysteresis, sticky pursuit |
| R4 | A creature chased out of its window is dropped mid-chase or deactivated with a stale target | quiescence predicate; test of a chase running 30 and 60 tiles; deactivation only after a confirmed checkpoint |
| R5 | Larger dense `/state` raises bandwidth beyond the soak envelope | 256 cap, worst-case arithmetic in 3.5, measured in the stage A soak; compression is a possible follow-up |
| R6 | World 2 (cavern) spawn data disagrees with its collision map (29% of anchors on blocked tiles) | audit before stage B; no blanket skip list; may need an exporter or coordinate fix |
| R7 | Unhostable anchors in world 0/1 (153 and 371) are not 'silent skips' but reduce authored population | generated, tested exact list with reason; counts reported in the evidence |
| R8 | Quests become reachable but unfinishable | quests are allowlisted per quest and gated by `HostedQuestCompletableTest` (stage C); hosting terrain does not add quests |
| R9 | Protocol bump strands probes and packaged clients | single bump in stage A; probes updated in the same PR; `CLIENT_DISTRIBUTION.md` already ships client and server together |
| R10 | Same-region portal transfers expose an assumption that source and destination regions differ | stage B test; epoch and readiness semantics kept as the fence |
| R11 | Content or save written while streaming is on admits players into places unreachable on rollback | rollback note above; refused explicitly, never repaired |
| R12 | Hot-spot concentration (20 players in world 2's densest block) overloads one tick | active cap, per-snapshot cap, and the gate covers a deliberate hot-spot run |
| R13 | Peer conflicts: the files in section 5 are the most contended in the repo | check `PEER_WORK.md`, land `AreaOfInterest` first, one integrator per checkout |
| R14 | Memory of three worlds plus the first activations pushes the heap | shared 1 byte per tile grid (2.1), lazy activation, heap gate |

## 8. Open questions for the owner

* **Q1.** Window sizes (48 snapshot, 64 simulation) are my initial call; the camera zoom range of the
  real client decides whether 48 is enough. I will confirm against the actual screen before stage A
  and record the result in `DESIGN_GUIDELINES.md`.
* **Q2.** Active-creature cap of 1,024 and snapshot cap of 256 are guard rails, not content rules.
* **Q3.** Dormant creatures resume at their persisted position and health. If you prefer
  single-player-like "wounded monsters heal while you are away" or "return to anchor after a while",
  that is a small, separate rule after stage A.
* **Q4.** The first slice of stage A hosts only world 0 and keeps the current world-1 pieces; portals
  into world 1/2 beyond the shipped ones stay refused until stage B. Say if world 0 alone is not
  worth shipping without B.

## 9. Slice record

* **Slice 1 (this PR):** this document, the TASKS row, the design-guidelines decision, the milestone
  and starter-route pointers, a PEER_WORK note.
* **Slice 2 (not started in this PR):** stage A behind the default-off option, with tests and an
  idle-tick measurement of the full world-0 plan with no players.
* **Slice 2, part 1 (T4C-0359):** see section 11. Part 2 (creature activation, T4C-0366): requirements in section 10, record in section 12.

## 10. Owner requirements for stage A part 2 (creature activation), 2026-10-30

Binding requirements; they refine 3.4 and the stage A gate. Part 1 does not implement them (the
streamed world 0 hosts no creatures yet).

1. **Dormant costs nothing, wakes fast, and is awake before the player arrives.** A creature with no
   player in range is neither simulated nor held in memory (no `Entry`, no controller, no per-creature
   map entries; only the shared immutable content `Spawn` and an index slot). It wakes quickly when a
   player walks up *and* when one teleports or portals in. For every arrival (portal, respawn,
   recall, transfer, join) the destination window must be **activated before the player is released
   at the destination**: pre-activate during the transfer commit and readiness handshake
   (`awaitingRegionReady` already freezes the actor), or hold the arrival until the first activation
   batches have landed. The player must never see an empty room.
   * Spec to write and then measure in part 2: activation latency for the busiest 384 x 384 windows
     (about 500 creatures in world 0, 959 in world 2; the 97 x 97 snapshot window is far smaller) at
     the 64-creatures-per-tick rate: 500 creatures is 8 ticks (0.24 s), 959 is 15 ticks (0.44 s) of
     pure rate, plus the batch read (**arithmetic, not measured**). If the measured latency
     including the database batch exceeds the arrival hold budget (target: at most 1.0 s from
     commit to release), raise the batch size (it is a constant, not a design limit) or activate the
     snapshot window first and the rest of the simulation window next.
   * Gate: a test that portals/teleports a player into the densest world-0 and world-2 windows and
     asserts every creature of the snapshot window is active in the first snapshot the player
     receives; a latency record for both in `ACCEPTANCE.md`.
2. **Exception-only persistence.** A creature at full health, at its spawn home, with no target and
   no pending life event is fully described by the content files and needs **no `online_spawns`
   row**. Rows are created and updated only for creatures that deviated: wounded, dead or waiting to
   respawn, displaced from home, or holding a target. When a deviated creature returns to the
   pristine state, its row is deleted or marked pristine (choose one in part 2 and test it). Startup
   and database writes become proportional to what players disturbed, not to the 14,239 monsters;
   the 3.4 startup scan becomes "read the deviated rows only". This replaces the "create the row at
   first activation" rule of 3.4; the per-spawn `loadOrCreateSpawn` round trip and its insert for
   pristine creatures go away.
   * Gates: activating then deactivating an untouched creature writes zero rows; a wounded creature
     writes exactly one and survives a restart; a creature that heals back to pristine leaves no
     stale deviation; startup time with 0 and with N deviated rows is recorded.

File lists in section 5 are unchanged, plus: `OnlineStore` gains the deviated-rows read and the
delete/pristine rule; `PersistentSessions`/`PortalTransfer` gain the pre-activation hook before
`regionReady` releases a frozen arrival.

## 11. Stage A part 1 record (T4C-0359, default-off `--stream-world0`)

Scope shipped: the shared pieces, the option and the protocol bump; **no creature activation**. With
`--stream-world0` the server hosts region `start` as the whole of world 0 (6144 x 3072, origin 0, same
key and spawn) next to the five unchanged world-1 pieces. The streamed region lists no creatures
(the world's 7,614 spawns stay dormant content until part 2). With the option off nothing changes
except the protocol number.

What was built: `AreaOfInterest` (window math, 32-tile cells, 510-tick release hysteresis; pure),
`SharedCollision` (1 byte per tile, 4-bit terrain palette plus foot, above-foot and portal bits;
verified tile by tile against the file and against the array loader), a window-backed `PreviewMap`,
`RegionMapLoader.Spec.streamed` (the 256 edge rule stays for every non-streamed spec; a streamed one
may reach the model bound), `OnlineRegion` bound 8192 (`OnlineRegion.MAX_DIMENSION`), protocol 9
with `/map` `streamed` (no `blocked`) and `/state` `window` and `truncated` (absent when not
streamed), windowed player, NPC and loot lists, the client accepting a streamed map up to 8192 and
validating the window (a streamed snapshot must carry one, it must fit the region, and everything
listed must lie inside it; a window on a non-streamed map is refused).

Limits handled: L1 `OnlineRegion` 8192; L2 `/map` publishes no grid for a streamed region (188 bytes
instead of about 108 MB); client map cap stays 1024 for a map that is not streamed; L3 the 256 spawn
cap in `PreviewWorld` still guards every region that lists its creatures, the streamed one lists
none yet; L4 path-permit ordinal is `(regionIndex << 32) | id`; L9/L10 NPC and player lists are
windowed (NPC: interactive first, then nearest, at most 64); L11 loot is filtered to the window and
cut at 128, but the region cache still does one whole-region read capped at 256 oldest rows (per-cell
loot reads are part 2); L12 no per-region copy of collision; L14 portals: no portal is newly derived
(all 12 cross-region routes between hosted regions were already enabled; the 76 world-0 to world-0
portals are same-region and stay refused with an explicit reason until stage B). L5-L8 and L13 are
creature topics and belong to part 2.

Deviations from the design text:
* NPC appearances: 652 of world 0's 1,010 records have no verified sprite frame (or are not
  deployable) and `inRegion` refuses them in a plain region. A streamed region leaves them out
  (358 are published, including all 3 interactive NPCs) instead of failing startup.
* The window type is `AreaOfInterest.Window` on the server and `OnlineWindow` in the shared model
  (same JSON shape); the design named only one.
* `--stream-world0` is a trailing argument of `--online COLLISION_FILE PORT`.
* **Float precision (new risk).** Movement runs in float world pixels in region-local coordinates.
  In a streamed region local equals absolute, so at x = 3700 a pixel coordinate is about 118,000
  and a float step is about 0.008 px (0.016 px at the far east edge). Same-origin walks over the
  shared grid and over the old arrays are identical to 1e-9 (tested); but against an origin-offset
  crop the same walk drifts by a tile or two over hundreds of ticks (timing of glide arrivals). It is
  not a rule difference, and the offline client has the same precision, but if a visible speed
  difference at the far east matters, step in a viewer-relative frame (part 2 or later).
* Build pitfall found: `OnlineProtocol.VERSION` is a compile-time constant inlined into callers.
  Building the server module without `-am` against a stale installed `game-domain` jar bakes the old
  number into `PreviewServer`; always build `multiplayer` with `-pl server -am`.

Measured (this machine, Windows, JDK 21, 2026-10-30, disposable PostgreSQL 16 in WSL):

| Quantity | Value |
|---|---|
| Idle tick, no players, paced 34 Hz, 1,200 counted ticks, default six-region plan | 0.557 ms mean |
| Idle tick, no players, same, `--stream-world0` (streamed world 0, no creatures) | 0.462 ms mean (lower because the 38 start-crop creatures are not hosted yet; this is not the part 2 number) |
| `/state` at the starter town (temple spawn, one player, 3 NPC views near, no creatures) | 4,572 bytes |
| `/state` model for a viewer in an empty window (no NPCs, no creatures, no loot) | about 330 bytes |
| `/map` for the streamed region | 188 bytes (was about 166 KB for the 249 x 117 crop) |
| Plan build with world 0 streamed, including shared collision, portals and the five pieces | 325 ms (126 ms for the default plan; loaded cold in one JVM, so indicative) |
| Heap held by the streamed world-0 plan | 20.4 MB (grid 18.0 MiB = 18,874,368 bytes, matches the 18.9 MB arithmetic) |
| Shared grid load alone | 68 ms, 19.0 MB |

Not measured in part 1: server process working set, activation cost, `/state` with creatures
(165 B each), the soak. Part 2 owns them (gates in sections 4 and 10).

### Handoff for part 2 (creature activation)

Start from: `PreviewWorld` streamed constructor (`map.streamed()` skips `content.region`, so
`creatures`, `creaturePositions`, `inactiveCreatures` and `creatureHealth` are empty and sized by the
active set once activation exists); `AreaOfInterest.Coverage` (per-tick cell coverage with
hysteresis, windows `SIMULATION_HALF_EXTENT` = 64 per player, not wired to anything yet);
`HostedWorlds.step` and `PersistentCreatures` (which still takes the region's spawn list in its
constructor and does one `loadOrCreateSpawn` per spawn, replace with the exception-only rule of
section 10); `OnlineMonsterPursuit` path search allocates `width * height` ints per solve (about
19 M for world 0), so it needs a windowed or hashed search before any creature can chase in a
streamed region; `RegionGroundLoot` per-cell reads; `PersistentSessions` for the pre-activation
hook (section 10); `WORLD_STREAMING_DESIGN.md` section 5 file list and the unhostable list tool.
Remaining test list is in section 4 (cell index, lifecycle, unhostable list, PostgreSQL gates).

## 12. Stage A part 2 record (T4C-0366, default-off `--stream-world0`)

Scope shipped: creature activation and deactivation for the streamed world 0, exception-only persistence,
the arrival hold, the windowed pursuit search, per-cell loot reads, the generated unhostable list, and the
soak spread mode. With the flag off the hosted plan, its tests and its startup path are the ones that
shipped (the full server suite, 410 tests, passes unchanged).

### What was built

* `CreatureCellIndex` (new, pure): the hosted spawn anchors of the region in the 32-tile cells of
  `AreaOfInterest`, plus an overlay for a creature that stands away from home (a wounded one that went
  dormant, or one displaced when the server stopped). A dormant creature costs one `CreatureSpawn` record
  (shared, immutable), one int in a cell array and one byte of lifecycle state; it has no `Entry`, no
  controller and no per-creature map entry.
* Lifecycle (`PersistentCreatures`, streamed mode): `DORMANT -> QUEUED -> LOADING -> ACTIVE -> SETTLING ->
  DORMANT`. `AreaOfInterest.Coverage` is fed every 8 ticks with one 129 x 129 simulation window per player
  (`PreviewWorld.simulationWindows`, frozen arrivals included) and immediately by `requestWindow` on an
  arrival. Newly covered cells queue their spawns; at most 64 per tick are dispatched, as one
  `online_spawns` read by key (`OnlineStore.loadSpawns`, one statement, no insert) on the existing
  persistence worker. A row with `died_at` is run through the existing respawn rule inside the same worker
  call, so a creature that died while dormant respawns on the first activation after its delay. The active
  cap (1,024) is an explicit counter (`cap-refusals`), never silent.
* Deactivation, once a second: a creature outside every covered cell (home cell and current cell) is
  settled only when nothing is pending, recovering, reserved, stunned or targeting and its mode is idle or
  patrol. Pristine and without a row: dropped, zero writes. Wounded or dead: the last state is flushed first
  and the creature is dropped only after the checkpoint is confirmed (a failed checkpoint keeps it live and
  retries). A chase, a return or a clan fight is sticky: a window never cuts it off.
* Exception-only persistence: a creature at full health, at home, with no target has no row. Rows appear
  only inside the transaction of the first write that needs one (`OnlineStore.ensureSpawn`, an
  `INSERT ... ON CONFLICT DO NOTHING` in the checkpoint, strike, provocation and spell transactions, with
  the same life id the creature already carries), and are deleted again by
  `OnlineStore.deleteSpawnIfPristine` (guarded by life, revision, health, target, nobody targeting it and
  no reward on that life) once the creature is pristine, within a second of healing back and on
  deactivation. A pure patrol position change is never checkpointed for a creature without a row. Startup
  reads the existing rows once (`listSpawnPositions`), indexes any creature standing outside its home cell by
  where it stands, and creates nothing. **No schema change or migration**: the table already allowed absent
  rows, and rows written by the old per-spawn startup keep working (they are read like any deviated row and
  removed when their creature is next seen pristine).
* Arrival hold: `PersistentSessions.requestArrival` activates the destination window at `join` and at the end
  of every death, recall or portal transfer (during the commit and the client's region load);
  `regionReady` no longer unfreezes the actor until `windowActive` (every creature whose home is in the 97 x
  97 snapshot window is live), or 3 s have passed so a database stall cannot strand a player. The HTTP reply
  to the ready call is unchanged; the actor simply stays frozen a little longer.
* `OnlineMonsterPursuit.findPath` keeps flat arrays for regions up to 2^20 tiles (all shipped ones) and uses
  hashed bookkeeping above that, bounded by the existing 8,000-expansion limit; the result is identical.
* `RegionGroundLoot` of a streamed region reads only the covered cells, merged into rectangles, in one
  statement (`OnlineStore.listGroundLoot(world, rectangles)`, at most 1,024 rows) and re-reads when a cell
  becomes covered. The 256-oldest whole-region read is not used for a streamed region.
* The generated list `unhostable-spawns-v1.json` (`UnhostableSpawnAudit` writes it, `UnhostableSpawnsTest`
  recomputes it from the real `.colbin` and requires byte equality): 576 of 7,614 world-0 records stay
  out. 153 are anchors on foot-blocked tiles (the design's count), 37 more stand on a tile whose tile above
  is blocked (a creature's home must pass the player stand rule, `OnlineMonsterPursuit` constructor), and 386
  have no walk sprite (11 of the 397 sprite-less records are already in the first two groups). That leaves
  7,038 hosted.
* `OnlineStore.loadSpawns`, `listSpawnPositions`, `deleteSpawnIfPristine`, `listGroundLoot(rects)`;
  `PreviewWorld` grows and shrinks its live creature set (`activateCreature`/`deactivateCreature`,
  `creatureSpawn(id)`), `OnlineMelee`, `OnlineSpellStrikes` and `OnlineClanCombat` look a creature up by id
  instead of caching or scanning the construction-time list.
* Soak: `NativeAdapterSoak` takes `T4C_SOAK_STREAM_WORLD0=1` (the streamed plan; adapters hold the tile they
  joined on instead of walking back to the temple; route search is bounded to a 201 x 201 box on a streamed
  map) and `multiplayer/tools/SoakSpread.java` moves the twenty QA characters to twenty well separated
  monster fields. The server prints one `streamed-creatures ...` line every 30 s (active, loading, queued,
  settling, covered cells, activated, deactivated, pristine rows deleted, batches, slowest batch, cap
  refusals, failed, peak active).

### Gates (disposable PostgreSQL 16, real world-0 content; `StreamedCreaturesDatabaseTest`, all pass)

| Gate | Result |
|---|---|
| Startup reads no per-spawn rows | 0 `online_spawns` rows for the 7,038 hosted spawns after construction |
| Activate then deactivate an untouched window writes 0 rows | 232 creatures activated and 232 deactivated around a player standing in the densest 129 x 129 window; 0 rows throughout |
| A player standing among 59 to 91 creatures | rows only for the aggroed ones (2 to 4 of them, each targeting); 0 pristine rows |
| A wounded creature writes exactly one row and survives a restart | 1 row after one landed hit; 1 row after shutdown; after a restart it activates wounded (health below authored maximum) |
| A creature that is pristine again leaves nothing stale | row set back to the content state, then activated: deleted within 3 s, 0 rows, `pristineRowsDeleted` = 1 |
| Arrival: every snapshot-window creature live before release | join into the densest window (175 creatures in 129 x 129, 91 in the 97 x 97 snapshot window): client "ready" at once, actor stayed frozen 117 to 141 ms, first snapshot after release listed all 59 creatures strictly inside the window |
| Activation latency, busiest 384 x 384 window (559 creatures) | nine arrivals 128 tiles apart, all 559 live in 297 to 313 ms (9 batches, slowest database batch 2 ms on the pooled store, 8 to 20 ms on the unpooled test store); under the 1.0 s budget with the batch left at 64 per tick |
| Existing suites with the flag off | server module 410 tests, 0 failures (includes `HostedKillRegionTest`, `HostedTransferTest`, `PersistentCreaturesTest`, `PreviewServerTest`, `PreviewWorldTest`, `OnlineMeleeTest`, `OnlineRetaliationTest`, `OnlineClanCombatTest`) |
| Pursuit in a streamed region | 26 of 26 placements single-player's creature A* can reach are followed on the 19 M-tile map (`StreamedPursuitTest`) |

World 2 (959 creatures in its busiest 384 window) is not hosted until stage B, so its latency is arithmetic
only: 15 ticks at 64 per tick (0.44 s) plus one batch read of the size measured above.

### Measurements (this machine, Windows, JDK 21, 2026-10-30)

| Quantity | Value | Budget |
|---|---|---|
| Idle tick, flag on, no players, 0 active creatures, paced 34 Hz | 0.448 ms mean, 6.4 ms max, 0 over | <= 2.0 ms |
| Idle tick, same, 120 / 291 / 524 / 1,024 active creatures (no players) | 0.553 / 0.942 / 1.388 / 2.256 ms mean (about 1.8 microseconds per active creature); max 9 to 11 ms; 0 ticks over 29.4 ms | the 2.0 ms budget is the no-player case, where nothing is active |
| 20 players spread over the 20 densest separated fields, 180 s, in-process, paced | tick mean 1.52 ms, max 21.2 ms, 0 over; CPU 9.1% of one core; heap in use at peak 665 MB; active creatures peaked at the 1,024 cap (1,482 activated, 652 deactivated) | tick <= 5 ms, CPU <= 60% |
| `/state` JSON, creatures + players + window (Gson, before NPC and loot lists), starter town | 2,068 bytes, 9 creatures | |
| same, densest 97 x 97 window in world 0 | 20,188 bytes, 114 creatures (177 bytes each) | p95 <= 20 KB |
| same, projection for the 256-creature cap | 256 x 177 = 45 KB | <= 42 KB: 8% over the arithmetic; no real window in world 0 has more than 114 creatures |
| Startup, plan + creatures, 0 / 1 deviated rows | 1.2 s (cold JVM, includes loading the 19 MB collision grid) / 0.32 s (warm) | |
| Heap held by 7,038 dormant spawns | not separable from the grid; the dormant records are shared content |  |

### 30-minute native soak, flag on, 20 adapters spread over world 0

Server `--online worldmap.colbin 18835 --stream-world0` on the disposable cluster; twenty native `OnlineConnection`
adapters (no renderer), protocol 9, each placed by `SoakSpread` in a different one of the twenty densest separated
monster fields (10 to 40 creatures in its 32-tile cell), fighting what is near and otherwise holding its tile;
reconnects every 900 s. Report and server log are kept outside the repository (scratch), the numbers below are copied from them.

| Quantity | This run (30 min, 61,177 ticks) | T4C-0315 / 0353 baseline (2 h, temple + 8 portal walkers) | Budget (3.9) |
|---|---|---|---|
| Tick mean | 2.014 ms | 2.48 ms | <= 5 ms |
| Tick max | 26.9 ms | 35.9 ms | below 29.4 ms |
| Ticks over budget / failed / deadline resets | 0 / 0 / 0 | 1 of 244,928 / 0 / 0 | no worse than 1 in 2 h, 0 failed |
| Observed tick rate | 34.00004 Hz | 34 Hz | 34 Hz |
| Server CPU | 17.1% of one core | 36.7% | <= 60% |
| Heap in use, peak sample | 692 MiB (committed 1 GiB; 481 MiB at start) | 654 MiB | <= 1 GiB |
| Working set | not measured (only the JVM heap counters are in the report) | 842 to 879 MiB | <= 1.5 GB |
| Application responses | 3.31 GB, about 1.8 MB/s total, about 90 KB/s per adapter, 649,901 requests, 0 read or write failures | 2.44 MB/s, 122 KB/s per adapter | |
| `/health` mean | 0.72 ms, 0 failures | | |
| Database | 138,995 pooled operations, slowest 44 ms, 0 acquire timeouts, 0 deadlocks | 0 persistence failures | |
| Creature lifecycle (server lines, every 30 s) | about 790 live in steady state, peak 1,024 (the cap), 1,720 activated, 931 deactivated, 168 pristine rows deleted, 100 activation batches (slowest 6 ms), 0 failed activations, cap refusals 1,042 ticks (all in the first minutes; none after) | n/a | |
| Attacks | 557 sent, 264 accepted, 293 refused (cooldown, range, busy), 20 scheduled reconnects | | |
| Adapter guards | 1 guard tripped: 10 `position_not_walkable` on adapter 14 (x about 5845, see below); every other guard clean for all twenty adapters | | 0 |

**One open finding, not hidden by loosening the guard.** The adapter parked in the far east (x = 5,845, the
largest x of the twenty) reported 10 `position_not_walkable` out of 11,946 observations. Each position is
on a diagonal glide between two walkable tile centres, but the two axes disagree by 0.003 tile where the guard
(`PreviewMap.onGridPath`, `LATTICE_EPSILON`) allows 0.001. Movement runs in float world pixels (design section 11,
"Float precision"): at x = 5,845 a float pixel is 0.016 px, while the neighbours at x = 5,456 and 5,616 stayed clean. The positions are legal (never inside a wall
or off the glide by more than 0.003 tile); the fix, stepping in a viewer-relative frame, was already named in part 1
and is stage B work together with worlds 1 and 2 (world-2 coordinates are smaller). Until then a streamed far-east
walker can trip the T4C-0315 lattice guard roughly once per 1,000 observations.

### Deviations from the design text

* The unhostable list is larger than the design's 153 (see above): the controller's home rule is the player
  stand rule, so 37 foot-passable anchors and 386 sprite-less records are excluded as well. Hosting the 37
  needs the creature home check relaxed to the foot rule in `OnlineMonsterPursuit`; it is a separate, flag-off
  visible decision.
* A snapshot is filtered from the active set (at most 1,024), not looked up through the cell index; the cost
  is O(active), measured above.
* A dormant wounded creature resumes where it was last checkpointed (design 3.4); an unhurt one has nothing
  persisted and re-activates at its home. Patrol drift is therefore not preserved for unhurt creatures.
* The active cap admits in arrival order, nearest-first admission under the cap is not implemented: twenty
  players in the twenty densest fields reach the cap (1,024) and some far creatures wait (`cap-refusals`
  counts ticks blocked). The design's own worst case (20 hot spots) exceeds 1,024 by 3 to 4 times.
* The loot read is capped at 1,024 rows and reaching the cap is not yet surfaced in the snapshot `truncated`
  hint.
* The portal transfer path calls the same `requestArrival` as `join`, but only the join/ready path is
  exercised by a database test (no hosted portal leads into world 0 from a world-1 piece in the test plan
  without a long walk); `HostedTransferTest` still passes with the hook in place.
* In-process measurements use the pooled store; `StreamedCreaturesDatabaseTest` therefore needs
  `OnlineDatabasePool` because the unpooled test store exhausts the Windows ephemeral port range.

### Remains for stage B and later

Worlds 1 and 2 as streamed regions (and the world-2 audit, risk R6); nearest-first admission and eviction
under the active cap; surfacing the loot cap in `truncated`; a database test through a real portal transfer
into the streamed region; relaxing the creature home rule to the foot rule (37 more spawns); the 386
sprite-less records need data, not code; world 2 arrival latency; the unchanged items of section 4 stage A
that need a rendered client (walk from the temple to a far monster field, restart mid-chase).
