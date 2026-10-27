# Twenty-player native network-adapter soak

This driver uses twenty instances of the desktop game's real `OnlineConnection`, including its
authentication, polling, commands, committed character refresh, corpse refresh, and logout paths.
It opens **no windows**. Its evidence is twenty native network adapters, **not twenty rendered clients**.
It never reads or writes single-player saves or preferences.

The current source targets protocol 4. It loads the server's exact `HostedRegionPlan.startingSlice`
allowlist (starting world and Lighthaven interior, portal routes 16/17), caches both collision crops,
and acknowledges each location epoch only after the matching map/snapshot metadata is validated.
Corpse recovery considers only corpses in the current world and crop. Tick/movement baselines reset
across committed location epochs; character revisions remain monotonic. Local map paths come from
the trusted hosted plan, never from network strings. Keep the world and dungeon collision files in
their normal sibling layout. It does not deliberately navigate a portal tour; use the dedicated
door probe to establish transitions, and inspect `worldTransfers` before claiming soak transfer coverage.

Already-running protocol 3 baselines use immutable copies of their original harness and runtime.
Do not replace those copies with this source or combine their results with protocol 4 evidence.

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
Each update writes a sibling `.tmp` file and replaces the prior complete JSON. On Windows,
another process can briefly hold the report without delete sharing. The writer retries only
that replacement access denial for up to two seconds; the prior report stays valid while it
waits. A persistent denial or interruption still fails the run. An earlier partial report,
even one with healthy metrics, is not evidence that the requested duration completed.
`NativeAdapterSoakReportProbe` exercises both a released and a persistent real Windows
`FileStream` lock against this replacement without starting a server or database.
For graceful operator cancellation, set `T4C_SOAK_STOP_FILE` to an initially absent private local
path before launch, then create that file to stop. All adapters close through their normal logout
path and the report remains `finished: false` when the requested duration did not complete.

For local operator runtime metrics, set `T4C_SOAK_CONTROL_FILE` to the private
shutdown-control token file before launching the wrapper. The harness accepts this
only with a loopback endpoint and reads the 64-hex token privately. It makes a
protected `GET /admin/metrics` request at startup and on each five-second report
cycle. Neither the token nor its path is printed or written to JSON. The optional
`serverRuntime` report section contains availability, sample/failure counts, and
initial/latest/maximum scalar snapshots. Without the variable it explicitly says
`available: false`; the ordinary harness still runs. Values include tick duration
and scheduler debt in nanoseconds, process CPU time in nanoseconds, JVM uptime in
milliseconds, and heap/non-heap usage in bytes. See [LOAD_METRICS.md](../LOAD_METRICS.md)
for field definitions and consistency limits.
Operator replies are capped at 64 KiB and the complete response has a two-second
deadline; a stalled or oversized response increments the sample failure count.
`NativeAdapterSoakDeadlineProbe` exercises a local HTTP peer that stalls after
headers and a partial body, without touching the QA server.

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
- Optional protected server runtime snapshots give internal tick timing/debt and JVM
  CPU/memory observations. They do not measure request bytes, network throughput,
  database operation latency or pool wait; these remain Beta measurement gaps.

`finished: true` means the requested timer ended and cleanup was requested, not that Beta passed.
Require zero invariant/connection errors, useful accepted gameplay and HP changes, successful
reconnects, and substantial time at twenty connected adapters; assess rejected counts and latency
against workload. The driver has no release threshold baked in. It does not verify rendered animation,
remote TLS, crash/restart recovery, quests, inventory UI, or map transfer. Those remain separate gates.
