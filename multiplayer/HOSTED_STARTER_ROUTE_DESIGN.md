# Authored starter-route hosting — T4C-0214

Architecture and read-only data audit, 2026-10-27, Asia/Jerusalem. **Candidate, not an approved
content/class scope, enabled route, native probe pass or M2/M3/Beta completion.**
[MILESTONES.md](MILESTONES.md), [SCOPE_PROPOSAL.md](SCOPE_PROPOSAL.md),
[QUEST_ABILITY_DESIGN.md](QUEST_ABILITY_DESIGN.md) and
[MONSTER_PURSUIT_DESIGN.md](MONSTER_PURSUIT_DESIGN.md) retain their scope and acceptance rules.
The owner has not approved the proposed starter classes, quest scope or exclusions.

## Decision supported by the audit

Use one contiguous world-0 `start` region, **[2735,2984) × [1029,1146), 249 × 117 tiles**,
plus the existing world-1 interior [264,344) × [353,413). Keep the actual portal16/17 route.
The small expansion contains a collision-valid walking route from the temple to every authored
quest Skeleton and stays inside existing descriptor/body/actor limits. No artificial portal,
chunk-boundary transfer, quest relocation, reward change or full-world streaming is needed.

This is the minimum axis-aligned rectangle containing both the complete existing world-0 crop
and every integer objective tile of the unchanged `renewed_wards` radius-50 circle. The circle
has x=2735..2835 and y=1045..1145 inclusive; the existing crop supplies the northern/eastern
bounds. Temple (2944,1059), Warden Aelric (2948,1044), Kilhiam (2961,1058), the world-0 return
from portal17 and the verified paths below are already inside. A smaller rectangle would remove
existing hosted territory or some objective tiles. This is a tile-domain minimum, not a claim
that every enclosed tile is traversable or that every enclosed script is supported.

Do not use the earlier illustrative [2720,3008) × [1008,1168) rectangle: it is 288 × 160,
exceeds `OnlineRegion`'s actual 256-tile per-axis bound, adds unnecessary encounters and brings
in EDGAR's unsupported unique `item.iron_key` drop. No cap increase is justified by this route.

## Source and diagnostic evidence

Runtime/content source inspected at fetched main `42b32feaa9f052da8a64d435132c0d7703c98c4d`
(PR148), with local metadata commit `6509d83f`. Server protocol is 5 and database schema is 10
in that source. T4C-0212's dirty map/world/controller preparation was read only as WIP; it is not
an integrated pursuit build. No game, server, database, exporter or GUI process was run here;
no asset or production source was rewritten. The pure Python diagnostic reads files only and
writes its own TEMP report. No online gameplay result is inferred from it.

| Input | SHA-256 |
| --- | --- |
| `assets/maps/worldmap/worldmap.colbin` | `6f2f5fc62e9dd1a8507ca19c11069f3750cc124034f15220c0ab9bf360c17bc8` |
| `assets/maps/dungeon/DungeonMap.colbin` | `1fa25a9092eb5eee1f4e7e37bfaefc6163ba34ec693efd54c494776f5423c09d` |
| `multiplayer/server/src/main/resources/content/monsters-v1.json` | `3c207b7f08319ff1a7fa31e10156b2016aafeabd145031144bca679c2ea1a31a` |
| `.../content/portals-v1.json` | `67df5e51a4f7141df5902959e1948df71d9050ab626be42f9c199efdb50aa500` |
| `.../content/items-v1.json` | `bfd3a0cfb65d95d45328597764eb3c55c7cb39faba29ac7ea5c83df07e5e3989` |
| `.../content/quests-v1.json` | `7b4ed924ab353839c3859dc50aecf697e23659886b1c2f8f9170f99ab87cc555` |
| `.../content/npc-interactions-v1.json` | `42637848d3b74f9fef97a10913530f4b04a42ec6115deab786666f4b5134f5a5` |

Exact audit command executed from the checkout:

```powershell
python C:/Users/menyi/AppData/Local/Temp/t4c-0214-route-audit-20261027.py
```

Final diagnostic source SHA-256 is
`f7020d104319e8eb93579d7590c3c075057652d2ca5b8934ebfbc3876bb519e6`.
Its final report is `C:/Users/menyi/AppData/Local/Temp/t4c-0214-route-audit-20261027-final.json`,
SHA-256 `35857cd3be9f4d2dc4cc34d174721b2c2c0a8ccf83d3dfe384cb605109a218ba`.
The earlier non-final report is retained: its first diagnostic used an incomplete item-key
normalizer and an unnecessarily strict two-tile NPC approach. Final results use the actual
`ItemKeys.normalize` transformation and strict NPC distance squared <120. Do not cite the
preliminary unresolved-loot list or preliminary NPC shortest paths as content failures.

The diagnostic mirrors `BinaryIOUtils`'s T4CBIN v1/DEFLATE envelope and little-endian dimensions;
the actual world map is 6144 × 3072. It applies `RegionMapLoader`'s raw movement codes 1–4 and
out-of-domain refusal, the one-tile above-foot clearance, and only enabled portal-source exceptions.
Eight-neighbour BFS rejects diagonal movement unless both orthogonal cells are free. Attack/talk
approaches use the server's raw supercover LOS including endpoint/corner checks. This is a static
terrain audit; it cannot prove runtime dynamic occupancy, native input handling or combat survival.

## Actual connectivity results

The 29,133-cell minimum crop has 20,815 player-walkable cells; 19,539 are reachable from the
temple centre (2944.5,1059.5). The full quest circle contains 7,845 integer objective tiles.
All **13** authored Skeleton anchors inside it have a reachable legal player-melee approach:
pixel distance <=64, LOS, neither endpoint a PvE sanctuary. They require 150–180 BFS steps from
the temple. The disconnected cells are not silently changed into floor.

| Destination | Reachable approach centre | Shortest BFS steps | Meaning |
| --- | --- | --- | --- |
| Warden Aelric (2948,1044) | (2940.5,1051.5) | 8 | Strict authored talk range and raw LOS; no need to stand on the NPC. |
| Kilhiam (2961,1058) | (2953.5,1050.5) | 9 | Existing first-letter interaction remains reachable. |
| Skeleton (2798,1100) | (2798.5,1103.5) | 150 | 58.24 pixels from its current anchor; valid pre-pursuit melee approach. |
| Portal16 source (2931,1056) | Source tile reachable | 22 | Existing authored entrance remains in the same connected component. |

The Skeleton anchors are (2782,1071), (2790,1073), (2775,1076), (2790,1082), (2772,1086),
(2767,1087), (2782,1091), (2802,1095), (2798,1100), (2785,1106), (2774,1109), (2782,1114),
(2781,1115). Do not replace these with a convenient custom hunting creature.

One exact compressed BFS path to the 150-step approach is below. Coordinates are tile indices;
walk through their centres, expanding each straight cardinal/diagonal segment. This path avoids
all authored portal sources. Recompute from advertised collision/current snapshots in the probe,
rather than treating this path as an unconditional movement permission:

```text
(2944,1059) (2943,1059) (2937,1065) (2937,1067) (2935,1067)
(2934,1068) (2923,1068) (2903,1088) (2901,1088) (2900,1089)
(2893,1089) (2880,1076) (2872,1076) (2848,1100) (2839,1100)
(2837,1098) (2807,1098) (2800,1105) (2798,1105) (2798,1103)
```

Temple raw code is **16**, which is PvP-safe but is not PvE sanctuary6/7. Do not claim the temple
alone tests monster sanctuary behavior. The static BFS is not a timing guarantee: server player
movement uses fixed swept substeps, and native polling latency and pursuit change the live route.

## Content census and startup blockers

The minimum crop contains **39 monster-export spawn records**, compared with six in the old crop:

| Exported type / resolved definition | Count | Authored behavior / support audit |
| --- | --- | --- |
| Olin Haad Guard 3 | 19 | Passive, physical retaliation; no authored item drops. |
| Skeleton | 13 | Aggressive physical encounter, all inside the quest circle. |
| Goblin | 4 | Aggressive physical encounter; Goblin→Horse clan hostility also exists. |
| Wandering Orc | 1 | Aggressive physical encounter. |
| Horse / Wild Horse | 1 | Passive, `canAttack=false`; do not invent a monster attack. |
| SUNDIAL / unresolved monster definition | 1 | (2881,1070), actually owned by the offline NPC system; current `PreviewWorld` rejects startup. |

The 38 resolved ordinary actors have no source-event scripts. Every authored drop resolves through
the actual `ItemKeys.normalize` rule to a catalogued non-unique item; this matches the current
`OnlineLootDrafts.supports` criterion. Physical attack definitions use supported dice, not monster
spells. No hunting-ground anchor falls inside the minimum crop; custom Witness/training/hunting
leashes and rewards are unnecessary for this candidate. All existing player/effect/quest/companion/
stun/loot guards remain in force. Current source has the supported quest-flag allowlist; that does
not imply lethal kill-credit production or a completed quest playthrough.

`SUNDIAL` is not evidence that unknown monsters should be skipped. Offline
`MonsterManager.isNpcOwnedSpawn` deliberately excludes `SUNDIAL` and NPC factory types, and
`NPCManager` makes Sundial stationary. Its actual NPC sprite is `@static:Horloge Solaire`, with
scripted clock dialogue. Fix the export/ownership boundary narrowly: classify verified NPC-owned
records separately using the same offline registry decision, retain them in an explicit diagnostic
manifest, and keep unresolved **ordinary monster** references fatal. Do not fabricate a MonsterDef,
turn on Sundial combat, or add an “ignore all unknown spawns” switch.

The minimal candidate does not require porting Sundial's clock script. It does require an explicit
declared unsupported NPC classification, with any replicated authored appearance routed as an NPC
and interaction refused unless implemented. Reuse the existing NPC appearance schema where possible;
do not register it as an interactive quest giver or claim it rendered before native verification.
The exporter/model validation change must be versioned and freshness-tested, not an ad hoc startup
catch. The exact player-visible exclusion remains part of the unapproved checkpoint scope.

A separate literal `@Spawn` audit of the offline NPC package found 29 records inside the crop,
with zero unparsed annotations: Kilhiam/Aelric, the MONSTER-tagged Sundial and 26 other AUTO NPCs.
These include shops, storage, scripted doors/portals and Dragon. Only the two story NPCs are in the
current online interaction catalog. Hosting terrain is not a claim to support the other NPCs or
their scripts. Preserve the explicit catalog allowlist and publish the exclusions before acceptance.

The unchanged interior contains 11 resolved ordinary actors (three Bat and eight Brown Rat;
`Rat` spawn aliases resolve to Brown Rat), not eleven source-event scripted monsters in this
content version. Reaudit its current loot/physical support when building the candidate; older
handoff wording about “scripted residents” is historical, not the current source census.

Goblin→Horse is the actual default directional clan relation. Player-only T4C-0212 pursuit cannot
claim complete authored behavior for this crop. Implement that required clan interaction with
durable creature lives and no fabricated player reward, or record an explicit accepted checkpoint
exclusion. Do not disable Goblins, remove the Horse or silently rewrite the relation to fit a demo.

## Portals, return and saved-coordinate compatibility

The candidate includes portal sources 12/13/14, 16 and 947. Only 16 remains enabled; 17 remains
its existing world-1 return. Sources12–14 lead to unhosted world1(134,667), and947 to unhosted
world1(901,2753). All four need an explicit unavailable-route result, without changing destinations.
The larger rejected rectangle would add18/19/47/948/1030 as further unrelated route obligations.

Current `HostedRegionPlan.startingSlice` filters the catalog to16/17, and `PortalCatalog.resolve`
returns empty for missing/unavailable destinations. `PersistentSessions.maybeTransfer` then does
nothing. Thus “not enabled” currently prevents a transfer but does **not** produce the explicit
refusal required by M3. Preserve all authored source metadata separately from the enabled graph;
recognize a refused source, report a bounded player-facing unavailable-route reason, and allow
the player to walk away. Do not open an unhosted destination or silently substitute the temple.
Coordinate the feedback shape with the protocol/native owners; do not bump protocol merely for
larger map dimensions, which already fit.

Store character/respawn/corpse/loot coordinates remain absolute world coordinates. Enlarging the
crop changes the descriptor/local x origin by -169, not the persisted character's actual x.
For example temple global2944.5 becomes local209.5 instead of40.5; converting it twice would move
the player. Existing spawn persistence keys are world+authored anchor+type and must remain unchanged.
Runtime creature local IDs will be renumbered by added spawns; never store or migrate those as lives.

Keep region key `start` and character `location_epoch` for ordinary walking within that region.
Actual portal/death transitions keep existing epoch increments, readiness freeze and lease fencing.
Roll the candidate out by draining/stopping the old server and reconnecting clients to its new
descriptor; no live origin change under old snapshots. Never mix old-origin actors, cached targets
or map views with the new descriptor. A reconnect restores global committed coordinates inside the
new bounds; saved respawns outside hosted regions remain explicit refusals, not repaired/warped saves.
Rollback to the small crop cannot safely admit players saved in its western expansion: retain
the larger hosting plan with compatible code or use a separate matching restore; do not erase progress.

## Wire, capacity and load consequences

`OnlineRegion` validates each axis <=256; native initial map parsing's looser <=1024 check does not
override `map.region()` validation. Candidate249×117 fits without changing either cap. The diagnostic's
compact UTF-8 map model (same field order/booleans as `PreviewServer.mapJson`) is **166,878 bytes**,
versus27,561 for the old crop. The native body subscriber caps every response at2MiB; this map is
well below it. This is a serializer-model measurement, not an actual HTTP timing/adapter pass.

The 38 resolved current `CreatureView` entries total6,289 compact bytes before future pursuit/life
fields. Both server and adapter cap creatures at256. Two catalogued NPCs fit the adapter's64 limit;
ground loot is separately limited to128 per snapshot. Private character JSON is a separate response
with its existing1MiB store/2MiB transport limits. Test the final actual serialization, including new
life/motion fields, at its real maximum values. Keep20 global player slots across both regions.

All actors still replicate to every viewer in a region; there is no creature AOI. Native loops sleep
75ms after polling work, so the added creature array alone is roughly84kB/s per client or1.68MB/s
at20 clients at the theoretical13.33 polls/s, before players/loot/envelopes/HTTP. This is an upper
poll-rate arithmetic estimate, not measured bandwidth. Map downloads occur on join/descriptor change,
not every snapshot. The renderer already loads allowlisted world assets rather than a new map file.

More active creatures increase acquisition scans, serialization, AI paths and persistence work.
Keep the T4C-0212 fair global path budget, fixed34Hz step (~29.41ms), bounded worker queues and
coalesced creature checkpoints. Record tick/deadline debt, queue/DB latency, snapshot bytes, CPU,
memory and errors on this candidate; the running old17-spawn baseline is not its performance proof.
`listGroundLoot` also caps a region read at256 oldest live rows while public views show128 nearest
cached rows. Exercise a populated boundary: excess rows must be paged/discoverable or explicitly
reported as an unsupported capacity condition, not silently presented as all loot loaded. Do not
remove caps as a hosting shortcut. The expanded static region alone needs neither DB schema nor AOI.

Full-world hosting would load18,874,368 collision cells and many unrelated spawns/scripts, exceed
current descriptor/actor/body assumptions and require AOI/streaming/ownership work. Multiple adjacent
chunks add seam travel, persistent epoch handoff and loot/AI boundaries that do not exist today.
Neither solves a measured failure of this249×117 candidate. Reconsider them only after an actual
candidate limit/performance failure that cannot be fixed within the bounded region design.

## Correct native loot fixture and implementation handoff

PR147's `NativeLootProbe` provisions world0(3700.5,1450.5) and starts `ServerMain --online`, which
always uses `startingSlice`. That point is outside both current and proposed regions; join fails
`409 unhosted_region`. It also never moves, its `nearestSkeleton` selects the first matching actor,
and its old nearest target was ~98 physical pixels away versus64 player melee reach. These are
static source facts; this task did not run or pass the probe.

Correct the probe only against the real integrated candidate: provision distinct disposable online
characters at a verified hosted temple point, confirm exact advertised region/protocol/readiness,
then BFS its published collision grid and drive **direction-only OnlineConnection input** from
observed positions. Use centre waypoints, stop/replan on lack of progress, avoid portal source tiles
unless testing a real route, and enforce bounded total/action deadlines. Select authored definition
`Skeleton` by live snapshot identity/position and seek current <=64px+LOS reach; after moving AI is
integrated, keep tracking/replanning rather than attacking an old spawn anchor.

Move the observer/racer and overweight character within the server's8-tile Chebyshev pickup range
of the actual committed death/drop position before asserting ownership/race/capacity behavior.
Seeing a replicated item does not authorize pickup. Handle real retaliation/death/respawn and
eligibility without lowering monster stats, moving objectives, changing rolls or writing coordinates.
Natural drop randomness means “three drops within30 kills” is not guaranteed; insufficient drops
is an incomplete attempt, never a pass or reason to force loot. Keep real refusal codes and expose
unexpected combat refusals instead of swallowing every exception until timeout. Closing immediately
after a pickup request proves an ambiguous close/reconnect case only if the outcome was actually
unacknowledged; an already completed reply is not lost-ack evidence.

The next cheap-model coding task, **after an explicit exact-commit handoff**, is one cohesive hosted
candidate slice: validated249×117 spec + principled NPC-owned-spawn classification + focused real-map
connectivity/constructor test, retaining unknown-monster failures and current portal16/17. Likely files:
`HostedRegionPlan.java`, `MonsterContent.java`, `OnlineMonsterContentExporter.java`, its generated
monster catalog and exporter/catalog tests, `PreviewWorld.java`'s classified membership, and a new or
extended focused hosted-route test. Reuse the actual offline ownership decision; do not copy a broad
ignore list. No default wider hosting should ship while its startup classification remains unresolved.

Before editing, recheck the canonical register and obtain Claude's temporary latest-state grant for
`HostedWorlds`, `PersistentSessions`, `OnlineStore` and `OnlineMelee` if touched, plus Codex0212's
`PreviewMap`/`PreviewWorld`/controller/test handoff. Portal-refusal feedback needs released HTTP/protocol
ownership and Claude's session/native ownership. NativeLootProbe/NATIVE_LOOT_PROBE remain Claude0204
scope; its fixture correction is an explicit handoff, not permission supplied by this design. Root
integrates shared TASKS/CHANGELOG/MILESTONES/ACCEPTANCE/HANDOFF evidence together under the short lock.

Validate the candidate with one focused real-map route/constructor regression and current exporter
freshness/capacity contracts; extend existing `HostedTransferTest` for origin/global-coordinate and
refused-route behavior. Then run isolated PostgreSQL save/rejoin/death/transfer plus the corrected
two-adapter loot/quest scenario on matching committed binaries/content/schema. Finally two rendered
clients must walk temple→Aelric→authored Skeleton area→turn-in, see pursuit/loot/independent rewards,
return through16/17, die/recover and reconnect with exact committed global positions. Preserve the
offline save/preferences regression and remote/two-machine/full20-player gates. No tests/builds or
those acceptance scenarios were executed by T4C-0214, and no core ownership or QA window is claimed.

## Hosting reach audit and skeleton cave — T4C-0300, 2026-10-29

Measured from the real collision maps and the 1,118 authored portals (walkable-component graph;
8-neighbour movement without corner cutting; a portal joins the component of its source tile to the
component of its target tile). Result for "what is the next hosting slice that unlocks the most quests":

- **No adjacent crop reaches another quest.** The 34 unhosted quests have givers 900 to 3,000+ tiles
  from the temple. Only `windhowl_borderwatch` shares the starter continent (1.29 M walkable tiles,
  x 1395..3046); its kill circle (radius 230) alone needs 461 tiles per axis, above the 256-per-axis
  `OnlineRegion` bound, and the giver is about 1,100 land tiles away. All others are on different
  walkable components (separate islands) with no authored portal path from the starter crop.
- **Portal reach from the starter crop** is 15 walkable areas (BFS over the authored graph): the
  Lighthaven interior, ten more dungeon chambers and four small world-0 pockets. None holds a quest
  giver or a quest target circle. Hosting any of them would add hunting ground, not quests.
- **Quest content therefore needs a transit decision, not more map.** Options for the owner: (a) hosted
  per-quest regions (giver plus the portion of the circle that holds the kill count) joined by authored
  ferry/teleport NPC routes ported from single-player, (b) raising the region axis bound with
  area-of-interest streaming, (c) an explicit non-authored hub. This design previously ruled out
  artificial portals; none was added.
- **Shipped instead: the skeleton cave.** Portals 12/13/14 (cave mouths at 2770..2771,1094..1095, inside the
  renewed_wards kill circle) lead to a closed 111 x 111 chamber (38..148 x 582..692 of world 1, 4,113
  walkable tiles, 30 authored residents) whose only exit is portal 15 back to 2772,1096. It is the only
  zone reachable from the starter crop that needs no further hosting, so it is now region `skeleton-cave`
  with portals 12-15 enabled. `HostedKillRegionTest` floods the real dungeon map from the landing tile and
  proves nothing walkable lies outside the crop and that portal 15 is its sole exit;
  `HostedTransferTest` walks mouth to cave and back through the real session transfer path.
- **Cost.** +30 creature spawns (about 50 to about 80 hosted). Idle region step on a disposable PostgreSQL
  cluster: 0.003 ms for the cave, 1.04 ms for the whole three-region tick (budget 29.4 ms). The 20-adapter
  soak was not re-run for this change; pursuit cost scales with players near monsters, so re-measure
  before the next Beta soak.

**Decision (owner, 2026-10-30, T4C-0349):** option (b), raising the region bound with area-of-interest streaming, is chosen. The measured design and staged plan are in [WORLD_STREAMING_DESIGN.md](WORLD_STREAMING_DESIGN.md); the artificial-hub and per-quest-ferry options are not pursued.

## Lighthaven sewers, and a correction to the reach audit — T4C-0304, 2026-10-29

The T4C-0300 audit said the starter crop's portal graph reaches "ten more dungeon chambers". That count
flooded the whole 1.29 M-tile starter continent. A player can only walk inside the hosted 249 x 117 crop,
and re-running the same walkable-component graph from the crop alone reaches exactly five dungeon areas:
the skeleton cave (hosted by T4C-0300), the Lighthaven interior (hosted but cropped to 80 x 60) and a
chain of three sewer levels below it (portals 0/1, 2/3 and 4/5). The other chambers hang off continental
portals (20-27, 61-67, 948-973) that sit hundreds of tiles outside the crop; they need the transit decision,
not a bigger rectangle.

- **Lighthaven interior grown to its whole walkable component**, 278..442 x 267..432 (165 x 166). Stair 0
  sits at 415,395, 71 tiles beyond the old crop, so the old 80 x 60 crop could not reach any sewer. The
  old crop held no walkable tile outside this box.
- **Three sewer levels**, each a region of its own: upper 148..387 x 17..242 (240 x 226, whole), middle
  8..217 x 243..411 (210 x 169) and lower 138..277 x 412..557 (140 x 146). The tight boxes of the upper and
  middle levels overlap (148..217 x 172..242) and one region may not overlap another, so the middle level
  gives up its north-west lobe (2,709 of 18,532 walkable tiles, 8 residents) and the lower level a 30-tile
  nub with no residents. Both crops make the lost tiles unreachable (the region edge is a wall) and no
  portal, spawn or stair is lost; `HostedKillRegionTest` pins the exact permitted loss.
- **Unhostable spawns.** Persisted creatures must stand on a standable tile. Five authored spawns do not
  (three in solid rock, two under a wall), so startup would refuse the whole region. They are listed by
  persistence key in `HostedRegionPlan.UNHOSTABLE_SPAWN_KEYS` and dropped before the creature world is
  built; the guard in `PersistentCreatures` is untouched, and a test proves the list is exactly the set of
  unstandable authored spawns (no pattern, no silent skips).
- **Residents.** 27 + 46 + 43 + 28 creatures (rats, bats, giant bats, slimes, spiders, goblins), all low
  level, plus display-only appearance NPCs (Twin Nevanis, Twin Shovanis, Balork) with no interaction. The
  existing clan and unsupported-combat guards accepted every region; nothing was weakened.
- **Cost.** Idle step on a disposable PostgreSQL cluster: whole six-region tick 1.09 ms (was 1.04 ms for
  three regions; budget 29.4 ms), 0.003 to 0.008 ms per region. Pursuit cost still scales with players near
  monsters, and no new soak was run.
- **Still refused:** portal 947 and the continental portals. No quest is added.

## Implemented — T4C-0231 (Q10), 2026-10-27

`HostedRegionPlan.startingSlice` now hosts the world-0 `start` region at
**[2735,2984) x [1029,1146), 249 x 117** plus the unchanged world-1 interior. Character,
respawn, corpse and loot coordinates remain absolute; only the descriptor origin moved (-169 x).

- **Smallest region.** The SCOPE_PROPOSAL illustration (origin 2720,1020, 264 x 136) was not used:
  this rectangle already holds the temple crop and every objective tile of the quest circle
  (read from the quest definition, not retyped), and stays under the 256-per-axis region bound.
- **Portals.** Enabled graph is still 16/17. Sources 12, 13, 14 and 947 sit inside the region and
  target unhosted world 1; they are kept in `HostedRegionPlan.refusedPortals`, never transfer,
  and the destination stays unhosted. Walking onto a refused source leaves the actor, position
  and epoch unchanged and the session connected. There is still no player-visible refusal
  message: that needs protocol/native work owned elsewhere.
- **SUNDIAL (2881,1070).** The classified monster export (`monsters-v2.json`, from the offline
  NPC-ownership decision) marks it NPC-owned. `HostedWorlds` drops only NPC-owned records with
  no monster definition before building the creature world; it is not rendered online and has no
  interaction. Any other record without a definition is still fatal.
- **Goblin and Wild Horse.** The region contains both, so the old "Unsupported clan combat"
  guard would refuse startup. The starter plan sets an explicit accepted exclusion
  (`clanHostilityNotSimulated`): Goblins do not attack the Horse and nobody gains credit from
  that fight. The guard still fires for any other plan. **This needs the owner's acceptance**
  until durable clan combat (T4C-0225) lands.
- **Tests.** `HostedKillRegionTest` (no database) proves region minimality, Skeleton anchors,
  Sundial exclusion, unknown-monster failure, refused portals, and a footprint-aware BFS that
  shows the temple spawn, Aelric's talking range, all Skeleton melee approaches and portal 16
  are mutually reachable. `HostedTransferTest` gained a PostgreSQL walk-onto-refused-portal case
  (runs in CI) and its two local-coordinate door cases now derive the origin.
