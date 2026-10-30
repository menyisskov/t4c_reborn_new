# Two-adapter native loot probe (T4C-0204 L12)

`NativeLootProbe.java` drives two actual `OnlineConnection` adapters, plus a third
adapter for the over-weight case, against a fresh `ServerMain --online` child that the
probe starts itself. It provisions disposable characters in a dedicated PostgreSQL
database and checks the committed result directly. It is native network evidence only.
It is not rendered-client evidence and does not complete M2.

## What the probe does

1. Starts its own server on a caller-selected loopback port and waits for `local-persistent` health.
2. Provisions and joins ONE character first, the killer, on world 0 at a start tile that the probe
   derives itself (see "Start position" below). It has the full warrior starter kit equipped and a
   level derived from the data (see "Killer level" below). The other three characters (racer,
   weighted, second racer) do not exist until the drop they need exists (see "Positioning the other
   characters" below).
3. Checks that the server's protocol equals the client adapter's `OnlineProtocol.VERSION` (8 at T4C-0357; the old literal 5 was stale) and that the killer joined.
4. The killer fights Skeletons until a drop appears, each in turn: it retargets by DISTANCE (nearest
   live Skeleton, not the first in the snapshot), walks toward it with the adapter's movement
   direction when out of melee reach, and attacks inside reach. It drinks a carried healing potion
   below half health. Drops that appear while it is working are kept and reused for later stages
   (while younger than 10 minutes; ground loot lives 15). Each stage has a hard budget; a death or
   respawn, a target that does not die in 75 s, 30 s in reach with no accepted attack, or 90 s with no
   live Skeleton stops the run with a message instead of spinning.
5. Simultaneous claim race (first drop): a racer is provisioned beside the drop and joins; once the
   owner window has expired the killer and the racer submit `pickUp` for the same drop, released by one
   latch. Exactly one result must be accepted. The loser must be refused as `online.loot.taken` (server `loot_taken`, T4C-0307: another character's claim is committed), `online.loot.unavailable`
   (server `loot_unavailable`) or the adapter's generic `online.loot.refused` (server `busy` or a stale
   revision; the adapter maps those to the generic key). The database check below is the authority that
   exactly one claim happened.
6. Over-weight refusal (second drop): a weighted character is provisioned beside the drop; after the
   owner window its pickup must be refused as `online.loot.too_heavy` (server `too_heavy`). The drop must
   stay visible to the killer's snapshot.
7. Lost-response reconnect (third drop): a second racer is provisioned beside the drop, submits a
   pickup and closes its adapter at once. The probe rejoins with the same credentials and issues no
   replay. The database decides the outcome.
8. Graceful shutdown of the owned child, requiring exit 0, then a database check. Each loot row
   must match exactly: a `CLAIMED` row is held by exactly one character, which is its recorded
   claimant, and has exactly one claim receipt. Any other state is held by no character and has
   no receipt. The race drop must be `CLAIMED`. The over-weight drop must still be `ON_GROUND`.

Output is a sanitized `report.json` in the new output directory and the same JSON on stdout.
It contains stages, counts, refusal keys, loot item IDs, item keys, states and holder labels.
It contains no account or character IDs, secrets, tokens, database credentials or paths
outside the output directory. Failure exits with status 1 and records the stage and exception
class. Child server logs and the private `control-1.token` file stay in the output directory.

## Over-weight fixture

An earlier version replaced the weighted character's whole inventory with a 1,000,000-unit stack.
Equipped gear is an inventory instance in saved progress, so that unequipped the starter kit
(no worn weapon or armor, no equipment strength bonus). The probe now calls
`OnlineOverweightFixture` (server module, unit-tested by `OnlineOverweightFixtureTest`):

- The fixture item is the heaviest authored item that is droppable, not unique and has positive
  weight (ties broken by key), so the stack stays small. It is a real item from the bundled content.
- The quantity is the smallest stack that leaves carried weight strictly above the carry maximum:
  `maximum = CarryCapacityRules.maximumWeight(effective strength)` where effective strength is
  base strength plus equipment bonuses, and carried weight is every inventory instance times its
  quantity, exactly as `OnlinePickupRules` computes them. Nothing is retyped. Strictly above the
  maximum means any later pickup, even a zero-weight item, is refused as `too_heavy`.
- The stack is appended to the unchanged starter inventory. The equipment slots still point at
  the same instances. It goes in through the same character-provisioning path as every other probe
  character (`OnlineStore.create`); no server guard is weakened.
- `report.json` records `overweightFixtureItemKey`, `overweightFixtureQuantity`, `carryMaximum`,
  `carriedBeforeFixture` and `carriedWithFixture`. The probe fails before starting anything
  if the fixture does not exceed the maximum.

Expected result lines in `report.json` for this assertion: stage "over-weight refusal leaves the
loot", `overweightRefusal` equal to `too_heavy`, and in the database check the over-weight drop
`ON_GROUND`, held by no character, with no claim receipt.

The pure sizing was checked offline for every starter class. Finding: some other classes'
starter kits (for example the Archer's arrows) already weigh more than their own carry maximum;
the probe uses warriors, whose kit fits.

## Start position (hosted starter region)

`ServerMain --online` always hosts `HostedRegionPlan.startingSlice`: world 0 region
`[2735,2984) x [1029,1146)` (the temple crop plus the whole `renewed_wards` kill circle) and the
Lighthaven interior. There is no override flag or environment variable; the operator sets
nothing for the slice. Any join outside it is refused `409 unhosted_region`, so the old fixed
world-0 `(3700.5, 1450.5)` start could never reach a loot assertion. The server guards are unchanged.

Before it touches the port, the database or the server, the probe now runs `resolveStart`, which
uses the same sources as `HostedKillRegionTest`: the real `startingSlice` plan built from the
runtime's `worldmap.colbin`, the `renewed_wards` circle and the authored `Skeleton` spawns inside it.
It takes the first anchor (ordered by x, then y) that has a tile within one to two tiles which is
hosted, standable (`HostedRegionPlan.canStand`), not a portal source (enabled or refused), within
`PLAYER_MELEE_ATTACK_RANGE` and with combat line of sight (`PreviewMap.allowsCombat`). Only the killer
starts on that tile. If no anchor qualifies, the probe stops with an
`IllegalArgumentException` beginning `Precondition failed:` and does nothing else. The chosen start
tile, the anchor and the number of circle anchors are written to `report.json`.

On the content in this checkout (offline dry check of `resolveStart` only; no server, port or
database used) it resolves to start `(2767.5, 1086.5)` beside the anchor `(2767, 1087)`, one of 13
circle anchors. That is informative, not a hard-coded input: the probe recomputes it each run.

## Killer level

The `renewed_wards` quest has `minLevel` 0, so the data does not say which level should fight its
Skeletons. The Skeleton is level 4 with 69 health, hits for 4 to 10 and has 13 anchors a few tiles apart
inside the 8-tile aggro range, so two can engage at once. A level-8 killer (about 91 health) died after two kills
(run 3). Since T4C-0357 the probe provisions every character at level 30 (`-Dloot.probe.level` overrides it; never below
twice the monster level), as the live rendered runs T4C-0318/0331/0341 did, with a 20-stack of Healing Potions (charges 1,
which the server's item-use rule requires) so the drink-below-half routine lasts. The starter ground stays lethal for new
characters exactly as single-player (T4C-0248); this only picks the probe's killer. It prints `skeletonLevel`, `questMinimumLevel`
and `probeCharacterLevel`. This is a probe headroom choice, not a balance statement.

How: `levelUp` feeds the server's own award path (`OnlineProgression.award`, the code a kill runs) in
memory until the level is reached, so health and unspent points come out as a player's would. The
character is then created through `OnlineStore.create` as before, and the matching cumulative XP is
stored in its `experience` column in the same throwaway database before it ever joins (the create call
has no experience argument; level 8 with 0 XP would be refused `invalid_progression` on the first award).
Both bounds of the server's own check hold by construction. Nothing is changed at runtime and no server
guard is touched.

## Positioning the other characters

Pickup range is 8 tiles (Chebyshev, `OnlinePickupRules.MAX_TILE_DISTANCE`) and drops land on the dead
Skeleton's tile. The racers and the weighted character are created through the same `create` path on a
standable, hosted, non-portal tile within 6.5 tiles of their drop, chosen as the farthest of those from
every live Skeleton, so no walk is needed. While the owner window (60 s) runs, the killer stays within
5 tiles of the drop and fights only what is in reach, and the new character fights back only what is in
reach. Each stage checks both claimants are within 7 tiles before it claims.

## Diagnostics

`report.json` carries `attackAndCommandOutcomes` (counts) and `diagnosticSamples` (the first six of each
kind): every attack, potion and pickup result, with distance, health and the adapter's key. The adapter
maps no refusal reason for `/attack` (`OnlineCommandRefusals` returns null for it), so a refused attack is
recorded as `refused_reason_not_exposed_by_adapter`; the raw server code is not observable from the probe.
A client-side mapping for `/attack` would make the next run self-explanatory (follow-up, outside this
probe-only change).

## Isolation requirements

- Use a runtime snapshot outside this checkout: one matching server JAR with its `lib` directory,
  and both hosted collision maps. Never run from live `target/classes`.
- Use a new loopback port other than 18765 and 18766. Reserve it and confirm it is unused. It
  must not be the QA realm port.
- Use a NEW database named `t4c_loot_<unique>`. The probe refuses any other name, including
  `t4c_online` and `t4c_test`. Create the database with your own private administrator procedure.
  Set `T4C_DB_URL`, `T4C_DB_USER` and `T4C_DB_PASSWORD` privately. Never put the password in shell
  history, arguments, logs or this document.
- Do not stop PostgreSQL or a shared QA server. Do not clear a lease. Keep the database and output
  for inspection after a failure. The probe performs no drops or deletes.
- Coordinate the build and runtime windows in `multiplayer/PEER_WORK.md` before running.

### Run prerequisites (operator checklist)

1. A claimed PEER_WORK window for the dedicated database and port (request text below).
2. A fresh database `t4c_loot_<id>` (lowercase letters, digits, underscore; the probe's URL check is
   `jdbc:postgresql://host:port/t4c_loot_[a-z0-9_]+`). Never `t4c_online`, `t4c_test`, or a database
   another task owns. The probe's child server migrates the schema itself.
3. `T4C_DB_URL`, `T4C_DB_USER`, `T4C_DB_PASSWORD` set privately. `T4C_TLS_*`, `T4C_PUBLIC_ORIGIN` unset
   and `T4C_BIND_ADDRESS` unset or `127.0.0.1`. `T4C_CONTROL_FILE` is set by the probe, not the operator.
4. A dedicated loopback port taken from the register, never 18765, 18766, 18770 or 18771 (the probe
   refuses 18765 and 18766 itself; the others belong to retained QA/soak/pursuit work).
5. No server slice setting is needed. Confirm the runtime snapshot's `assets/maps/dungeon/DungeonMap.colbin`
   is copied beside the world map, because `startingSlice` reads it for the Lighthaven region.
6. Run from a repository root whose current directory is not inside the runtime or output paths.

### Ready-to-paste PEER_WORK request

```text
- <date time zone>: <agent> requests a database/port run window for NativeLootProbe (T4C-0204 L12,
  two-adapter ground-loot QA; probe start now derived inside the hosted starter region by T4C-0235).
  Needs: NEW PostgreSQL database t4c_loot_<id> on the shared PG service (create-only, no existing
  database touched), dedicated loopback port <port> from the register (not 18765/18766/18770/18771),
  one throwaway runtime snapshot and output directory under TEMP, one ServerMain --online child owned
  by the probe, up to 20 minutes. No owner QA, t4c_online, t4c_test, root Maven outputs or lease clearing.
  Evidence: sanitized report.json kept; database and output preserved after any failure. Releases the
  port and window on completion; will record the result and exact probe/JAR hashes in the register.
```

## Commands

From the repository root, after the matching artifacts exist:

```powershell
$runtime = Join-Path $env:TEMP ('t4c-loot-runtime-' + [guid]::NewGuid().ToString('N'))
$output = Join-Path $env:TEMP ('t4c-loot-evidence-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $runtime -ErrorAction Stop | Out-Null
@('lib','assets/maps/worldmap','assets/maps/dungeon') |
  ForEach-Object { New-Item -ItemType Directory -Path (Join-Path $runtime $_) -ErrorAction Stop | Out-Null }
New-Item -ItemType Directory -Path (Join-Path $runtime 'client') -ErrorAction Stop | Out-Null
Copy-Item -LiteralPath 'target/T4C-1.0-SNAPSHOT.jar' -Destination (Join-Path $runtime 'client/T4C.jar')
Copy-Item -LiteralPath 'multiplayer/server/target/t4c-server.jar' -Destination (Join-Path $runtime 't4c-server.jar')
Copy-Item -Path 'multiplayer/server/target/lib/*' -Destination (Join-Path $runtime 'lib')
Copy-Item -LiteralPath 'assets/maps/worldmap/worldmap.colbin' -Destination (Join-Path $runtime 'assets/maps/worldmap/worldmap.colbin')
Copy-Item -LiteralPath 'assets/maps/dungeon/DungeonMap.colbin' -Destination (Join-Path $runtime 'assets/maps/dungeon/DungeonMap.colbin')
$dependencies = (Join-Path $runtime 'client/T4C.jar') + ';' + (Join-Path $runtime 't4c-server.jar') + ';' + (Join-Path $runtime 'lib/*')
javac -proc:none -cp $dependencies -d (Join-Path $runtime 'probe') multiplayer/tools/NativeLootProbe.java
if ($LASTEXITCODE -ne 0) { throw 'Probe compilation failed' }
$probeClasspath = (Join-Path $runtime 'probe') + ';' + $dependencies
$javaExe = (Get-Command java -ErrorAction Stop).Source
# Set private T4C_DB_* variables for a newly created t4c_loot_* database here.
java -cp $probeClasspath com.perso.T4C.server.NativeLootProbe $runtime $javaExe <unused-port> $output
if ($LASTEXITCODE -ne 0) { throw 'Loot probe failed; preserve the output and dedicated DB' }
Get-Content -LiteralPath (Join-Path $output 'report.json')
```

The probe is compiled against the copied runtime, not the live build. The `assets/...` copies
are the two hosted maps. `client/T4C.jar` supplies `OnlineConnection`; the server JAR alone does
not contain it. The probe's classpath check accepts only entries under the runtime snapshot.
The probe's compile check was done against these three artifacts; the probe itself was not run.

## Assumptions to confirm on the first real run

- Walking and distance retargeting now exist and worked in run 3 (two Skeletons were killed), but the
  killer did not survive the third engagement. Whether any single fighter survives long enough for three
  drops is open (see the recorded run 3 and the owner question).
- Skeleton drops are random: the authored table rolls about 9% per kill in total (0.03 + 0.01 + 0.02 +
  0.03 + 0.0007), so three drops need about 33 kills on average. Each drop wait is capped at 14 minutes
  and the whole run at 40; a run that runs out fails with an explicit stage and the kill count.
- `OnlineCommandRefusals` maps pickup codes to `online.loot.*` keys. The earlier assertions compared the
  raw server codes and could never have matched; they now compare the mapped keys.
- The lost-response case is client-side: the adapter closes right after the request is sent.
  Whether the server answered before the close is recorded but not required. The database
  decides the outcome, and either a single claim or no claim is a pass. A duplicate is a failure.

## Not proven

- The rendered client: no window is opened, no loot sprite is checked and no click is made.
- Restart or PostgreSQL crash recovery during a claim. Use the restart and database recovery
  probes for those.
- Corpse carry-weight parity (L11), which needs the owner's decision, and the M2 two-client
  rendered acceptance gate in `MILESTONES.md`.
- A sustained soak, remote transport or a complete loot economy.

## Recorded run

First live use, 2026-10-28, against a disposable PostgreSQL 16 cluster from the acceptance cluster
tooling ([ACCEPTANCE_CLUSTER.md](ACCEPTANCE_CLUSTER.md), T4C-0247) on loopback 56871, database
`t4c_loot_<hex>` created inside the cluster (the cluster's own `t4c_acc_` database is refused by the
probe name check), probe server on 18791. Local PG16 evidence only. Both runs FAILED; no loot
assertion group was reached. The cluster was destroyed afterwards; the sanitized `report.json` files
and server logs stayed under a TEMP evidence directory (not committed).

Environment: probe SHA-256 before the join fix `21DEBD9CDA2A5A60D593CB428F0C2E1F901A8797C11A0865FB2F939C27123DB0`;
`client/T4C.jar` `CDD79B455CB8CC0EB5BB6308852BF3656F6ED1D5EBC604E6ECD2EE325D76C518`; `t4c-server.jar`
`9ACBF52366FBF6ACCE69BF245DA6B34105ABC24F3B83844B4F76E9ACDECF7F71`; protocol 5; start tile (2767.5, 1086.5),
anchor (2767, 1087), 13 circle anchors; overweight fixture `item.asile_chest` x1, carry maximum 145,
carried 55 before and 10055 with the fixture.

- **Run 1: failed at "join native adapters and check protocol"** ("Server rejected request: 503").
  Root cause (probe-only): the probe opened all three `OnlineConnection` adapters at once, but the
  server admits at most two concurrent `/join` requests (`HttpRequestAdmission.MAX_PUBLIC` = 2) and
  answers the third `503 busy`. Fixed in the probe: adapters now join one at a time. No server guard changed.
- **Run 2 (the single allowed rerun): failed at "kill skeletons until drops appear and observer sees them"**
  ("Skeleton was not killed before the deadline", after the full 20 minutes). Joins and the protocol-5 check
  passed. Read-only inspection of the database afterwards: no skeleton was killed (all three characters
  have 0 XP, no ground loot, no corpses). 49 combat events were recorded in three consecutive
  ~12 second windows, one per character, all within about 35 seconds of the join, and all three characters
  ended at their saved respawn point (2951, 1038), not the start tile. Reading: the three level-1 warriors
  (33 to 38 HP) stacked on the anchor tile were defeated one after another by the patrolling Skeletons and
  respawned about 200 tiles away; from then on `kill` targets the first live Skeleton in the snapshot
  regardless of distance, swallows every attack refusal, and never walks, so it spun until the deadline.
  This was inferred from persisted state, not from a logged refusal reason.
- Assertion groups: join and protocol passed on run 2 (failed on run 1); kill and loot drop, owner window,
  first-claim race, over-weight refusal, expiry and lost response were NOT reached. The database
  check and the exit-0 shutdown assertion were not reached either.

Follow-up taken in the next change: refusal logging, a lone killer, a data-derived level, distance
retargeting and walking, bounded stages (all described above). Run 3 is the result.

### Run 3 (T4C-0245, one rerun on a new disposable cluster): FAILED at the kill stage

Same method as run 2, on a new PostgreSQL 16 cluster (loopback 56872, extra database
`t4c_loot_<same hex>`, probe server 18792), destroyed afterwards. The runtime snapshot was removed; the
sanitized `report.json` and server logs stayed under a TEMP evidence directory (not committed). The
`createdb` call inside the cluster needs the cluster runtime's `LD_LIBRARY_PATH` (as `acceptance-cluster.sh`
sets), otherwise it fails with a missing `libpq.so.5`.

- Provisioning: one killer, level 8 (Skeleton level 4 x 2, quest minimum 0), 137,420 XP seeded, 91 maximum
  health, full starter kit. Join and the protocol-5 check passed.
- Kill stage: the killer walked to and killed 2 Skeletons (586 XP gained), using 3 healing potions, then was
  defeated and respawned at the saved respawn tile (2951, 1038) with 45/91 health, about 60 seconds after
  joining. The probe stopped at once with `killer was moved far away, presumed defeated and respawned ...
  kills=2 acceptedAttacks=31` instead of spinning. No drop appeared (nothing in `online_ground_loot` or
  `online_corpses`), so no loot assertion was reached and no racer, weighted or second racer was created.
- Attack outcomes: 31 accepted, 59 refused with no reason exposed by the adapter (the probe attacks every
  300 ms, so most are very likely cooldown refusals; all had `inReach=true` at 25 to 36 px). This confirms
  the run-2 reading: fighting works, the problem is survival, not reach or a refused attack path.
- Assertion groups: join and protocol passed; kill/drop, owner window, first-claim race, over-weight
  refusal, expiry, lost response, database check and exit-0 shutdown NOT reached.

Owner question (balance, not a probe fault): a level-8 warrior with a recruit's greatsword, leather kit and
potions lost about 45 of 91 health killing two Skeletons and died to the next. A fresh level-1 warrior has
33 to 38 health. Can a player realistically work through the 12-kill `renewed_wards` quest at its (zero)
minimum level, and at what level should it start? The quest and the starter ground are authored for it, and the
Skeletons are aggressive with 13 anchors inside each other's aggro range. The probe cannot make the loot
assertions meaningful without a killer that survives, and giving it a higher level would hide the question.

## Movement rule change (T4C-0288)

The server moves players with single-player's grid-reservation rule (PR #234): a released key finishes its
tile, there is no mid-tile reversal, and blocked steps slide. The probe's `steer` now picks its direction relative
to the tile the player is arriving on (the last direction sent) with a half-tile dead band, so the stop-at-once
assumption no longer makes it oscillate around a target. It compiles against the current runtime; it was **not run**
for this change (the Skeleton ground is lethal for new characters, T4C-0248).

## Run 4 (T4C-0357): PASS, native adapters, throwaway Docker PostgreSQL 18.1

Owner-approved window on 2026-10-30. Image `postgres:18.1` (pinned, digest
`sha256:1090bc3a8ccfb0b55f78a494d76f8d603434f7e4553543d6e807bc7bd6bbd17f`; CI uses the floating `postgres:18`), one container
published only on 127.0.0.1:58431 with a generated per-run password held in process environment only, database
`t4c_loot_397a09f6daf3` (the probe's name guard untouched), `ServerMain --online` on 18981 started and gracefully stopped by
the probe (exit 0). Protocol 8. Server JAR SHA-256 `8FFF1D33EE26E82B7EA4D06292CAD1FBE77618FB1CE19E6FB4BA7EB28E2DC569`, client JAR
`A503A842FB35EB0BAC7940EBEBB8AE516C8D93723BB5C3178D7D27190742C778`, passing probe `151494FE7249BC9B6546A3FA72BF6E0465420A0A5A6ABF0A596974AFD23F91F2`.
Container, anonymous volume and TEMP runtime removed afterwards; the image stays.

Getting there took four probe-only repairs, each found by a failed attempt on the same container setup (a fresh container per
attempt): (1) the literal protocol-5 assertion was stale (server and client are at 8); (2) the killer is level 30 with 262 HP
instead of level 8; (3) the race-loser key set predated T4C-0307, so a loser correctly refused as `online.loot.taken` failed
the run; (4) the probe's own potion stack had charges 0, which the server's item-use rule refuses as `unsupported_item`, so
the killer ran out of working potions and died after 12 kills in the attempt before the final one. That attempt did pass
the race and over-weight assertions. No server code, balance or assertion was weakened; the database authority check is unchanged.

Result of the passing run (6 min 18 s, 13 Skeleton kills, 3 drops, killer at 5,800,350 XP):

- Drop observed by a second adapter: yes (`observerSawFirstDrop`).
- Simultaneous claim after the 60 s owner window: killer won, the racer was refused `online.loot.taken`. Database: that drop
  `CLAIMED`, held by exactly one character (the killer), one claim receipt.
- Over-weight refusal: a level-30 warrior carrying the `item.asile_chest` fixture (carried 10,095 against a maximum of 145)
  was refused `online.loot.too_heavy`; the drop stayed `ON_GROUND`, no holder, no receipt.
- Lost response: a second racer sent a pickup and closed its adapter at once; it rejoined with the same credentials and the
  probe issued no replay. The server had answered before the close (`lostResponseAnsweredBeforeClose` true) and the drop
  ended `ON_GROUND`, no holder, no receipt (no duplicate).
- Graceful shutdown of the owned child with exit 0, then the database check above: all three rows matched exactly.
- Attacks: 118 accepted (157 busy and 89 cooldown refusals come from the 300 ms attack loop), 5 no-target; 7 potions accepted.

Not proven: the rendered client, a server restart during a claim (covered by `NativeLootFaultDrill`), the CI PostgreSQL
service (local Docker 18.1 only; CI stays the authority), corpse weight parity (L11).

Finding for the live-run lane (not fixed here): `LiveRunProvision.makeWithHeadroom` also adds its healing-potion stack with
charges 0, which the server refuses as `unsupported_item`. The probe was fixed; the provisioning script was not touched.
