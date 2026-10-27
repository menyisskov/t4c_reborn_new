# Authoritative monster pursuit — T4C-0209

Design checkpoint, 2026-10-27, Asia/Jerusalem. **Architecture only; no pursuit implementation,
runtime verification, content/class approval or milestone completion is claimed.**
[MILESTONES.md](MILESTONES.md) remains the release checklist. [SCOPE_PROPOSAL.md](SCOPE_PROPOSAL.md)
and its exclusions remain proposals. This document does not approve staged visuals or replace
the full 20-player Beta.

## Source and ownership boundary

Investigation started at local `34144115` (PR131); fetched `origin/main`
`6d8305fbf570e93d7aa58319eb21bfe86d06d23a` includes PR132's quest/spell exports. During the
read-only investigation the checkout integrator advanced local HEAD to `ccb1b42a`; that commit
only reconciles coordination/task records. No branch/index operation was performed by this task.
Its claim-publication PR133 subsequently merged as main `5f6d3221`; this changes no inspected runtime
source. The acknowledgement's initial branch label was historical; the integrator controls branches.
The inspected melee/loot implementation is from `952320d6` (PR130). Relevant tracked content blobs:

| Content | Git blob |
| --- | --- |
| `server/src/main/resources/content/monsters-v1.json` | `eea2094340949f2c1f4f0d3e25dad9820e58cb7d` |
| `server/src/main/resources/content/quests-v1.json` | `47ccc467977213a9c430808b1abf689192d5da99` |
| `server/src/main/resources/content/spells-v1.json` | `cb58de37080b002e226f7e63bf93c1dbbbc8ec27` |

Paths beginning `server/` below are relative to `multiplayer/`; game sources begin
`src/main/java/com/perso/T4C/`. The source baseline has monster content v1, protocol 4 and
storage schema 10; it is distinct from the live source-`8da43482`/schema-9 QA server and clients.
Do not infer runtime evidence from these source versions. Recheck the latest protocol/schema and
peer claims before coding; reserve the next schema version, rather than reusing Claude's v10.

The canonical register is `C:/Users/menyi/repos/t4c_reborn_new/multiplayer/PEER_WORK.md`.
Codex `pursuit_design` owns only this new document under the root's T4C-0209 reservation.
Claude owns the additive loot and quest/ability lane (0204/0206), including the relevant store,
session, hosted-world and melee interfaces. Codex 0208 owns its isolated database-recovery tools.
No implementation, build, database, server-port or desktop window is claimed here. The root
integrator maintains TASKS, CHANGELOG, MILESTONES, ACCEPTANCE and HANDOFF together for delivery.

## What the offline game actually does

The authority is `monster/core/BaseMonster.java`, with spawn application in
`MonsterManager.instantiateSpawn`, `DataMonster`, `MonsterDef.Attack`, `config/GameConstants.java`,
`helper/Pathfinding.java`, `helper/CollisionManager.java` and `helper/SanctuaryCombatRules.java`.
These are behavior facts to preserve, not newly selected balance values:

| Situation | Existing behavior |
| --- | --- |
| Stationary spawn | Clears targets/navigation and never pursues. `canBeAttackedByPlayer()` returns false. Stationary is an authored protection flag, distinct from today's stationary network preview. |
| Natural player acquisition | Spawn aggression overrides the definition default (`aggro > 0`). Requires distance at most `MONSTER_AGGRO_RANGE` and LOS, with neither endpoint in sanctuary. Aggro metadata is not a percentage roll or a strength/level comparison. |
| Movement | `MONSTER_SPEED`, currently 45 pixels/s, normalized in pixel space. `GRID_W=32`, `GRID_H=16`. Definition `speed` is not used by this base movement path. Do not divide both axes by GRID_W or add a new balance multiplier. |
| Natural chase leash | Current monster-to-target pixel distance at most `MONSTER_AGGRO_LEASH_RANGE` (30 × GRID_W), **not** distance from spawn. |
| Retaliation | `aggroOn` switches to the attacking player and marks retaliation, including on passive nonstationary actors. Retaliation leash is 60 × GRID_W. The player damage entry point invokes it before applying damage. Sanctuary still prevents it. |
| Reach/attack | `resolveAttackRange()` is maximum authored attack range × max(GRID_W, GRID_H), minimum one tile. Physical attacks have maximum one tile; eligibility also uses `round(pixelDistance/maxGrid)` and `Attack.isInRange`. The 1.5-second base cooldown follows the attempted attack. |
| Lost sight | Existing aggro does not reset just because LOS is lost. It pursues a path and cannot strike without LOS. Natural acquisition requires LOS. |
| No usable player path / blocked chase | Accumulates 0.75 seconds of unsuccessful chase, then drops aggro and chooses the spawn as patrol target. Unavailable-path retry is 0.75 seconds; blocked repath is throttled at 0.5 seconds after six blocked attempts. Successful chase clears the unreachable timer. |
| Reset/home | Clears aggro reason/targets, attack timer/pose and navigation, then **walks** to initial position. Does not heal, resurrect, teleport or create a new life. Normal patrol may resume; a new eligible target can interrupt return. |
| Patrol | Random candidate around initial position, radius `MONSTER_PATROL_RADIUS` (6 × GRID_W), up to 12 attempts. Pause follows existing min/max constants; no new seconds-long idle delay. |
| Death/respawn | Respawn restores full health/mana and initial position and clears target/timers/navigation. The online equivalent must retain its already durable life-generation and reward rules. |

Natural radius is currently 8 × GRID_W. Hunting-ground anchors are an existing exception:
`HuntingGrounds.at(canonicalName, spawnTile)` changes natural acquisition to 12 × GRID_W and
requires the player within the ground's authored Chebyshev leash about the field centre.
Neither of the current two hosted crops requires those fields. Export that metadata before
hosting them; do not load desktop-relative hunting files from the server or silently apply a
generic leash there.

Offline navigation uses bounded eight-neighbour A*, one-tile static clearance except at the goal,
diagonal side checks and tile-centre waypoints; actual movement checks `CollisionManager.hasCollision`,
which can include the runtime dynamic provider. It also has short obstacle fallback movement.
Offline LOS samples raw collision along the ray. The server already intentionally uses a stricter
supercover LOS and must keep it: corner walls and code-9 sight blockers never become permission
to attack. Do not replace server LOS with a desktop singleton or relax it to movement's wall-foot
exception. A shared path search needs an injected terrain predicate, not desktop initialization.

Bound the aggregate path work as well as each search. Initially admit at most one existing-limit
A* solve per global simulation tick across regions, with a fair pending queue; reuse valid paths
between solves. The existing search caps expansions at 8,000 and checks a 16-ms time ceiling.
Queued navigation cannot hold a creature transaction reservation, and a superseded target/life
invalidates its queued request. Record path budget/debt during the integrated load run before
claiming the larger hosted region stable; a per-creature bound multiplied by all actors is inadequate.

Offline clan targeting runs before natural player acquisition: nearest visible enemy within the
natural radius, only for aggressive, nonstationary actors with an attack animation. The current
default relation is **Goblin → Horse** (`MonsterClanRelations`); it is directional. Custom companions,
monster spells and scripted lifecycle effects are additional behavior, not supplied by player pursuit.

## Present server gap and affected joins

`PreviewWorld` constructs one immutable `CreatureView` per authored spawn and changes only health
and visibility. IDs are local to a region; they are not durable life identities. `PersistentCreatures`
loads/respawns `OnlineStore.SpawnState`, reserves a creature during combat and hides uncertain kills.
Saved state currently contains spawn key, generation/event UUID, recipient/claimed time, health,
target-character UUID and next-attack deadline. It contains **no current position, aggro reason,
target region epoch, mode or state revision**.

`OnlineMelee` and `OnlineRetaliation` both calculate range from `spawn.spawn().x/y`. `OnlineMelee`
also rolls ground loot at that spawn tile. Future quest kill-credit integration in
[QUEST_ABILITY_DESIGN.md](QUEST_ABILITY_DESIGN.md) explicitly requires the monster's current death
tile; leaving any of these consumers on the anchor creates an attack/loot/quest mismatch.
The immutable anchor remains the persistence identity and home location after movement starts.

`OnlineRetaliation` currently has no acquisition or pursuit, and retains a durable provoked target
while it is absent. It uses `MONSTER_ATTACK_RANGE` (two × GRID_W) plus a rounded physical range
guard. That preview guard is not the authored pursuit stopping distance: moving actors must use
the shared `resolveAttackRange`/eligible-attack rules above. Reusing two tiles would stop physical
monsters early. Only ordinary dice attacks are supported; monster spells are refused.

`HostedWorlds.step` moves players, drains sessions, refreshes creatures and runs retaliation. Session
bindings already track lease deadline, revision, region and location epoch, and freeze during
transfer/death until readiness. `tokenForCharacter` excludes closing/expired/absent bindings;
`belongs` alone does not prove readiness. Attack commits use `lockSession`, current life/target/
deadline CAS and receipts. Death and transfer clear persisted aggro for that character, and
`reloadTargets` invalidates pending cache reads. Preserve that delayed-read protection.

Native `OnlineConnection` already validates in-region creature coordinates; `OnlineClient` applies
their coordinates to sprites. Its creature rendering presently supplies fixed facing/movement
state, so coordinate motion alone is not a complete walking/attack presentation. Representation
of moving/facing/attack/life identity needs a coordinated model, protocol and adapter change.

## Minimal cohesive authority model

Keep one authoritative server process and its simulation owner. Extend `PersistentCreatures` as
the single creature-state owner; add one region-local pursuit driver, not a second world/cache or
one worker per creature. All blocking storage stays on bounded workers. The driver receives
server-authored eligible player views and terrain access and returns decisions to this owner.

Use one current creature state for **all** snapshots, movement, melee, retaliation, loot and quest
inputs. It consists of local fractional tile position, home anchor, life UUID, state revision,
`PATROL | NATURAL | RETALIATION | RETURN` mode, target UUID/target location epoch when applicable,
next-attack UTC deadline, and transient path/patrol/unreachable timers. Stationary/dead/unavailable
states are existing guards, not alternative moving lives. Never rewrite the spawn persistence key
when a creature walks.

1. On each fixed simulation step, drain committed state and session removals/transfers first, then
   evaluate creature target validity, movement and due attacks. A creature reserved for a write,
   reload or combat cannot accept a competing mutation. Publish one consistent region snapshot.
2. Natural player choice is the nearest eligible player in authored range, with stable character
   UUID tie-break. Keep the current target while valid; do not select anew every tick. This tie-break
   is a multiplayer arbitration rule, not an offline balance gate. A committed player provocation
   overrides natural/previous player target as offline `aggroOn` does.
3. Eligibility requires an active unexpired binding in the creature's region, alive supported state,
   ready for that exact location epoch, no transfer/death pending, and no sanctuary at either
   endpoint. Exclude unsupported players from proactive attacks just as the existing loadout path
   refuses them; never provoke an encounter that the character cannot legally finish.
4. Continue a valid chase without LOS; attempt attacks only at authored reach, eligible physical
   attack range and current server LOS/sanctuary. Movement follows a collision-valid path using
   pixel-space speed converted separately through GRID_W/GRID_H. Sweep bounded substeps and reject
   diagonal cuts; a path is advisory, never permission to cross a blocked cell or region boundary.
5. Leash/sanctuary/wrong-epoch/death invalidation clears the target/reason/deadline and initiates
   collision-valid RETURN. No healing on reset. An unreachable return holds safely and retries;
   it does not teleport through a wall. Reacquisition can interrupt RETURN as offline does.
6. Preserve the existing reconnect contract: an absent/expired/disconnected target receives no
   attack and supplies no stale position. Suspend chasing it, retaining target UUID/epoch/reason
   and deadline durably. On rejoin, rebind only to a valid fresh lease in the same epoch/region and
   re-evaluate leash/sanctuary; missed time never grants burst attacks. Another player's committed
   hit can retarget the creature meanwhile. Suspension does not make the creature invulnerable.
   This online mapping is explicit because offline has no disconnected target; it preserves
   `OnlineRetaliationTest` rather than silently changing its persistent-aggro guarantee.

Do not add “attack only weaker characters”, random aggression probability, a spawn-centred ordinary
leash, full heal on evade, or cross-region pursuit. None follows from the inspected behavior.

## Durability and concurrency contract

Add nullable current absolute position (world is the immutable spawn's world), aggro mode/reason,
target epoch and a monotonically increasing creature-state revision to `online_spawns`. Paired
coordinates must be finite and both present or both absent. Null migrated coordinates mean the
authored anchor; do not initialize a fresh life or heal old wounded health. An existing non-null
target migrates as RETALIATION (all old targets came from provocation); obtain/validate its epoch
from authoritative character state before permitting an attack. No target credentials or tokens
belong in public snapshots.

Distinguish a live movement view from its last durable checkpoint, as player movement already does
(current player checkpoint interval is five seconds). Coalesce dirty creature position/navigation
checkpoints at a bounded cadence; use the existing five-second cadence initially, not a new DB
write every 34-Hz tick. Persist meaningful target/reset transitions promptly. A forced process kill
may rewind uncommitted travel to the last position checkpoint; it may **not** heal committed damage,
forget a committed kill/reward, shorten a committed cooldown, or restore a cleared epoch/target.
Graceful shutdown flushes accepted dirty creature state before reporting success; current
`PersistentCreatures.close()` only stops its read/respawn worker and is insufficient for that change.

The checkpoint writes one immutable captured state with expected life UUID + state revision CAS,
then advances that revision. Keep only one write in flight per creature; coalesce newer dirty motion
without letting its completion overwrite a newer simulation position. On SQL ambiguity stop that
creature's gameplay and reload committed state; do not automatically replay an uncertain mutation.
Old queued work must carry a cache-generation fence in addition to DB revision so a pre-clear read
cannot resurrect a target locally. Existing death/read invalidation is the pattern to extend.

Combat must not act against an old spatial checkpoint:

- Reserve/freeze the creature before preparing a melee or monster-attack draft. Capture its current
  position, life, target/reason/epoch, spatial revision and the player's current region/epoch in that
  immutable draft. Let any earlier creature checkpoint finish before accepting the reservation;
  `life()` already refuses pending/recovering entries. No spatial write races the combat transaction.
- The character worker transaction keeps **character lock before spawn lock**, verifies the current
  character lease/revision/location epoch and creature life/state revision, and persists the captured
  creature position/AI changes together with damage/cooldown/reward/drop/quest state. Extend the
  fingerprint with this authoritative spatial payload. A mismatching replay must conflict.
- Player movement after an immediate-impact attack was accepted does not retroactively cancel it.
  The accepted server position is the impact point; a concurrent committed transfer/death/lease
  replacement is fenced by its character row/epoch and single-flight queue. Never reroll the draft.
- A creature-only acquisition targeting a player also validates that player's expected lease and
  location epoch under the same lock order before setting aggro. Reset/checkpoint without a target
  uses only life/state CAS. Multi-creature target clears on fatal death/transfer must invalidate all
  their state revisions and caches and acquire spawn rows in deterministic key order.
- A lethal commit leaves no moving/attacking living state. Its drop and quest location is the same
  captured **absolute current death position**, never the anchor. Death reward deduplication remains
  the life event UUID. Respawn alone advances generation/event UUID, returns to anchor/full health
  and clears target/mode/cooldown/navigation/current-life motion in one guarded store transition.

One server process per database/hosted world remains an operational requirement. A row revision is
not a distributed region lease; do not advertise multiple authoritative servers on the same spawns.
Player target checks must nevertheless fence replaced/expired character leases. Public local entity
IDs require current region epoch and life identity at command admission; extending melee to include
the advertised life prevents a delayed same-region old-life intent hitting a respawned actor. Bump
the protocol once with the peer's pending loot/quest changes, rather than publishing mixed contracts.

## Supported content and guards

The current two hosted regions are world 0 `(2904,1029,80,60)` and world 1
`(264,353,80,60)`, with only portals 16/17 enabled. The proposed Skeleton quest area is not hosted.
Do not extend that rectangle, relocate objectives or accept `SUNDIAL` as a generic monster here.

Candidate ordinary physical behavior in the existing monster export:

| Definition | Authored default / attacks | Pursuit consequence |
| --- | --- | --- |
| Skeleton | Aggro 100, physical `1d7+3` | Natural player acquisition and pursuit; required by the proposed unchanged quest. |
| Goblin | Aggro 40, physical `1d8+4` | Natural player acquisition plus authored Goblin→Horse hostility. |
| Wandering Orc | Aggro 40, physical `1d11+6` | Natural player acquisition and pursuit. |
| Olin Haad Guard 3 | Aggro -100, physical `1d5+3` | Passive unless authored spawn override; player-hit retaliation. |
| Wild Horse | Aggro -100, `canAttack=false` | Do not invent attacks; nonstationary patrol and Goblin target eligibility remain authored behavior. |

All five have no source-event scripts in this export; spawn flags still take precedence. Do not
interpret their aggression numbers as roll odds or their stored speed 0 as immobility. A first
Skeleton/guard player-pursuit slice can be implemented without clan combat, but it cannot claim
complete behavior for an expanded region containing Goblin and Horse. That next region needs
clan target identity/health/death transactions and a declared consequence for non-player kills
(no fabricated player reward), or an explicit accepted exclusion recorded in MILESTONES. Never
silently disable the relation or call player-only pursuit full authored AI.

Retain all existing unsupported guards: stationary protection; source-event scripts; unsupported
loot references/unique items (`OnlineLootDrafts.supports`); unsupported monster attacks/spells;
companions, unsupported stun blow (skill `stun_blow`), unsupported active buffs/effects and
quest/story flags. Claude's proposed catalog-aware quest flag allowlist must replace the narrow
letter-only guard in its own coordinated phase. Do not remove that guard just to get aggro working.
Proactive AI must share the same creature/character support decision as attack admission; a scripted
or unkillable unsupported actor must not begin a normal physical fight with players.

## Implementation boundaries and handoffs

These are phases, not new task IDs or active claims. Route coding to `gpt-6-sol` per AGENTS section 6.
No core phase starts until the canonical register records the current owner's explicit handoff and
the exact latest source state. Do not run a helper-only parallel lane that nobody consumes.

| Phase | Small cohesive result / exact likely files | Prerequisite |
| --- | --- | --- |
| P1: shared authored decisions | Add one pure `monster/core/MonsterPursuitRules.java`; make `BaseMonster` delegate scalar acquisition/leash/reach decisions; include it in `multiplayer/domain/pom.xml` and add one focused rules test. Preserve offline behavior. No store/server changes. | Root confirms those files unowned, records exact scope and build window. |
| P2: one moving durable physical slice | Extend `OnlineStore`, `PersistentCreatures`, `PreviewWorld`; add one `OnlineMonsterPursuit` driver; wire `HostedWorlds`; use current position in `OnlineMelee`/`OnlineRetaliation` and lethal loot/quest drafts; add readiness/epoch target lookup in `PersistentSessions`. Implement movement/state CAS/atomic spatial combat/shutdown together. | Explicit Claude0204/0206 handoff for store/session/hosted/melee and direct tests; reserve next schema; reconcile pending quest location/regen hooks before editing. |
| P2 navigation within that slice | Reuse existing bounded A* by extracting its pure engine to one `helper/GridPathfinding.java` with injected static collision/clearance; retain `Pathfinding` as the offline adapter. Add domain include and server `PreviewMap` terrain/step adapter. Use path budget/retry limits; no desktop singleton, collision-file access or unbounded per-tick search. | Claim `Pathfinding`, new engine, domain pom and PreviewMap with P2. If dynamic occupancy is needed, pass a runtime predicate; do not claim desktop footprint parity without its test. |
| P3: life-aware native view | Coordinate `CreatureView`, `OnlineProtocol`, `OnlineConnection`, `OnlineClient`, server snapshot/attack parsing in `PreviewServer` and direct tests: current-life attack reference, facing/moving/attack state and committed death/respawn. | Handoff for protocol/HTTP files and live adapter ownership; integrate once with loot/quest wire changes. Visual staging/review rules apply to actual visual changes. |
| P4: integrated authored scope | Validate hosted routes/collision and ordinary creature support; add clan behavior if this exact approved region requires it; connect full quest/loot/ability behavior. | Actual scope choices recorded; Claude quest/ability integration, supported loot and hosting dependencies complete. |

P1 is the next bounded cheap-model coding task after its narrow file claim: extract immediately
used pure scalar rules from the existing offline caller, preserve boundary semantics, and validate
the existing stationary protection/sanctuary tests. It does not turn on online AI and must be labelled
groundwork. P2 is the next functional delivery; do not split it into independently enabled spatial
features that attack at spawn while rendering motion. A bounded build/DB window is a separate claim;
none authorizes stopping the owner's current schema-9 QA server or native clients.

## Focused verification and acceptance

Search/extend existing coverage before adding tests: `BaseMonsterAttackabilityTest`,
`SanctuaryCombatRulesTest`, `MonsterManagerHuntingRespawnTest`, `OnlineMonsterContentExporterTest`,
`PreviewMapTest`, `OnlineMeleeTest`, `OnlineRetaliationTest`, `PersistentCreaturesTest`,
`HostedTransferTest` and `persistence/OnlineStoreTest`. Existing retaliation coverage explicitly
checks committed-only HP, observer isolation, cooldown/reconnect and death/logout position;
creature tests cover delayed aggro reads and durable respawn. Preserve those behaviors.

Add only missing consequential cases:

1. One deterministic natural-aggressive versus passive-provoked case, with natural/retaliation
   leash boundary and current-target retention/tie arbitration. Assert no strength/level gate.
2. One obstacle/corner path showing both coordinate axes obey pixel speed, no wall traversal,
   no attack without LOS, and return after the authored unreachable interval. Reuse sanctuary
   endpoint fixtures; test no healing or new life on return, not every compass direction.
3. One PostgreSQL moving-life strike join: snapshot, melee range, drop and quest objective use
   the same displaced death position; stale life/state loses without reward. Restart recovers
   wounded position/health/reason/cooldown; new respawn returns to anchor with a new event UUID.
4. Extend the delayed-read race to a pending motion/aggro checkpoint followed by death/transfer:
   old completions cannot restore old target/epoch/position. Replaced/expired leases and
   awaiting-region-ready players cannot receive attacks or new aggro; reconnect resumes only
   from fresh valid binding. A different player remains unaffected.
5. One failure boundary around spatial combat commit: precommit rollback leaves all state intact;
   postcommit lost acknowledgement reloads exactly the committed location/damage/drop/credit once.
   Block the creature worker while unrelated player movement/ticks continue. Verify graceful
   creature flush and explicit failure on an undrained shutdown. Reuse 0208 recovery infrastructure
   only after an explicit tool/resource handoff; it is not owned by this design task.

Representative commands after acquiring the appropriate windows: focused root tests with
`mvn -q -Dtest=BaseMonsterAttackabilityTest,SanctuaryCombatRulesTest test`, then
`mvn -q -f multiplayer/pom.xml verify` with the dedicated PostgreSQL environment. Use exact new test
names/selectors when implemented and retain the real command/result in ACCEPTANCE; these commands
have **not been run by T4C-0209**. Build the integrated candidate/package from its exact committed
source; do not relabel older schema-9 smoke evidence.

Final acceptance requires two **rendered** clients on the same integrated candidate: observe a
natural aggressive encounter and passive provocation; have two characters contend for one target;
run around a wall, out of reach, into sanctuary and through a supported region boundary; see the
same pursuit/return/death/new life; verify displaced loot/quest credit; disconnect/rejoin the target;
gracefully restart and force-recover the server with the declared checkpoint limit. Repeat supported
role costs/abilities and preserve offline launch/save/preferences. Record exact client/server/content/
protocol/schema revisions, observed results and limitations. Native adapters and passing services
are useful prerequisites but do not satisfy this rendered M4 gate, M6, remote acceptance or Beta.
