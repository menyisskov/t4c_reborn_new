# Two-adapter server restart probe (T4C-0207)

`NativeRestartProbe.java` uses two actual `OnlineConnection` adapters and two fresh
operator-provisioned accounts in a dedicated PostgreSQL database. It starts its own
`ServerMain --online` child on a caller-selected loopback port. Each character obtains
the authored opening letter. The probe compares the committed letter UUID, flag,
complete progress, gold, experience, position and revision through a private admin
shutdown (exit code 0 required), a restart, a forced kill of only its own child,
real database lease expiry and a second restart. It never clears a lease or replays
an ambiguous interaction. A short direction intent just before the forced kill has
no commit acknowledgement; the database record *after* the kill is the authoritative
recovery baseline. Graceful final checkpoints may legitimately increment revisions.

## Isolation and run

Coordinate a build/output read window first. Create a fresh copied runtime outside
the repository, containing one matching server JAR and its `lib` directory, one
matching desktop JAR and its libraries, and both hosted collision maps. Compile the
probe against this snapshot and run it from that same snapshot. Never use live
`target/classes` or a JAR that another build can replace during the run. Java 21 is
required. The runtime, probe output and each report directory must be create-only.
The tool refuses the shared 18765/18766 ports and any database name other than
`t4c_restart_*`; choose another confirmed-unused loopback port if 18767 is occupied.

Create a **new** database named `t4c_restart_<unique>` with your private database
administrator procedure, then set `T4C_DB_URL`, `T4C_DB_USER` and `T4C_DB_PASSWORD`
privately for that database. Do not place the password in shell history, arguments,
output or this document. The server performs the schema migration in the dedicated
database; the probe provisions two disposable accounts after health succeeds.
Do not stop PostgreSQL or a shared QA server. Leave the database and output for
inspection after failures; there is no automatic delete or reset.

From the repository root, after matching built artifacts are available:

```powershell
$package = 'target/online-client-packages/qa-0205-8da43482-b'
$runtime = Join-Path $env:TEMP ('t4c-restart-runtime-' + [guid]::NewGuid().ToString('N'))
$output = Join-Path $env:TEMP ('t4c-restart-evidence-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $runtime -ErrorAction Stop | Out-Null
@('lib','client','client-lib','probe','assets/maps/worldmap','assets/maps/dungeon') |
  ForEach-Object { New-Item -ItemType Directory -Path (Join-Path $runtime $_) -ErrorAction Stop | Out-Null }
Copy-Item -LiteralPath 'multiplayer/server/target/t4c-server.jar' -Destination (Join-Path $runtime 't4c-server.jar')
Copy-Item -Path 'multiplayer/server/target/lib/*' -Destination (Join-Path $runtime 'lib')
Copy-Item -LiteralPath (Join-Path $package 'app/T4C.jar') -Destination (Join-Path $runtime 'client/T4C.jar')
Copy-Item -Path (Join-Path $package 'lib/*') -Destination (Join-Path $runtime 'client-lib')
Copy-Item -LiteralPath 'assets/maps/worldmap/worldmap.colbin' -Destination (Join-Path $runtime 'assets/maps/worldmap/worldmap.colbin')
Copy-Item -LiteralPath 'assets/maps/dungeon/DungeonMap.colbin' -Destination (Join-Path $runtime 'assets/maps/dungeon/DungeonMap.colbin')
$dependencies = (Join-Path $runtime 'client/T4C.jar') + ';' + (Join-Path $runtime 't4c-server.jar') + ';' + (Join-Path $runtime 'lib/*') + ';' + (Join-Path $runtime 'client-lib/*')
javac -proc:none -cp $dependencies -d (Join-Path $runtime 'probe') multiplayer/tools/NativeRestartProbe.java
if ($LASTEXITCODE -ne 0) { throw 'Probe compilation failed' }
$probeClasspath = (Join-Path $runtime 'probe') + ';' + $dependencies
$javaExe = (Get-Command java -ErrorAction Stop).Source
# Set private T4C_DB_* variables for a newly created t4c_restart_* database here.
java -cp $probeClasspath com.perso.T4C.server.NativeRestartProbe $runtime $javaExe 18767 $output
if ($LASTEXITCODE -ne 0) { throw 'Restart probe failed; preserve the output and dedicated DB' }
Get-Content -LiteralPath (Join-Path $output 'report.txt')
```

The output directory must not exist when the probe starts. It contains a sanitized
`report.txt`, separate child stdout/stderr logs and private `control-*.token` files.
Treat the entire directory as private and never attach or publish the token files.
The report contains no account access keys, database password or admin token. It
records the letter UUID and a SHA-256 of complete committed progress for each
phase; the code compares the full progress records. A failed run retains its data
and reports only the failure stage/class; inspect private logs locally.

## Recorded run and limits

The final-source local pass finished at 11:53:57 Asia/Jerusalem on 2026-10-27.
The Java source SHA-256 was
`9F324B943D982B5D14F0FB20A60A5E3650DCE23020DF3E2D3894C091ECBA6AEA`.
It used base `8da43482`, protocol 4, schema 9 and committed progress version 1,
server JAR SHA-256 `38D6D00DAFCD4FB81D08476667C435BC6485149EA373FD3FAB8599DC8D903D4A`
and packaged desktop JAR SHA-256
`BC09B3328EDAB9EB0D34005E07E9FF325CDB8B6CC62AE57CE1247770AD3A2D1F`.
The tool exited 0 on isolated loopback port 18767 against the fresh
`t4c_restart_0207_final_03e1c342` database, without stopping the shared QA
realm. Both players retained separate letter UUIDs and their original flag, full
progress SHA-256, position and balances (gold 225/213; XP 0/0). Graceful final
checkpoints changed both revisions from 1 to 2, and the final stop changed both
from 2 to 3. The forced kill left both committed revisions at 2. Both leases
remained in use immediately after that kill; the probe waited 29,690 ms for
actual lease availability before rejoining. All three child stderr logs were
empty and port 18767 had no listener after completion. The private report is
`%TEMP%/t4c-0207-evidence-finalsource-03e1c342/report.txt`. Three earlier
successful isolated reports and databases were retained for inspection; this
final-source run is the delivery evidence.

This is two network adapters, not two rendered windows. It covers server-process
graceful/forced restart and actual lease expiry, not PostgreSQL outage/recovery,
backup restore, remote transport, general quest/loot replay or a sustained soak.
The opening letter is the one consequential mutation exercised. A tool pass is
evidence for this narrow behavior only; milestone acceptance still needs the
rendered two-client playthrough and the other checked gates in `MILESTONES.md`.
