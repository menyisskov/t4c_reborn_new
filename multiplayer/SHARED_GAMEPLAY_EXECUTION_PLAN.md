# Shared gameplay execution plan (T4C-0405)

Owner direction: 2026-10-31. Baseline `f4b34fa3`; branch `codex/shared-game-runtime-0405`.
This is an execution plan, not completion evidence. Exact offline behavior and presentation
for the entire game are required before the requested handback. The original implementation
is the source of execution, not a specification for parallel online implementations.
Historical intermediate Beta allowances do not reduce this request.

## Execution boundary

Keep one authoritative world simulation. Offline and server invoke the same extracted
execution components. Offline adapters retain current object state, callback order, random
sources, collision/path queries and persistence behavior. Server adapters supply authoritative
participant/world state inside the existing authenticated, revision/epoch/life-fenced command
and transaction boundaries. Clients send intentions and render committed state and ordered
presentation through the existing screen, GUI, animation, sound and effects implementations.
Client puppets do not independently grant rewards or resolve combat.

The existing domain Maven module already compiles selected original sources directly. Extend
that boundary with rendering-free original execution; do not create a copied server rule tree.
A component is shared only after the offline caller delegates to it and the server calls that
same source. Shared final-value formulas alone do not establish shared lifecycle execution.

Preserve mutable object identity where existing scripts depend on it. Explicit ports cover
participant state, world queries, collision/path search, time/randomness, persistence and
presentation. Do not collapse per-character quest flags into global state. Tick each world
entity once; participant commands have an explicit actor context and authoritative ordering.
Multiple participants must never multiply AI timers, periodic damage or world-event ticks.

## Phases and review boundaries

Each row is a sequence of bounded PRs, not permission for one unreviewable whole-game diff.
Characterize the original behavior first, move it behind a port, delegate offline, verify, then
adapt the server and verify the consequential persistence joins. Finish each reviewed slice
before rebasing the next slice onto integrated main. No subsystem or milestone closes merely
because its first extraction passes.

| Phase | Original execution to reuse | Dependencies and exit evidence |
| --- | --- | --- |
| 0: Baseline and inventory | Screen/session/input entrypoints, registries, saves, GUI and render goldens | Record build/content/schema versions; enumerate actual registry coverage dynamically; paired offline/online scenario ledger identifies every unsupported path. Existing historical counts are not current coverage. |
| 1: NPC locomotion | BaseNPC patrol, target selection, pause, path waypoint progression and conversation suspension | First move original patrol execution into NpcLocomotion; retain protected state compatibility. Offline path adapter calls original Pathfinding. Seeded traces preserve float/random/callback order. This first PR does not provide server NPC parity. |
| 2: Shared path and NPC execution | Original Pathfinding and NPC constructors, initialize/popup, authored scripts, hostile/flee behavior and interaction lifecycle | Make the existing path search accept a graphics-free collision port while retaining algorithm and production budget. Server then uses that search and shared locomotion, never an online allowlist. Extract state/world/presentation access around actual script methods; all NPC registrations and script types remain accounted for. |
| 3: Monster world execution | BaseMonster/MonsterManager creation, script hooks, AI, target/path selection, combat, death, respawn, clans, loot and summons | Explicit participant context, one entity tick per world step, original manager ordering and authored definitions. Retain atomic creature life, damage, loot, reward and quest fencing. Paired traces plus real transaction/retry tests. |
| 4: Player and economy | Original commands, movement/dynamic collision, combat, equipment, inventory/item use/drop, shops/bank/storage, training, skills/stats, progression/rebirth and death/recovery | Extract actual service execution and wire original GUI command entrypoints. Preserve attribute requirements, per-instance charges/identities, quantities, save compatibility and account storage semantics. Atomic multi-character/ground transfers conserve ownership. |
| 5: Spell lifecycle | Actual screen casting, SpellCastingService and SpellEffectManager execution | Reuse targeting, start/refusal/exhaustion, projectile and impact timing, AoE, drains, buffs/debuff expiry, periodic effects, channels, summons, auras and monster spells. Existing SpellEngine internals delegate or retire after migration; formula equality alone is insufficient. |
| 6: Complete world and systems | Original authored map/spawn/portal/teleport registry and quest/world-event runtime | All worlds/routes, campaigns and quest types; journal/kill log; companions/tame, harvest/crafting, trials/divine/karma, return scrolls and world clocks. Preserve lifecycle and persistent/global/per-player distinctions. No shortened quest or moved objective. |
| 7: Original presentation and controls | Existing dialogue/shop/storage/training/inventory/spellbook/statistics/journal/options/macros/creation components and input handlers | Original layout, pointer/keyboard actions, quickslots/macros/preferences, cursor/facing/walk/attack/hit/death/cast animations, VFX, floating feedback, sound/music/zone behavior. Ordered event identities/recipients prevent duplicate effects across polls/reconnect. Multiplayer chat remains integrated in original chat. |
| 8: Reliability and delivery | Existing auth, command receipts, persistence and operational tooling | Lost acknowledgements, retries, stale revisions/epochs/lives, simultaneous actions, shutdown/restart/crash, abuse and restore. Full integrated two-rendered-client mixed stability and package/remote gates remain required; record actual duration under the owner decision removing the fixed two-hour minimum. |

## First extraction invariants

Do not rebalance or repair original behavior while moving it. BaseNPC constructor and spawn
initialization consume their existing random draws. A paused tick crossing zero selects its
new target but stays stopped for that tick. Arrival uses the original strict distance threshold,
snaps to the target, draws a pause and clears its path. Random target selection retains its
angle/radius math, draw order and twelve attempts. Path retarget/waypoint thresholds, unavailable
path behavior, collision clearance, conversation target-reference/pause save and restore,
hostile/flee sharing of the path cache and facing behavior remain intact.

Pathfinding currently uses both expansion limits and a 16 ms wall-clock guard checked every
32 expansions. Preserve the production policy during extraction. Deterministic bounded
fixtures characterize normal paths; budget interruption has separate evidence. Do not claim
arbitrary load produces an identical trace when the original algorithm itself uses elapsed time.

## Required coverage ledger

For each row below, record canonical source/caller, shared implementation, offline delegation,
server caller, transaction/save compatibility coverage, rendered offline reference, rendered
online result, simultaneous-player/recovery result and remaining limitation. An unchecked
column remains open. Representative tests cover distinct paths; content integrity checks join
every actual registered reference rather than duplicating one test for each item/NPC.

| Coverage family | Required scenarios |
| --- | --- |
| Accounts/characters | All offered classes, original creation/selection/preview, allocation/training/rebirth, independent credentials and reconnect |
| NPCs/services | Every lifecycle/script category; conversation start/end/range/retry; shops, bank/storage, trainers, quests, summons and global/personal flags |
| Combat/monsters | Melee, bows/quivers, dual weapons, stealth/stun/armor/auras, all AI/script/loot categories, clans/bosses, reset/death/respawn |
| Spells | Every registered effect and target category including channels/projectiles/periodics/summons; expiry, cancellation, failure and audiovisual order |
| Items/economy | Equip/use/drop/pickup, instance charges, quantity prompts, uniqueness/weight, currency, corpse, crafting/harvest and simultaneous ownership |
| World/progression | Every authored route/portal/quest/campaign/world-system category; durable rewards, clocks, world events, trials/divine/karma, companions |
| Interface/media | Original screen/panel/input inventory, mouse and keyboard, hotkeys/macros/options/preferences, animations/effects/sounds/music and feedback |
| Persistence/security | Old saves and protected preferences, reconnect/crash/retry, no duplicate rewards/transfers, authorization and abuse, clean packages/remote transport |

Completion requires this complete integrated ledger and the canonical PARITY, MILESTONES and
ACCEPTANCE gates, including two real rendered clients and mixed-session stability/recovery evidence with actual duration recorded. The owner removed the fixed two-hour minimum on 2026-10-28 (DESIGN_GUIDELINES); older minimum-duration wording is historical and does not restore that gate.
Service or adapter success never substitutes for observed rendered behavior. Record failures,
exact commands/build/content/schema/hashes and limitations; keep credentials and personal state
outside version control. Root coordinates GUI, disposable runtime and integration resources;
peer QA18773/18774/55441 and protected owner state are outside this work window.

## Current execution state

Phase 1 is assigned to a cheap-tier implementation agent in the isolated worktree; no passing
result or server adapter is claimed in this document. A mid-tier read-only NPC lifecycle audit
identifies the next exact port extraction. Root owns canonical TASKS/PEER_WORK and Git integration.
All whole-game parity gates remain open until their evidence is recorded.


## Concrete-object headless runtime decision

Prefer retaining actual Player, ScriptedNpc and authored subclasses over replacing every
NpcBehaviorContext.player()/npc() use with a new partial model. Player's constructor creates
PlayerAnimations and PlayerMovement; BaseNPC creates NPCAnimations, which creates composite
PlayerAnimations or loads its single sprite base. Existing tests already construct these
objects with an unloaded SpriteLoader, so a GL window is not inherently needed merely to
instantiate them. This is feasibility evidence, not proof of production headless execution.

An empty sprite loader is NOT a correct production adapter. Attack completion and animation
state inspect actual frame counts. Separate original sprite-bin metadata and animation
timeline from texture allocation/rendering. The authoritative execution must receive the real
metadata while the native renderer retains texture/frame presentation. Preserve constructor
random draws, frame timing, attack gates and all callbacks. Depending on LibGDX core types
(Vector2 and declarations referring to SpriteBatch) does not require invoking a GL backend;
a shared runtime module may depend on core while excluding desktop backend/GL startup.

Next design spike inventories the reachable source/dependency closure and proves construction
plus real-script execution using injected metadata/presentation/runtime-service ports. Keep
the lightweight pure domain boundary for existing rules; add a shared concrete runtime boundary
only after the closure and package build are measured. Avoid teaching the server a second
Player or scripted-NPC implementation.

The direct GUI operations are concentrated: ScriptedNpc opens learning/training/shop/selling
screens, emits shared system messages and triggers NPC spell VFX. Route those boundaries into
original local actions or recipient-addressed server presentation events, retaining the
original script and concrete Player mutations. Services must return domain offer/request data
without constructing a Screen on the server. Other direct authored GUI/static calls still
require an inventory; the concentrated ScriptedNpc sites do not establish universal coverage.

Global NpcWorldFlags, NpcScriptRuntime summon bookkeeping/callback and collision providers must
be scoped to a world runtime. A scoped runtime context may preserve existing static entrypoints
as offline adapters, but must not leak across worlds/players or survive beyond an operation.
Callbacks become deferred effects: draft concrete state and ordered presentation, atomically
commit existing authenticated persistence, then publish. Failure discards the draft and all
presentation/rewards. Preserve full state, inventory instance identity/charges and mutable
references; serializing only the current partial CharacterProgress is not sufficient.

This route avoids hundreds of mechanical behavior callsite substitutions, while still requiring
real transaction/state mapping, world ownership, metadata and presentation separation. A full
rendering backend or empty no-op graphics objects alone solves none of those requirements.

### Next build proof: single compiled class owner

Approved next PR boundary: add multiplayer/runtime as game-runtime, compiling the original
root sourceDirectory directly. Its production dependency graph provides LibGDX core, Gson and
logging; desktop backend and freetype required for compilation remain provided-only. The
existing game-domain becomes a compatibility facade depending on game-runtime, retains its
focused tests and stops recompiling the same production classes. Server keeps its current
domain dependency and receives one original-class runtime jar transitively. The offline root
build remains its original source build. This avoids duplicate gameplay classes on one server
classpath and avoids manually maintaining hundreds of source includes.

The all-source jar can contain unloaded desktop entrypoint classes; that is a transitional
packaging boundary, not a claim every packaged class is graphics-free. A forked headless smoke
must run with no backend/native runtime dependencies, construct real Player and authored NPC
objects, execute a meaningful real script mutation and prove no save/window side effect.
Runtime resources must use the real existing asset paths. Dependency inspection and headless
execution establish classpath feasibility only. Empty-animation construction in this proof
must not be wired into production combat. The subsequent real sprite metadata/timeline and
presentation ports remain required before authoritative gameplay adoption.

### Frame asset boundary, without placeholder animation

Animations retain ordered frame names/metadata rather than eager TextureRegion lists. Keep
all existing discovery loops and fallback selection; only a render operation resolves the
chosen name through the existing SpriteLoader texture cache. Add a java.nio sprite-bin load
entrypoint for the headless runtime, using the original SpriteBinIO parser and real metadata.
No synthetic TextureRegion, fake frame or empty animation stands in for a real authored frame.

Exact next source scope: SpriteBinIO (only if metadata streaming requires a common reader),
SpriteLoader, a new immutable frame metadata descriptor/catalog, EntityAnimationsBase,
PlayerAnimations, NPCAnimations, MonsterAnimations and focused existing/new metadata tests.
SpriteLoader indexes texture identity by FIRST duplicate lower-case name but offset metadata
by LAST duplicate name; preserve both policies. Player frame discovery continues over missing
letters; monster discovery stops at its first missing frame and also handles numeric/angleless
patterns. Preserve direction fallback, original body-part and angle iteration, maximum attack
frame counts, minimum durations, native timing profile, walk-loop callback and death completion.
Cache shadow frame identities lazily too, so constructing real actors never uploads a texture.

Validate frame identity/order/counts against real sprite-bin metadata and the frozen original
loader discovery, then compare actual Player/NPC/Monster timeline state across identical deltas.
Client render goldens and native GL smoke remain necessary because moving texture allocation
can affect cache/refresh/offset behavior even when the counts match.

### Authoritative concrete Player state: lossless draft requirement

Do not use ProgressMirror -> PlayerStateDto -> PlayerStateMapper as the server's authoritative
round trip. It is a client display projection: it clamps long gold/storageGold to int, expands
stacks into repeated string keys with parallel UUIDs, aggregates inventory charges by key,
omits cooldowns and other runtime fields, and reconstructs active buffs from remaining seconds
instead of their stored rolled amounts. PlayerStateDto is a legacy save contract, not a complete
runtime snapshot. Reusing it would lose authoritative state and can re-roll buffs.

Before routing an online mutation through original Player, introduce an exact in-memory
snapshot/draft contract around original state: stats/vitals/base and derived bonuses, exact buff
amounts/timestamps, cooldown/exhaustion/stun/stealth/detection state, regeneration accumulator,
progression, flags with expirations, position/movement reservation, inventory/equipment/storage,
companions and operation callbacks. Separate transient presentation-only state from persistent
state explicitly. Use trusted committed server state plus current world location/epoch to
populate it; after execution produce validated persisted changes and deferred ordered events.
No direct file writes or immediate world publication may escape a speculative transaction.

Server item UUID/quantity/charge/container ownership requires an instance-aware backing model
or a mutation journal at the original inventory service boundary. A plain final List<String>
diff cannot distinguish identical item instances or faithfully conserve per-instance charges.
Retain existing original list views for gameplay/UI compatibility while preserving identity
through their actual add/remove/equip/storage operations. Do not infer a new identity for an
unchanged item after every command. Existing server long-valued balances also need an explicit
compatibility decision where original Player uses int; never silently clamp or truncate.

Prove unchanged round trips first (including duplicate keys with different charges, worn items,
storage, rolled buffs, deadlines, large balances and old record versions), then a representative
real original service/script mutation, refusal rollback and lost-ack replay. Atomic receipt,
revision/epoch and creature-life claims remain the commit authority. Multi-character actions
share one atomic unit; only after success are concrete state and presentation published.
