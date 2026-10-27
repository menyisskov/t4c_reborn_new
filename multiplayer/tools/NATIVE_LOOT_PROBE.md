# Two-adapter native loot probe (T4C-0204 L12)

`NativeLootProbe.java` drives two actual `OnlineConnection` adapters, plus a third
adapter for the over-weight case, against a fresh `ServerMain --online` child that the
probe starts itself. It provisions disposable characters in a dedicated PostgreSQL
database and checks the committed result directly. It is native network evidence only.
It is not rendered-client evidence and does not complete M2.

## What the probe does

1. Starts its own server on a caller-selected loopback port and waits for `local-persistent` health.
2. Provisions three characters at the authored Witness Skeleton tile on the worldmap (world 0):
   a killer, a racer/observer and a weighted character. Each gets a warrior kit. The weighted
   character's inventory holds one stack of 1,000,000 of the first positive-weight authored item,
   so its carried weight exceeds any strength-based capacity.
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

## Commands

From the repository root, after the matching artifacts exist:

```powershell
$runtime = Join-Path $env:TEMP ('t4c-loot-runtime-' + [guid]::NewGuid().ToString('N'))
$output = Join-Path $env:TEMP ('t4c-loot-evidence-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $runtime -ErrorAction Stop | Out-Null
@('lib','assets/maps/worldmap','assets/maps/dungeon') |
  ForEach-Object { New-Item -ItemType Directory -Path (Join-Path $runtime $_) -ErrorAction Stop | Out-Null }
Copy-Item -LiteralPath 'multiplayer/server/target/t4c-server.jar' -Destination (Join-Path $runtime 't4c-server.jar')
Copy-Item -Path 'multiplayer/server/target/lib/*' -Destination (Join-Path $runtime 'lib')
Copy-Item -LiteralPath 'assets/maps/worldmap/worldmap.colbin' -Destination (Join-Path $runtime 'assets/maps/worldmap/worldmap.colbin')
Copy-Item -LiteralPath 'assets/maps/dungeon/DungeonMap.colbin' -Destination (Join-Path $runtime 'assets/maps/dungeon/DungeonMap.colbin')
$dependencies = (Join-Path $runtime 't4c-server.jar') + ';' + (Join-Path $runtime 'lib/*')
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
are the two hosted maps. The `client` copy from the restart probe is not needed here. The
probe's classpath check accepts only entries under the runtime snapshot.

## Assumptions to confirm on the first real run

- The Witness Skeleton tile `(3700.5, 1450.5)` on world 0 is walkable and within melee reach
  of a character placed there. The probe does not walk. If melee reports out of range, move the
  spawn constants (or add walking) and record the change. This is not yet verified.
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

None yet. This document is written before a recorded run. Record the run here with the
probe SHA-256, the server and client JAR hashes, the protocol and the report path only after an
actual exit-0 run.
