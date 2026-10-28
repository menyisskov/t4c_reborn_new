# Two-adapter native loot probe (T4C-0204 L12)

`NativeLootProbe.java` drives two actual `OnlineConnection` adapters, plus a third
adapter for the over-weight case, against a fresh `ServerMain --online` child that the
probe starts itself. It provisions disposable characters in a dedicated PostgreSQL
database and checks the committed result directly. It is native network evidence only.
It is not rendered-client evidence and does not complete M2.

## What the probe does

1. Starts its own server on a caller-selected loopback port and waits for `local-persistent` health.
2. Provisions three characters on world 0 at a start tile that the probe derives itself (see
   "Start position" below): a killer, a racer/observer and a weighted character. Each gets the full
   warrior starter kit and keeps it equipped, so all three can fight and their equipment strength
   bonuses apply. The weighted character gets one extra inventory stack, the over-weight fixture
   (see "Over-weight fixture" below), appended to the kit and never replacing it.
3. Checks that the protocol is 5 and that the adapters joined.
4. Kills Skeletons with the killer's `attack` until at least three loot drops appear. The observer's
   snapshot must show every drop.
5. Simultaneous claim race: after the first drop's owner window has expired, the killer and the
   racer submit `pickUp` for the same drop, released by one latch. Exactly one result must be
   accepted. The loser must be refused as `loot_unavailable` or `busy`.
6. Over-weight refusal: after the second drop's owner window, the weighted character's pickup must
   be refused as `too_heavy`. The drop must stay visible to the observer.
7. Lost-response reconnect: the racer submits a pickup for the third drop and closes its adapter at
   once. The probe rejoins with the same credentials and issues no replay. The database decides
   the outcome.
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
`PLAYER_MELEE_ATTACK_RANGE` and with combat line of sight (`PreviewMap.allowsCombat`). All three
characters start on that tile. If no anchor qualifies, the probe stops with an
`IllegalArgumentException` beginning `Precondition failed:` and does nothing else. The chosen start
tile, the anchor and the number of circle anchors are written to `report.json`.

On the content in this checkout (offline dry check of `resolveStart` only; no server, port or
database used) it resolves to start `(2767.5, 1086.5)` beside the anchor `(2767, 1087)`, one of 13
circle anchors. That is informative, not a hard-coded input: the probe recomputes it each run.

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

- The derived start tile is within melee reach of its anchor Skeleton, but the Skeleton is a live
  creature: it may have patrolled or may aggro and fight the characters. The probe does not walk, so
  after the anchored Skeleton dies, later targets must be respawns or Skeletons that wander into
  reach. If the run stops at the kill cap or never finds a target, add walking or per-anchor starts
  (a follow-up) instead of relaxing any server guard. Not yet verified.
- All three characters start on the same tile; whether the server accepts stacked joins is untested.
- Skeleton drops are random. The probe kills up to 30 Skeletons until three drops appear. A run
  that stops at the kill cap fails with an explicit stage.
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

Follow-up needed before another run (not done here): make the probe diagnosable (record attack refusal
reasons instead of ignoring them), stop stacking weak characters on one tile (start the killer alone, or give
it enough health or a safer approach), and walk or re-target by distance. Whether level-1 warriors can beat
a Skeleton at all is a balance question for the owner, not for this tool.
