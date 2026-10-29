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
complete initial/latest snapshots plus maximum scalar fields. Arrays in `max`
remain from the initial sample; use `latest` for current HTTP rows and histograms.
Without the variable it explicitly says
`available: false`; the ordinary harness still runs. Values include tick duration
and scheduler debt in nanoseconds, process CPU time in nanoseconds, JVM uptime in
milliseconds, and heap/non-heap usage in bytes. See [LOAD_METRICS.md](../LOAD_METRICS.md)
for field definitions and consistency limits.
Operator replies are capped at 1 MiB and the complete response has a two-second
deadline; a stalled or oversized response increments the sample failure count.
The finite cap covers the measured 434,724-byte dense upper schema for all 897
fixed route/method/status rows with every numeric value at `Long.MAX_VALUE`.
The previous 64 KiB cap could reject plausible long-lived operator snapshots;
it was not a server response limit. `NativeAdapterSoakDeadlineProbe` uses owned
ephemeral loopback peers to check a stalled partial body, a large valid HTTP
traffic snapshot retained in `initial`/`latest`, and refusal above 1 MiB.

The driver routes movement through the real collision map, distributes targets across available
creatures, rotates assignments every thirty seconds, attacks at bounded intervals, prioritizes
its own corpse recovery, and reconnects players on staggered schedules after two minutes.
Adapter opens are paced (T4C-0250): the server admits only two concurrent public requests
(`HttpRequestAdmission.MAX_PUBLIC`) and answers the rest `503 busy`, so the driver opens one adapter at
a time with at least 500 ms between opens, initially and for every reconnect (`OpenGate`; the values are
reported under `openPacing`). The real `OnlineConnection` also retries a definite `503 busy` on the
anonymous `/map` read, on `/join` and on the post-join `/map` read, up to six times with jittered exponential
backoff (about 125 to 250 ms rising to 1.5 to 3 s, under ten seconds in total). It never retries a gameplay
command or a poll. An adapter whose worker ends (for example when those retries run out) is reported
immediately as `adapter_stopped_before_connect` or `adapter_stopped_after_connect` and reopened after the
usual three seconds, instead of waiting for the old fifteen-second timeout; a stuck "connecting" adapter
still reports `connection_or_reconnect_timeout`. Each player row records `firstConnectedMillis`. The pacing
helper has a headless self-check: run the soak source with the single argument `--self-test` (same
classpath as the wrapper). Normal rejected gameplay intents (cooldowns, contention, stale revisions) are counted separately
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
- Optional protected server runtime snapshots give internal tick timing/debt, JVM
  CPU/memory and bounded HTTP application body-byte/lifetime observations. They do
  not measure wire bandwidth, database operation latency or pool wait; these remain
  Beta measurement gaps.

`finished: true` means the requested timer ended and cleanup was requested, not that Beta passed.
Require zero invariant/connection errors, useful accepted gameplay and HP changes, successful
reconnects, and substantial time at twenty connected adapters; assess rejected counts and latency
against workload. The driver has no release threshold baked in. It does not verify rendered animation,
remote TLS, crash/restart recovery, quests, inventory UI, or map transfer. Those remain separate gates.

## Recorded run

### Smoke, PG16 WSL, 12 minutes, not the two-hour gate (T4C-0247, 2026-10-28): FAILED the tool's own zero-error criterion

Local smoke on a disposable PostgreSQL 16 cluster (acceptance cluster tooling, loopback 56881, database
`t4c_acc_<hex>` used directly because the soak has no database naming rule), a TEMP runtime snapshot of the
server JAR plus both hosted maps, and one `ServerMain --online` child on 18793 started with
`T4C_CONTROL_FILE`. Source: `origin/main` f7bb21c1, protocol 5. Scale: the tool's fixed twenty adapters
(it accepts no smaller count) for 720 seconds, i.e. the documented 3-minute smoke stretched to 12 minutes.
The wrapper exited 1 ("Adapter soak failed") because of connection failures; the report itself ran to
completion (`finished: true`, 720.011 s). The cluster was destroyed and the TEMP runtime removed afterwards;
the sanitized report stays in TEMP, not committed. Local PG16 evidence only.

| Metric | Result |
| --- | --- |
| Time at N connected adapters | 20: 214.2 s (29.8%), 19: 12.6 s, 18: 124.4 s, 16: 60.4 s, 10: 65.1 s, 8: 72.6 s, 6: 51.2 s, 2: 47.3 s, remainder small; at least 19: 226.8 s |
| Players seen in one snapshot (max) | 20 (reached late; 16 by about minute 5) |
| Adapter failures | 80 in total; the only error text was `connection_or_reconnect_timeout` (18 players; players 8 and 14 had none); no invariant, stall, position or revision errors |
| Reconnects | 156 |
| Attacks / recoveries | 713 attacks, 9 corpse recoveries (so at least nine adapter deaths; the report has no death counter) |
| Commands accepted / rejected | 438 / 284 (cooldown, contention, stale revision) |
| HP changes / movement observations | 1,230 / 67,091 |
| World transfers | 0 (not exercised) |
| Command latency buckets | `<=100` ms 144, `<=250` ms 550, `<=500` ms 28; nothing slower (722 commands) |
| `/health` | 0 failures, mean 0.664 ms, observed tick rate 33.989 Hz (`tickDeficitProxy` 8.238) |
| Server ticks | 25,385 completed, 0 failed, mean 2.53 ms, max callback 182.9 ms, 1 over-budget callback, 1 deadline reset, max deadline debt 215.7 ms |
| Memory | peak heap used 604.1 MiB (committed 1,024 MiB, 28 processors), non-heap peak 44.4 MiB |
| CPU | about 20% of one core on average (143.9 s process CPU) |
| Operator metrics samples | 144 successful, 0 failed |
| Database (database-wide) | connections 3 at start, 6 at end; commits 1,175 to 46,649; rollbacks 0 to 0; deadlocks 0; size 8.37 to 9.80 MB |
| HTTP | 179,932 requests completed, 1.363 GB response body written; non-2xx: `/map` 503 x61, `/join` 503 x19, `/attack` 409 x277, `/input` 409 x15, `/recover` 409 x5 |
| Shutdown | private `/admin/shutdown` answered 200 and the server process exited (exit code not captured) |

Failure analysis. The tool opens all twenty adapters at once, but the server admits at most two concurrent
public requests (`HttpRequestAdmission.MAX_PUBLIC` = 2, added by T4C-0222 after this tool was written) and
answers the rest `503 busy`. Eighteen adapters timed out on their first join, and the server counted 61 `/map`
and 19 `/join` 503s, which matches. They recovered through the tool's staggered reconnect schedule (first
reconnect at 120 s plus 6 s per player), so sustained twenty-adapter occupancy only began late and covers 29.8% of
the run, not a stable plateau. This is read from counters and code, not from per-request logs. It is **not a
server defect**: the admission cap is a deliberate guard and no server code was changed. It is a tool and scale
mismatch (twenty simultaneous joins and reconnect bursts against a two-wide public admission). Follow-up, not
done here: stagger the initial opens and reconnects, or retry on 503, before any two-hour run. Whether two
concurrent public requests is enough for a real twenty-player realm is a separate question.

Limits. The starter ground is lethal for new characters (DESIGN_GUIDELINES.md T4C-0248, an open owner decision):
the nine recoveries show deaths happened, and the movement/attack/recovery/reconnect mix is partly shaped by
survival there. Nothing was worked around. No bandwidth, wire-level, database-operation or pool-wait latency was
measured. Quests, loot, travel, rendered clients and TLS were not exercised. This does **not** pass the two-hour
soak, the M5 gate or Beta. Per ACCEPTANCE.md the Beta soak gate is two real rendered clients; twenty-adapter runs
are follow-up scale evidence.

### Paced-open rerun, PG16 WSL, 12 minutes, not the two-hour gate (T4C-0250, 2026-10-28): passed the tool's zero-error criterion

Same setup as the first smoke (fresh disposable PostgreSQL 16 cluster on loopback 56891, TEMP runtime snapshot,
one `ServerMain --online` child on 18794 with a private control file, twenty adapters, 720 s), run from the
T4C-0250 branch off `origin/main` abecaaf0 (protocol 5): one-at-a-time paced opens plus the client's busy retry.
The wrapper exited 0, the report ran to completion (`finished: true`, 720.045 s), the cluster was destroyed and the
TEMP runtime and provisioned profiles removed. Local PG16 evidence only.

| Metric | First smoke (T4C-0247) | Paced rerun (T4C-0250) |
| --- | --- | --- |
| Time at 20 adapters | 214.2 s (29.8%) | 406.5 s (56.5%) |
| Time at 19 or more | 226.8 s | 702.0 s (97.5%) |
| All twenty first connected | late, after the 120 s+ reconnect schedule | 20 of 20 within 0.2 to 0.5 s of their own open; all up in about 11 s |
| Adapter failures | 80 (`connection_or_reconnect_timeout`, 18 players) | 0 |
| Reconnects (scheduled, every 120 s per player) | 156 | 96 |
| Server 503 responses (`/map`, `/join`) | 61 and 19 | none recorded (no 503 row in the HTTP table) |
| Attacks / recoveries | 713 / 9 | 932 / 14 |
| Commands accepted / rejected | 438 / 284 | 600 / 346 |
| HP changes / movement observations | 1,230 / 67,091 | 1,582 / 93,786 |
| Command latency buckets (ms) | `<=100` 144, `<=250` 550, `<=500` 28 | `<=250` 279, `<=500` 665, `<=1000` 2 (946 commands) |
| `/health` | mean 0.664 ms, 33.989 Hz | mean 0.494 ms, 34.001 Hz, 0 failures, tick deficit 0 |
| Ticks | 25,385 ok, 0 failed, max callback 182.9 ms, 1 over budget | 24,742 ok, 0 failed, max callback 39.0 ms, 2 over budget, 0 deadline resets, max deadline debt 52.9 ms |
| Peak heap used | 604.1 MiB | 645.5 MiB (committed 1,024 MiB) |
| CPU | about 20% of one core | about 24% of one core (175.2 s over 720 s) |
| Database (database-wide) | commits 1,175 to 46,649; connections 3 to 6 | commits 569 to 55,117; connections 3 to 5; rollbacks 0; deadlocks 0; size 8.37 to 9.85 MB |
| HTTP | 179,932 requests; non-2xx 503 x80, 409 x297 | 261,885 requests, 2.054 GB body; only 409 (`/attack` 340, `/input` 23, `/recover` 3) |
| Operator metrics samples | 144 / 0 failed | 144 / 0 failed |

Reading it. Pacing removed the 503 storm: no adapter hit `busy` at all, so the client's new retry was not exercised
by the soak (it is covered by `OnlineBusyBackoffTest`; this run does not prove the backoff against a real 503).
Time at exactly 20 is not higher than 56.5% because the tool deliberately closes and reopens each adapter every 120 s
(96 reconnects, each at least 3 s offline plus pacing), which holds the count at 19 for most of the remainder. That is
the tool's churn, not a connection failure; it was a hidden part of the first smoke too. To measure a steadier plateau
lengthen the reconnect period or add a no-reconnect option (not done). The lethal starter ground (T4C-0248) still
shapes the attack/recovery mix. The result does **not** pass the two-hour soak, the M5 gate or Beta. No server code
was changed.

Admission adequacy (recommendation, not a change). With a login burst of twenty the server needed only the paced one-wide
open; at most one join was in flight and the 116 joins had a 100 ms-or-faster handler lifetime for 116 of 116. Two public
slots look adequate for a burst once clients pace or back off, because a join holds its slot for well under 100 ms
and the retry schedule spreads the rest across a few seconds. An unpaced simultaneous burst of twenty would be expected to still
see 503s for the first seconds (not re-measured here), and a real client without the new retry would have failed; with it the worst case
is a login that takes up to roughly ten seconds before giving up. Re-measure with an unpaced burst (open gate set to
twenty) if the owner wants a hard number before raising `MAX_PUBLIC`.

### Rerun after server movement parity, PG16 WSL, 12 minutes (T4C-0287, 2026-10-29): FAILED, with a server-side persistence finding

Same setup as the paced rerun (fresh disposable PostgreSQL 16 cluster on loopback 56982, TEMP runtime snapshot, one
`ServerMain --online` child on 18797 with a private control file, twenty adapters, 720 s, one-at-a-time paced opens), but
on `origin/main` 0a6622f9 plus later main (server movement now follows single-player's grid-reservation rule, PR #234;
also the shop, spell-engine and regeneration changes merged since the previous run), with the soak's walker changed to plan
from the tile the player is arriving on. The report ran to completion (`finished: true`, 720.045 s); the cluster was
destroyed and the TEMP runtime and profiles removed. Local PG16 evidence only.

| Metric | Paced rerun (T4C-0250) | After movement parity (T4C-0287) |
| --- | --- | --- |
| Time at 20 adapters | 406.5 s (56.5%) | 23.4 s (3.2%) |
| Time at 19 or more | 702.0 s (97.5%) | 55.5 s (7.7%); most time at 13 to 17 |
| Adapter failures | 0 | 860 (`adapter_stopped_after_connect`, `adapter_stopped_before_connect`, `gameplay_result_unavailable`) |
| Reconnects | 96 (scheduled) | 873 |
| Attacks / recoveries | 932 / 14 | 1,181 / 8 |
| Commands accepted / rejected | 600 / 346 | 758 / 398 |
| HP changes / movement observations | 1,582 / 93,786 | 1,969 / 78,616 |
| Command latency (ms, all adapters) | `<=250` 279, `<=500` 665, `<=1000` 2 | `<=250` 111, `<=500` 1,052, `<=1000` 25 |
| `/health` | mean 0.494 ms, 34.001 Hz, 0 failures | mean 1.272 ms, 34.000 Hz, 0 failures |
| Ticks | 24,742 ok, 0 failed, max callback 39.0 ms, 2 over budget | 24,446 ok, 0 failed, mean 2.29 ms, max callback 22.3 ms, 0 over budget, 0 deadline resets, max debt 4.8 ms |
| Peak heap used / CPU | 645.5 MiB / 175.2 s | 596.6 MiB / 178.3 s |
| Database (database-wide) | commits 569 to 55,117; rollbacks 0 | commits 403 to 54,536; **rollbacks 0 to 828**; deadlocks 0 |
| HTTP non-2xx | only 409 (`/attack` 340, `/input` 23, `/recover` 3) | `/join` 409 x739, `/attack` 409 x407, `/input` 409 x17, 401 on stale-session calls, `/corpses` 503 x7 |

Finding (server, not fixed here). The server log shows 89 times `Online persistence failed (classes=Conflict, sqlstate=unavailable);
character disconnected. Recover after lease expiry.` That is a commit that lost a revision conflict; the character is dropped and
its lease stays held, so the adapter's reconnects are answered `409` on `/join` (739 of them) until the lease expires, which
explains the failures, the reconnect count and the low time at 20. The tick loop itself was healthy (no failed tick, 34.0 Hz).
Isolation, one 4-minute soak each on the same cluster: the same code with the swept movement rule
(`-Dt4c.server.movement=swept`) still produced 24 Conflict disconnects (229 failures), and the previous, unmodified soak walker
against the new grid rule produced 32 (297 failures). So the Conflicts are **not caused by the movement rule or by the walker
change**; they come from something else merged since the paced rerun (candidates are the shop, spell-engine, regeneration or
stun commits that now write the character; not bisected here). The soak's own walker change was checked to be harmless:
movement observations and attacks continue as before. This run therefore does not show the movement rule's effect on
sustained occupancy; a clean comparison needs the persistence Conflict fixed first. The result does **not** pass the two-hour
soak, the M5 gate or Beta. No server code was changed.
