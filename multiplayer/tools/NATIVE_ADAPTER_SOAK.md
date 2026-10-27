# Twenty-player native network-adapter soak

This driver uses twenty instances of the desktop game's real `OnlineConnection`, including its
authentication, polling, commands, committed character refresh, corpse refresh, and logout paths.
It opens **no windows**. Its evidence is twenty native network adapters, **not twenty rendered clients**.
It never reads or writes single-player saves or preferences.

Build the matching desktop and multiplayer packages first, then generate `target/classpath.txt`
using `mvn dependency:build-classpath -Dmdep.outputFile=target/classpath.txt`. Start an isolated
persistent QA server with no other connected players. Use a disposable QA database; provisioning
adds twenty accounts and characters, and combat changes its shared creature state. No teardown
deletes records. Keep QA data for debugging, or retire the dedicated database separately.

From the repository root, with `T4C_DB_URL`, `T4C_DB_USER`, and `T4C_DB_PASSWORD` set for that QA
database (never put passwords on a command line):

```powershell
./multiplayer/tools/run-native-adapter-soak.ps1 -Port 18765 -Seconds 180 -ServerProcessId 12345
```

First run a three-minute smoke check, then inspect its report before a two-hour run:

```powershell
./multiplayer/tools/run-native-adapter-soak.ps1 -Port 18765 -Seconds 7200 -ServerProcessId 12345
```

Fresh credentials go through the existing owner-private provisioner into ignored
`multiplayer/local/soak-<run>/player-01.json` through `player-20.json`. They never appear in output
or the report. To resume those same isolated characters, pass `-Profiles <that-directory>`.
The default JSON report is under ignored `target/`, updated every five seconds and at exit.
Existing report files are rejected. Run this wrapper in a hidden background PowerShell process
with redirected logs when unattended work is required; preserve its PID and report path.

The driver routes movement through the real collision map, distributes targets across available
creatures, rotates assignments every thirty seconds, attacks at bounded intervals, prioritizes
its own corpse recovery, and reconnects players on staggered schedules after two minutes.
Normal rejected gameplay intents (cooldowns, contention, stale revisions) are counted separately
from connection failures. Shared creature lifetimes mean attacks may decrease while all targets
await their authored respawn; the report must be inspected rather than assuming useful combat ran.

The report records:

- Time spent at each connected-adapter count, including startup and intentional reconnect gaps;
  maximum players seen in snapshots does **not** prove sustained twenty-player occupancy.
- Per-player attack/recovery submissions, accepted/rejected commands, reconnects, movement and
  HP changes, and bounded command-latency histograms. Latency includes adapter polling/refresh.
- Owner identity, monotonic character revisions and session ticks, valid health, collision-safe
  positions, and stalled-snapshot checks. It does not assert equality between snapshot HP and
  owner HP read in a later request, which could legitimately span a commit.
- `/health` mean latency and observed tick rate. Tick deficit is a wall-clock proxy against
  34 Hz, **not internal scheduler debt**; server restarts invalidate this measure.
- Optional server CPU time from the supplied PID; `-1` means unavailable. Verify the PID belongs
  to the isolated server. Optional PostgreSQL database-wide size, connections, commits, rollbacks,
  and deadlocks at start/end; these include other workloads on that database. Without DB environment
  variables, those metrics are marked unavailable.

`finished: true` means the requested timer ended and cleanup was requested, not that Beta passed.
Require zero invariant/connection errors, useful accepted gameplay and HP changes, successful
reconnects, and substantial time at twenty connected adapters; assess rejected counts and latency
against workload. The driver has no release threshold baked in. It does not verify rendered animation,
remote TLS, crash/restart recovery, quests, inventory UI, or map transfer. Those remain separate gates.
