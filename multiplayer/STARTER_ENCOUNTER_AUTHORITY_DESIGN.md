# Starter encounter authority — T4C-0223

Status: architecture only, 2026-10-27 Asia/Jerusalem. Audited against main
`55de14ce999b6b7bd4d874496e941d2d39c17106`; moving-creature P2 was integrated by
PR157/main `d4dc908abe1c8082dac08d9da20deb1e825e6d59` (protocol5/schema11).
No implementation, export, build, database or native acceptance was performed here.

This closes the two specific design gaps blocking the unchanged authored starter
region: NPC-owned MONSTER records, and Goblin→Horse combat. Reuse
[HOSTED_STARTER_ROUTE_DESIGN.md](HOSTED_STARTER_ROUTE_DESIGN.md)'s audited
world0 `(2735,1029,249,117)` crop, its 13 Skeleton anchors, connectivity and
portal16/17 interior. Do not repeat the geometry project or expand to full-world hosting.
[MONSTER_PURSUIT_DESIGN.md](MONSTER_PURSUIT_DESIGN.md) remains the source for
ordinary pursuit rules; its P2 implementation now exists and must be extended coherently.

The approved checkpoint is Warrior + Paladin, Paladin mace/shield, Light Heal and
potions; Aelric's `renewed_wards` remains 12 qualifying Skeleton kills, 500 gold,
400 XP and a durable Renew Armor grant (casting that reward excluded). Mage/Archer
creation-kit amendments do not add those classes to checkpoint online combat.
The owner subsequently changed Beta/GA concurrency to two real rendered clients;
all other gameplay, content, recovery, security and two-hour mixed-stability gates remain.
This design neither completes the checkpoint nor changes the full Beta scope.

## Source findings and decisions

| Current source | Consequence |
| --- | --- |
| `monster/core/MonsterManager.isNpcOwnedSpawn` excludes case-insensitive SUNDIAL or `NpcFactoryRegistry.find(type) != null`; `NPCManager.isNpcType` uses the same decision. | Ownership is independent of a MONSTER annotation and must be resolved before MonsterDef lookup. |
| `NpcFactoryRegistry` resolves ID/class/Spawn aliases; `Sundial` carries MONSTER annotations, `SPRITE_BASE = "@static:Horloge Solaire"`; NPCManager forces SUNDIAL stationary. | Preserve the record at (2881,1070) as an NPC; its hostile-looking annotation does not authorize combat. |
| `OnlineMonsterContentExporter.snapshot` presently maps every `SpawnRegistry.monsters()` record through MonsterRegistry; MonsterContent v1 allows null definitions, and PreviewWorld rejects unsupported membership. | Classify explicitly at export, retain unresolved ordinary monsters as errors at hosted membership. An ignore-null startup catch is incorrect. |
| `HostedWorlds` explicitly throws `Unsupported clan combat in hosted region` when both Goblin and Horse are present. | Fixing Sundial alone still cannot enable this crop. Remove this guard only after the complete durable clan path is integrated and tested. |
| `MonsterClanRelations.resolveClan/defaultRelations` classify Goblin/Horse and declare only directional GOBLIN→HORSE by default. | Export registry-derived clan metadata and relations; do not keep HostedWorlds' name-substring guard as the authority. Horse does not become hostile in reverse. |
| `BaseMonster.findNearestEnemyMonster`, `canAttackClanTarget`, `aggroOnMonster`, `updateMonsterCombat` | A living aggressive, nonstationary actor with an attack animation may acquire a living enemy within MONSTER_AGGRO_RANGE and LOS. When idle, an enemy creature has priority over natural player acquisition; an existing valid fight is retained. |
| `BaseMonster.applyPlayerDamage/aggroOn` | A committed player hit supersedes the monster target and enters player RETALIATION; preserve that priority. |
| `BaseMonster.applyClanDamage` | A struck defender retaliates only if its own aggressive/nonstationary/attack capability and directional enemy relation permit. Horse has `canAttack=false`; it can be damaged and killed but must not invent an attack or flight behavior. |
| `BaseMonster.performAttack(BaseMonster)`, `CombatProfiles.fromMonster`, `CombatResolver` | Resolve authored physical dice through the shared physical resolver for both monster profiles, including random armor/hit/dodge. Monster profiles currently have empty skill maps, so stun_blow yields zero stun; do not invent stun state for this slice or permit future nonzero status effects silently. |
| `MonsterManager.notifyKilledByPlayer` and `MainGameScreen.configureMonsterDamageCallback` | Loot and quest callbacks are player-kill notifications, separate from general death scripts. Clan kills supply no player XP/gold/drop/killlog/quest credit, including a previously player-wounded Horse. Generic scripted deaths remain unsupported. |
| `OnlineStore.SpawnState`, `respawn` and PersistentCreatures | Schema11 derives death/respawn from player `online_world_rewards` recipient/time. No-player deaths need an independent terminal-life record; health0 with recipientnull alone is insufficient. |

The T4C-0214 census remains the source-audited baseline: 39 MONSTER-export records
in the crop, 38 ordinary creatures (19 guards, 13 Skeletons, four Goblins, one Orc,
one Horse) and one NPC-owned Sundial. The separate NPC annotation audit found 29
NPC records including Sundial, Aelric, Kilhiam and 26 other NPCs. Counts are diagnostic
facts, not immutable game rules. Regenerate/reconcile from the exact implementation
candidate; a missing/unclassified record is an error, not an allowed count difference.

## Export and NPC ownership contract

Extract the duplicated offline ownership decision into one build-time registry helper
(e.g. `npc/core/NpcSpawnOwnership`), used by MonsterManager, NPCManager and the
exporter. Keep desktop reflection/factory creation out of the headless server. Preserve
case/alias normalization, explicit SUNDIAL compatibility and offline spawn behavior.
Do not resolve an NPC-owned identity as a MonsterDef merely because one also exists.
Detect conflicting NPC aliases used by an exported spawn: NpcFactoryRegistry's
`putIfAbsent` currently hides alias collisions, so export must reject ambiguity rather
than choose classpath scan order. Do not broaden scope to unrelated registry refactors.

Bump monster content to v2 with explicit spawn ownership (`MONSTER | NPC`) and,
for NPC records, canonical NPC identity. Retain every original type/world/anchor/flags
and persistenceKey. NPC ownership has no combat definition; MONSTER ownership must
resolve its definition before a region becomes playable. Export canonical clan identity,
attack-animation capability and sorted directional relations from the offline registry
for ordinary definitions; validate referenced clans and capable physical attacks. Do not
infer factions from runtime display-name substrings or load desktop assets on the server.
A global unsupported custom MONSTER may remain diagnostic, but hosting it is fatal.

Keep one generated ownership/appearance manifest for all authored NPC spawn records,
with canonical NPC ID, spawn identity, location, sprite/parts and online interaction
support status. Deduplicate the Sundial MONSTER/NPC discovery paths by stable spawn
identity; retain multiple genuine spawns of the same NPC ID. Make completeness and
export freshness tests compare the source registry to this manifest. An unavailable
appearance must have an explicit diagnostic and must block claiming that actor rendered;
it cannot disappear from the exported inventory.

Separate appearances from the strict interactive catalog. `NpcInteractionCatalog.SUPPORTED`
stays Kilhiam/Aelric. `RegionNpcs` may publish additional NPC appearances without
registering their scripts, shops, doors, Dragon or Sundial clock interaction as supported.
Reuse NpcView's appearance fields; distinguish canonical interaction ID from stable
spawn-view identity, since NpcInteractionContent v1 assumes unique IDs and cannot
represent repeated Sundials. Unsupported interactions return one bounded English refusal
through the existing interaction path. Keep their exclusions explicit in candidate scope
and acceptance; no placeholder quest giver or fake portal. Any additional wire field or
static-sprite renderer support needs coordinated native ownership and actual verification.
This work does not authorize disabling an existing authored NPC behavior offline.

## Runtime target and transaction contract

Extend the existing region simulation and PersistentCreatures owner, not a second cache
or background thread per actor. Use a discriminated target:
`None | Character(characterUUID, locationEpoch) | Creature(spawnKey, lifeUUID)`.
Local entity IDs are snapshot handles only. Add `CLAN` mode; retain existing player
NATURAL/RETALIATION and IDLE/PATROL/RETURN meanings. A target reference always names
the current life; a respawn at the same tile cannot inherit the old attack.

On a tick, drain committed changes first. Choose the nearest valid enemy creature at
current pixel-space distance with LOS, then stable persistenceKey tie-break (explicit
multiplayer arbitration replacing offline list-order ties). Use the shared natural leash
and authored attack reach. Retain a valid current target; do not let a nearby Horse steal
an ongoing player retaliation or natural player fight. When idle, clan acquisition precedes
natural player acquisition as offline does. A committed player hit retargets immediately.
A dead/wrong-life/wrong-region/nonenemy target clears and returns without healing or a
new life. No cross-region chase, catch-up attack burst or client-authored target selection.

Reuse P2 collision-valid motion, pixel speed conversion, LOS, bounded fair global path
budget, reservation/freeze, five-second coalesced checkpoints and cache-generation fences.
Offline monster combat has no player-combat unreachable timeout; do not accidentally add
that timeout by routing CLAN through the player branch. Failed paths stop/retry under
the existing budget and reset on invalid life/leash. Keep player sanctuary rules unchanged;
offline clan methods do not add a new player-sanctuary restriction to creature combat.
The current candidate physical subset has no skills/spells/scripts; refuse unsupported
attacks/status effects rather than changing their authored effects.

Implement one trusted clan-attack draft capturing BOTH actors' spawn key, life, state
revision, current absolute pose, source target reference, expected cooldown and resolved
result. Reserve/freeze both entries in sorted spawnKey order; if either is pending,
recovering/reserved, defer without rolling or consuming cooldown. Earlier checkpoints
finish before reservation. Resolve the RNG once, never reroll a draft after SQL uncertainty.

Store transaction order is: all required character rows in UUID order, then all touched
spawn rows in persistenceKey order. A clan-only attack locks no character rows and never
later acquires them. Existing player transactions keep character-before-spawn; acquire the
full deterministic spawn lock set before any spawn mutation. Include target-clears on
character death/transfer and future multi-spawn actions in that same ordering. No transaction
may lock source then target in encounter direction. Thus simultaneous reciprocal/overlapping
attacks and player kills cannot form opposite spawn-lock cycles.

Under both row locks, verify alive current lives, revisions, same region/current support,
CLAN target identity, source cooldown and authoritative captured geometry. Persist source
cooldown/pose/revision and target damage/pose/revision atomically. A miss still commits the
source cooldown. A lethal result marks the target terminal and clears its target/deadline;
never publish a moving living target with committed zero health. Other actors targeting
that life invalidate on the next drained state; their transactions fail the target-live CAS.
Do not scan/lock every pursuer in the lethal transaction. Character death/transfer clears
only Character targets; a similarly shaped creature life UUID must not be mistaken for one.

CAS on both preattack revisions/lives gives at-most-once application of the immutable draft.
On lost acknowledgement or SQL ambiguity, freeze/reload BOTH entries; do not retry with new
expected revisions, regenerate damage or assume rollback. A worker completion updates neither
cache until the whole committed pair is available and both generation fences match. Discard
stale completion publication and reload both when either fence changed. No HTTP mutation is
added: this is server-generated simulation work on bounded persistence workers. Independent
players/regions continue ticking while the pair waits.

## Durable death and schema compatibility

Use the next coordinated schema version (candidate **12**, not reserved by this design;
reconcile Claude's latest migration before coding). Add `died_at` and `death_kind`
(`PLAYER | CLAN`, null while alive) to online_spawns, plus nullable creature-target key/life
and CLAN mode. Constraints enforce paired creature key/life, one target kind at a time,
mode/target consistency and no target/cooldown on terminal state. A creature target references
an existing spawn identity, while its life UUID is validated by transaction CAS; it must not
be a foreign key that prevents the target advancing to a new life.

Backfill old rewarded spawn lives' died_at from their existing reward claimed_at and
kind PLAYER. Preserve generation, event UUID, wounded health, current pose, cooldown,
player target/epoch and state revision. Alive schema11 rows remain alive; never initialize
a new life or refill HP as migration. Fail explicitly on inconsistent legacy death data.
Existing online_world_rewards remains the unique PLAYER reward event/recipient ledger;
CLAN death does not insert a fake character, nullable recipient reward, loot source or quest
claim. Change every creature alive/respawn predicate/cache check to terminal state rather
than `recipient != null` (including checkpoints, attack admission, snapshots and delayed reads).

Player lethal transactions atomically set terminal metadata AND their existing reward,
drop, quest and progression writes. Only the successful player lethal claimant receives
kill credit; an earlier wound supplies only its already committed per-hit XP, not a future
claim on a clan death. Keep the existing kill-credit hook's captured absolute current death
tile for Skeleton's radius/world objective, never the anchor or player location. No party
or shared-kill rule is introduced.

CLAN lethal transactions set terminal metadata with no reward. Respawn eligibility for BOTH
kinds uses died_at and the authored delay, guarded by expected dead life/revision. Respawn
alone increments generation, allocates the new life event UUID, restores anchor/full HP and
clears terminal/target/cooldown/navigation. Reward receipts for old player lives remain intact.
A Horse killed during a race with a player therefore dies once, has either a real player
recipient or no recipient according to the winning lethal transaction, and respawns once.

Persist clan target transitions promptly, and include creature-target identity in each motion
checkpoint/CAS. Crash may rewind uncommitted travel by the existing checkpoint allowance;
it cannot forget committed damage/death, shorten cooldown or resurrect a terminal life.
Graceful shutdown drains pair mutations and dirty checkpoints and fails explicitly if not
fully drained. Current P2 shutdown limits and one-server-per-database deployment remain.
Schema12 cannot safely run with schema11 binaries; use the normal stopped-server migration,
matching package and backup procedure, never an in-place old/new mixed-server rollout.

## Minimal implementation sequence and ownership

These are implementation phases, not allocated task IDs or active file grants. Route coding
to gpt-6-sol. Recheck canonical PEER_WORK and obtain exact latest-state handoffs before each
phase; this design grants no ownership of Claude's core/native files or T4C-0222 HTTP files.

| Phase | Cohesive delivery and exact likely ownership | Gate before enabling |
| --- | --- | --- |
| A: classified content | New shared offline ownership helper; MonsterManager/NPCManager delegation; MonsterContent/exporter and generated content; NPC ownership/appearance manifest; exporter/catalog/registry tests. Include necessary domain pom includes only with a separate grant. | Every source record accounted for, known NPC-owned and unknown ordinary paths distinguished, aliases deterministic, export fresh. Keep old hosted region default. |
| B: durable clan encounter | OnlineStore migration/records/transaction tests, PersistentCreatures and OnlineMonsterPursuit/tests, OnlineRetaliation or one new OnlineClanCombat driver, HostedWorlds wiring/test. Coordinate OnlineMelee/player lethal hooks and session death/transfer lock sets. | Two-life CAS, independent terminal state, reward separation, restart/respawn and checkpoint publication tests pass together. Do not remove the Goblin+Horse guard in a helper-only groundwork PR. |
| C: integrated starter hosting | HostedRegionPlan, PreviewWorld membership, RegionNpcs appearance/unsupported feedback, focused real-map route test; portal metadata/refusal handling in PersistentSessions/native/HTTP only after their owners release scope. | A+B and actual snapshot limits/appearance readiness, approved quest/reward/Light Heal integration, explicit unavailable sources12/13/14/947. Then enable the audited crop, keeping16/17 return. |
| D: candidate verification | Corrected existing NativeLootProbe/quest route tooling and documentation under Claude handoff, exact packaged client/server/content/protocol/schema manifest. Root updates TASKS/MILESTONES/ACCEPTANCE/HANDOFF together. | Two rendered clients and remaining milestone criteria; service/native-adapter passes are labeled as such. |

No map-size protocol change is needed. CLAN targets need not be public; current CreatureView
can show HP/pose/death without revealing persistence keys. Life-aware melee intent and native
attack/death/motion presentation remain the pending coordinated wire work from P2 design;
do not infer those contracts are solved by a private DB life field. Batch any necessary NPC
spawn identity and current-life intent changes into one validated protocol revision with the
native/HTTP owners, without borrowing T4C-0222's in-progress PreviewServer ownership.

## Focused verification and remaining evidence

Extend existing focused tests first: MonsterKindNpcSpawnLookupTest,
OnlineMonsterContentExporterTest, MonsterCatalogTest, OnlineMonsterPursuitTest,
PersistentCreaturesTest, OnlineStoreTest, OnlineMeleeTest, OnlineRetaliationTest,
HostedTransferTest and MonsterManagerPlayerKillNotificationTest. Avoid enumerating every
spawn or clan when one representative path proves the contract.

1. Real exported Sundial is NPC-owned with correct canonical/static appearance and retained
   metadata; an unknown ordinary MONSTER in the same crop fails startup. Check registry alias
   conflict and export freshness, plus full source-record reconciliation.
2. Goblin acquires Horse before an idle natural player candidate; Horse does not attack;
   a player hit changes Goblin to player retaliation. Existing valid target retention, directed
   hostility and target-life invalidation survive checkpoint/restart. Verify physical resolver
   equivalence with a fixed RNG, not a second invented damage formula.
3. PostgreSQL pair attack commits both states/cooldown once. Race player lethal damage against
   clan lethal damage and race two sources against one Horse: one terminal life, correct single
   player reward or none, no fabricated loot/quest credit, no deadlock. Force precommit rollback
   and postcommit lost acknowledgement; reload recovers the same result without a second roll.
4. Migrate an actual schema11 wounded/moving player-target life and an already rewarded dead
   life; preserve their state/delays. Kill Horse by clan, restart, then respawn at its anchor/new
   life; stale drafts/checkpoints cannot harm the new life. Verify terminal creature filters and
   graceful pair-drain failure as consequential joins, not every scalar field in separate tests.
5. Construct the actual249×117 candidate with all classified records and active clan authority;
   reuse T4C-0214 real-map route test. Confirm world/local-origin conversion and saved global
   position, actual serialized body/actor caps, unavailable routes and16/17 round trip.

On matching committed packages/content/schema, run an isolated native-adapter quest/loot route
using direction-only movement from the temple, Aelric, the authored Skeleton hunting area and
back. Do not relocate characters/objectives to avoid pathing or force randomized loot. Verify
12 current-death-location qualifying kills, independent two-player progression, exact gold/XP/
Renew Armor grant, inventory/full/claim races, Light Heal/potions and restart/rejoin. Observe
actual Goblin→Horse behavior separately if its authored patrols do not naturally meet during
the bounded quest attempt; a deterministic service fixture proves transaction behavior but
cannot be relabeled an observed authored encounter.

Then two real rendered clients must walk that complete route, see shared actor motion/lives,
unsupported NPC/route refusals, combat/death/loot and their own durable rewards; return16/17,
die/recover, disconnect/rejoin and restart the matching server. Record exact hashes, observed
results, failures and limitations. Offline launch/gameplay/existing saves/preferences remain
required. Remote/deployed, clean-machine, abuse/recovery and two-hour mixed Beta gates remain
open until separately proven. Historical20-client scale evidence remains historical/follow-up.

Unknowns to resolve during implementation: complete NPC appearance readiness on the enlarged
crop; exact next schema/protocol after Claude's current work; the actual native route/combat
survival and real randomized-loot duration; measured candidate tick/queue/DB/snapshot load.
None is evidence to omit a spawn, weaken rewards, invent a portal or declare a milestone done.
