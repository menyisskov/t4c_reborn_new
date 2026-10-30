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
their normal sibling layout. By default it does not navigate portals, so `worldTransfers` stays 0 and
nothing is claimed about transfers; set `T4C_SOAK_PORTAL_WALKERS` (below) for a mixed load with region transfers.

### Optional paced portal walking (T4C-0308)

`T4C_SOAK_PORTAL_WALKERS=N` (0 to 20, default 0 = off) makes N evenly spread adapters take periodic portal trips
while everyone else, and the walkers between trips, keep attacking, recovering and reconnecting as before. A trip is:
walk to an authored portal source tile in the current region, transfer, spend `T4C_SOAK_PORTAL_DWELL_SECONDS`
(default 20) in the destination, optionally go one or two levels deeper (up to three hops, so the sewer stairs are
used), then walk back through the paired portals to the starting region. A walker rests
`T4C_SOAK_PORTAL_INTERVAL_SECONDS` (default 60) between trips and abandons a leg after
`T4C_SOAK_PORTAL_LEG_TIMEOUT_SECONDS` (default 300; the hub to cave-mouth walk alone takes up to 165 s at the shared player speed, so 150 timed out about one leg per run). The first trips are staggered from 30 s in. The routes are derived
from the trusted hosted plan (a portal is walked when both its source and target are in hosted regions and it has a
way back), never from network strings, and walking uses the same arriving-tile planning as the combat walker, to the
exact portal source tile. Every guard is unchanged: a region change without an epoch change, an epoch regression, a
position outside the shared collision map and so on still count as adapter failures. A trip that is surprised (for
example a death respawn elsewhere) is counted as aborted, a stalled leg as a leg timeout; both are reported, neither is
an adapter failure. The report adds a `portalWalk` section (routes, trips started/completed/aborted, leg timeouts,
outbound/inbound transfers), per-adapter `regionTransfers` and `portalTrip`, and bounded `errorDetails` (region and
position beside an error name). Unit checks run with `NativeAdapterSoak.java --self-test`.

### Optional steadier mode (T4C-0315)

`T4C_SOAK_RECONNECT_INTERVAL_SECONDS=N` (30 to 86400, default 120 = unchanged) sets the time between each adapter's scheduled
reconnects. At the default every adapter closes and reopens every two minutes, and the one-at-a-time paced opens plus the
three-second reopen delay cap the time spent at twenty adapters near 50 to 56%, whatever the server does. A larger interval
(for example 900) lets the soak measure a plateau at twenty; the report states the value used (`reconnectIntervalSeconds`).
Reconnect coverage shrinks accordingly, so use the default when reconnects are what is being exercised.

The walkability guard checks the snapshot position against the server's own movement rule (`PreviewMap.onGridPath`): a tile
centre the grid rule lets a player rest on, or a point on the straight glide between two adjacent such centres (diagonals too).
Anything off that lattice or inside a foot-blocked tile still counts as `position_not_walkable`. It replaced `canStand`, which
folds the tile above the foot into the block and is stricter than the rule that moves players (see the T4C-0315 section).

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

**Streamed world 0 (T4C-0363).** For a server started with `--stream-world0`, set `T4C_SOAK_STREAM_WORLD0=1` so the
harness builds the same streamed plan (the adapters read the shared collision grid, route inside a 201 x 201 box instead
of the 19 M-tile map, and hold the tile they joined on when nothing is near). After provisioning, run
`java -cp <soak classpath> multiplayer/tools/SoakSpread.java <profiles-dir> <worldmap.colbin>` against the disposable
database (it refuses any database not named `t4c_acc_` or `t4c_loot_`) to move the twenty characters from the temple to
twenty separated monster fields, so a run exercises activation, chases and deactivation across the map. The server prints a
`streamed-creatures` line every 30 s (active, loading, queued, settling, covered cells, activated, deactivated, pristine
rows deleted, batches, slowest batch, cap refusals, peak active); keep its stdout for the report. Recorded in
WORLD_STREAMING_DESIGN.md section 12.

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

### Rerun after server movement parity, PG16 WSL, 12 minutes (T4C-0288, 2026-10-29): FAILED, with a server-side persistence finding

Same setup as the paced rerun (fresh disposable PostgreSQL 16 cluster on loopback 56982, TEMP runtime snapshot, one
`ServerMain --online` child on 18797 with a private control file, twenty adapters, 720 s, one-at-a-time paced opens), but
on `origin/main` 0a6622f9 plus later main (server movement now follows single-player's grid-reservation rule, PR #234;
also the shop, spell-engine and regeneration changes merged since the previous run), with the soak's walker changed to plan
from the tile the player is arriving on. The report ran to completion (`finished: true`, 720.045 s); the cluster was
destroyed and the TEMP runtime and profiles removed. Local PG16 evidence only.

| Metric | Paced rerun (T4C-0250) | After movement parity (T4C-0288) |
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

### Root cause of the T4C-0288 persistence Conflicts and 12-minute rerun on fixed main (T4C-0298, 2026-10-29): passed the tool's zero-error criterion

Cause. All of the "Online persistence failed (classes=Conflict)" disconnects were one operation: the retaliation commit
(`operation=monster_attack`, the safe attribution added by PR #250). A diagnostic build that read the creature row back inside the
failing transaction showed the row matched the attack in every field (life, revision, target, due time, alive). Only the database-clock
due check refused: the guarded UPDATE requires `next_attack_at <= clock_timestamp()` (T4C-0190). The server fires a retaliation on the
first 34 Hz tick where its own clock reaches the due time, but the WSL PostgreSQL clock runs about 5 to 15 ms behind the Windows JVM,
so the commit arrived while the database still said "not due". The transaction had written nothing, so the refusal was definite, yet it
was handled like an ambiguous commit: the character was disconnected, its lease stayed held and every reconnect got `409` on `/join`
until the lease expired. It was not a revision race. Checkpoint, shop, spell and melee writers are single-flight per character and
none of them failed. It is not a regression from one merged change: a 4-minute soak of the PR #203 tree (the one that passed 12 minutes
on 2026-10-28) also showed 4 such disconnects today, and main before the fix showed 15 to 18 in 4 minutes, so the guard is old and the
rate follows clock skew and retaliation volume (more monsters retaliate since the monster spell attack changes).

Fix. Codex's PR #252 (merged at `d92d265c`) turns a due-time miss, only while every other precondition still matches, into a deferred
unapplied result instead of a failure. An independent fix written in this session (a definite `attack_not_due` refusal) was dropped in
favor of it; both give the same behaviour.

Runs (PostgreSQL 16 WSL, disposable clusters, `ServerMain --online`, twenty paced adapters; six soaks in total):

| Build | Length | Persistence Conflict disconnects | Adapter failures |
| --- | --- | --- | --- |
| main `9461ce4c` (before the fix) | 4 min | 15, then 18 (all `monster_attack`) | about 20 per 4 minutes (reconnect storm; 860 in 12 minutes) |
| PR #203 tree `4bcb72b4` | 4 min | 4 | 0 reported |
| main `9461ce4c` plus the local equivalent fix | 4 min, then 12 min | 0, 0 | 0, 0 |
| main `f7f8c26f` plus T4C-0298 docs (includes #252) | 12 min | 0 | 0 |

| Metric (12 minutes) | Paced rerun (T4C-0250, PR #203) | After movement parity, before the fix (T4C-0288) | Fixed main (this run) |
| --- | --- | --- | --- |
| Time at 20 adapters | 406.5 s (56.5%) | 23.4 s (3.2%) | 403.3 s (56.0%) |
| Time at 19 or more | 97.5% | 7.7% | 97.3% |
| Adapter failures | 0 | 860 | 0 |
| Reconnects | 96 | 873 | 96 |
| Attacks / recoveries | 932 / 14 | 1,181 / 8 | 1,575 / 18 |
| Commands accepted / rejected | 600 / 346 | 758 / 398 | 999 / 593 |
| Command latency (ms, all adapters) | `<=250` 279, `<=500` 665, `<=1000` 2 | `<=250` 111, `<=500` 1,052, `<=1000` 25 | `<=250` 159, `<=500` 1,402, `<=1000` 31 |
| `/health` | 34.001 Hz | 34.000 Hz | 34.000 Hz, mean 1.12 ms |
| Database rollbacks | 0 | 828 | 0 |

Limits. The 12-minute length and the paced reconnect churn are unchanged, so the 56% at twenty is the tool's own ceiling, not a plateau.
The skew was inferred from the logged JVM time against the database time inside the failing transaction (a few milliseconds each);
production hosts with a different clock offset see the same race, which is why the deferral matters beyond this machine.
Local PG16 evidence only. This does **not** pass the soak gate, M5 or Beta.

### Rerun with the hosted skeleton cave (T4C-0301, 2026-10-29): passed the tool's zero-error criterion

PR #260 (T4C-0300) hosts the closed skeleton cave (hosted spawns about 50 to about 80, portals 12-15 enabled) and was not
soaked before review. Same setup as the fixed-main run: fresh disposable PostgreSQL 16 cluster on loopback 56941, one
`ServerMain --online` child on 18811 from the PR head `cd0f2953` (PR #260 then merged at `21378cca`; its one later commit changes only a test message; protocol 7, region `skeleton-cave` loaded), a private control
file, twenty paced adapters, 720 s. The wrapper exited 0 and the report ran to completion (`finished: true`, 720.095 s). The server was
stopped through the private `/admin/shutdown` (200), the cluster destroyed, and the sanitized report stays in TEMP, not committed.
Local PG16 evidence only.

| Metric (12 minutes) | Fixed main `f7f8c26f` (T4C-0298) | PR #260 head `cd0f2953` (this run) |
| --- | --- | --- |
| Time at 20 adapters | 403.3 s (56.0%) | 403.0 s (56.0%) |
| Time at 19 or more | 97.3% | 97.4% (701.1 s) |
| Adapter failures | 0 | 0 |
| Reconnects (scheduled) | 96 | 96 |
| Database rollbacks / deadlocks | 0 / 0 | 0 / 0 (commits 3,326 to 80,957) |
| `/health` tick rate | 34.000 Hz, mean 1.12 ms | 33.999 Hz, mean 1.55 ms, 0 failures, tick deficit proxy 0.94 |
| "Online persistence failed" lines | 0 | 0 (the whole server log is three startup lines) |
| Server ticks | not recorded | 26,357 ok, 0 failed, mean 2.26 ms, max 23.9 ms, 0 over budget, 0 deadline resets, max deadline debt 6.7 ms |
| Attacks / recoveries | 1,575 / 18 | 1,402 / 16 |
| Commands accepted / rejected | 999 / 593 | 911 / 507 |
| Command latency (ms, all adapters) | `<=250` 159, `<=500` 1,402, `<=1000` 31 | `<=250` 164, `<=500` 1,244, `<=1000` 10 |
| HTTP non-2xx | not recorded | only 409: `/attack` 503, `/input` 27, `/recover` 4 (no 503, no 401) |
| Peak heap used / process CPU | not recorded / not recorded | 623 MiB / 281.6 s (see below) |

Reading it. No regression in any measured gate: occupancy, failures, reconnects, rollbacks and tick rate match the baseline, and no
persistence failure of any operation appeared. Mean `/health` latency (1.55 ms against 1.12 ms) and the lower attack count are within
the run-to-run spread seen between earlier runs (0.49 to 1.27 ms for `/health`). Process CPU of 281.6 s over 775 s of server uptime is higher
than the 175 to 178 s recorded for the earlier twelve-minute runs, although mean tick cost (2.26 ms against 2.29 ms in T4C-0288) did not rise;
the baseline for fixed main was not captured, so this is reported, not explained. The extra CPU is not visible in any tick, deadline or latency
figure.

Limits. The soak tool never walks a portal (`worldTransfers` 0), so the adapters stayed in the starting world and Lighthaven; the skeleton
cave was loaded and its creatures simulated, but no adapter entered it. This run therefore shows that hosting the extra region does not
degrade the existing twenty-player load; it says nothing about players inside the cave. The 56% at twenty remains the tool's reconnect-churn
ceiling. Local PG16 evidence only; this does **not** pass the soak gate, M5 or Beta. No server code was changed.

### Mixed load with region transfers (T4C-0308, 2026-10-29): two server defects found and fixed, final run passed the tool's zero-error criterion

T4C-0300/0304 host the skeleton cave, the Lighthaven interior and three sewer levels (portals 0-5 and 12-17), but the soak never walked
a portal. With `T4C_SOAK_PORTAL_WALKERS=8` (8 of the 20 adapters walk; trips every 60 s, 20 s dwell, up to three hops) the same
twelve-minute, paced-open, twenty-adapter soak now exercises transfers, location epochs and lease fencing alongside attacks, deaths
and the scheduled 120 s reconnects. Setup as before: fresh disposable PostgreSQL 16 cluster per run on loopback 57043, one
`ServerMain --online` on 18831 from the worktree jar, private control file, `/admin/shutdown` (200), cluster destroyed after. Local PG16
evidence only. Runs, in order (B to E ran the full 720 s and finished):

| Run | Build | Result |
| --- | --- | --- |
| A | tool on main `ad623ac8` | aborted at 137 s by the **wrapper**, not the server: one adapter printed its single stopped-connection line to stderr and Windows PowerShell 5.1 (`$ErrorActionPreference = 'Stop'`) turned it into a terminating error. The wrapper now keeps stderr visible and judges by exit code. |
| B | main + walkers | persistence failures, see below; first `position_not_walkable` |
| C | main, no walkers (control) | the same persistence failures **without any transfer** |
| D | + due-check deferral fix | 0 persistence failures; 18 `position_not_walkable` on three walkers |
| E | + arrival-centering fix (this PR) | passed: 0 failures |

| Metric (12 minutes) | T4C-0301 (PR #260, no hosted sewers, no walking) | C: main, no walking | B: main + 8 walkers | D: + deferral fix | E: + centred arrivals (this PR) |
| --- | --- | --- | --- | --- | --- |
| Time at 20 adapters | 403.0 s (56.0%) | 317.2 s (44.1%) | 328.8 s (45.7%) | 403.2 s (56.0%) | 403.0 s (56.0%) |
| Time at 19 or more | 97.4% | 84.8% | 91.2% | 97.4% | 97.3% |
| Adapter failures | 0 | 61 | 46 | 18 | 0 |
| Reconnects (96 scheduled) | 96 | 152 | 133 | 96 | 96 |
| Database rollbacks / deadlocks | 0 / 0 | 60 / 0 | 40 / 0 | 0 / 0 | 0 / 0 |
| "Online persistence failed" lines | 0 | 6 (`monster_attack` Conflict) | 4 (`monster_attack` Conflict) | 0 | 0 |
| Transfer failures (`operation=transfer`) | n/a | n/a | 0 | 0 | 0 |
| `/health` tick rate / mean | 33.999 Hz / 1.55 ms | 34.001 Hz / 3.88 ms | 34.002 Hz / 3.37 ms | 34.000 Hz / 1.19 ms | 34.000 Hz / 1.18 ms |
| Server ticks (mean / max / over budget) | 2.26 / 23.9 ms / 0 | 5.99 / 36.1 ms / 15 | 5.39 / 38.4 ms / 24 | 2.51 / 18.3 ms / 0 | 2.57 / 18.7 ms / 0 |
| Process CPU (about 775 s uptime) | 281.6 s | 575 s | 499 s | 301 s | 286 s |
| Trips started / completed / aborted / leg timeouts | 0 | 0 | 29 / 21 / 0 / 2 | 29 / 22 / 0 / 1 | 28 / 22 / 0 / 1 |
| Region transfers out / in (all adapters) | 0 | 0 | 26 / 23 (49 seen) | 30 / 25 (55 seen) | 25 / 24 (49 seen) |
| World changes / epoch changes (all adapters) | 0 / 0 | 0 / 4 | 44 / 56 | 48 / 61 | 45 / 54 |
| Attacks / recoveries | 1,402 / 16 | 369 / 2 | 342 / 1 | 359 / 0 | 446 / 0 |
| HTTP non-2xx | `/attack` 409 x503, `/input` 409 x27, `/recover` 409 x4 | `/join` 409 x54, `/attack` 409 x122, 401s | `/join` 409 x36, `/attack` 409 x98, `/input` 409 x55 | `/attack` 409 x129, `/input` 409 x54 | `/attack` 409 x190, `/input` 409 x48 |

Attack counts fall in the sewer-capable builds because the 1,402 of T4C-0301 is not comparable (older hosted ground, and adapters here spend time
walking and dying in other regions); they are not a regression signal. "Seen" transfers count the adapters' observed epoch changes that changed region, which
include a transfer's arrival after a reconnect.

Defect 1, persistence Conflict on `monster_attack` (present on main **without walking**, run C). The T4C-0298 deferral (PR #252) decided
"the attack was not yet due" by reading the database clock a second time after the guarded UPDATE had missed. A diagnostic build (not committed) showed
every other field of the locked row equal to the attack's (same life, revision, target, deadline, health) and the second clock reading 1 to 2 ms
past the deadline the UPDATE had seen as not yet due, so the deferral said "due" and the code threw the Conflict that disconnects the target and holds its
lease (the `409` reconnect storm that cut time at twenty to 44-46%). The UPDATE has exactly the compared predicates plus the due check, so a miss with
all others matching can only be the due check; the second reading is dropped (`OnlineStore.attackDeadlineNotDue`, pure rule `onlyDueCheckMissed`
covered by `AttackDeadlineMissTest`). The CPU and tick-time growth against T4C-0301 (575 s and 6 ms in run C) disappears with it, so it was the reconnect
storm, not the extra hosted regions. The rate was 4 to 6 disconnects in 12 minutes whether or not adapters walked, so it needs no transfer to appear. The
T4C-0298 result stands for its run (a larger clock skew then); the race remains whenever the two readings straddle the deadline.

Defect 2, `position_not_walkable` after leaving the skeleton cave (run D, 18 failures on adapters 10, 13 and 18 only; run B had the same six on one
adapter). Authored portal targets (for example portal 15 to 2772,1096) and the respawn point (2951,1038) are whole tile numbers and were stored as the player
position, so players arrived on a tile **corner**. Grid movement keeps the half-tile offset, so the player then walked along the row boundary (observed at
2785.0 to 2786.0, 1096.0) through tiles the shared collision map calls blocked, which the soak's unchanged walkability guard reported. A database read after
run D showed a character stored at exactly 2932,1059. Portal transfers and respawns now land on the centre of the authored tile
(`PersistentSessions.arrivalAtTileCentre`, covered by `ArrivalAtTileCentreTest` and the updated `HostedTransferTest` arrival assertion). Existing saves are
unchanged (no cap or limit involved): a character already saved on a corner keeps it until its next transfer or death.

Not fixed, reported. (1) About one walker leg per run times out in the start region near the cave mouths (last seen at 2777.7,1099.5); the walker is a
cardinal breadth-first router over the collision map and the server uses grid reservations, so a corner slide or a body-blocking creature can stall it. It is
reported as a leg timeout, never an adapter failure, and the next trip proceeds. (2) `HostedTransferTest.caveMouth...` failed once in this environment with a
`transfer` persistence Conflict and once with Windows ephemeral-port exhaustion during a ten-class run, and passed in three isolated reruns. The guarded transfer
UPDATE uses the same database-clock pattern (`portal_cooldown_until<=clock_timestamp()`), the likely cause, but it was not reproduced under a diagnostic build, so
that is a hypothesis. (3) The Codex-claimed retaliation, shop and UI files were not touched.

Limits. 8 of 20 adapters walk; the 56% at twenty is still the tool's reconnect-churn ceiling (96 scheduled closes); no steady plateau at twenty was measured.
Twelve minutes and one run per configuration. This does **not** pass the two-hour soak, the M5 gate or Beta.
### Follow-up (T4C-0309, 2026-10-29): the cave-mouth leg timeout and the transfer Conflict explained

Both items T4C-0308 left unfixed were run to ground.

**Leg timeout near the cave mouths (2777.7,1099.5): not a stall.** A headless replay drives the real `PreviewWorld` (the server's grid-reservation rule over the
hosted collision crop) with the soak's walker (cardinal breadth-first route, 100 ms plans, steering by the arriving tile). From 2,239 start tiles around the three
cave-mouth portals every connected start reached its goal; the only non-arrivals were 14 starts in disconnected pockets of the collision map, which no walker can
leave. Nothing in the shared rule or the server traps a walker, so no movement rule changed and the single-player golden tests are untouched. The cause is length: ideal
walking at the shared player speed takes 142 s from the Lighthaven door arrival (2932,1059) and 147 s from the hub spawn to the mouth, up to 164 s from the creature
circle (2951,1038), and 143 s back from the cave arrival (2772,1096) to door 16. That is the whole of the default 150 s leg timeout before any creature body-blocks the
walker, so about one leg per run ended a few tiles short (2777.7,1099.5 is eight tiles from the mouth). The default is now 300 s and
`PortalWalkTimingTest` pins both facts: the walk is long (over 120 s) and the timeout keeps at least 50% headroom over the slowest ideal route.

**Transfer Conflict: the database-clock race, reproduced.** The portal cooldown is stamped on the server clock (`now + 350 ms`) and the guarded transaction compares
it with `clock_timestamp()` on the database. A hop proposed on the first tick the server clock allowed it (a walker or test relocating straight onto the next portal
after arriving) could be refused while the database clock was a few milliseconds behind, and the refusal was handled as an unknown outcome: disconnect and lease hold.
With the server clock held 1.5 s ahead of the database in a test, the pre-fix build printed `operation=transfer, classes=Conflict` and dropped the walker, exactly the
flake in `HostedTransferTest`. By elimination, as for `monster_attack`: the revision, epoch and world checks pass under the row lock just before the cooldown check, so the
stored cooldown is the value the server already saw expire and only the two clocks differ. The refusal is now a definite `portal_cooldown` (nothing written, guard
unchanged and still in the database), the walker is unfrozen and the server proposes the hop again a quarter second later. `HostedTransferTest` gained a test with the
server clock ahead (the hop commits only once the database agrees, the session survives) and a test that a transfer inside a stored cooldown is refused and writes
nothing. Full server suite on a disposable PG16 cluster: 324 tests, 0 failures. No soak run was repeated for this change.

### Two-hour mixed soak (T4C-0315, 2026-10-29): the run itself failed the zero-error criterion on a guard/rule mismatch, which exposed a rejoin lockout; fixed and re-run for 30 minutes

The M5 two-hour, twenty-adapter soak on `origin/main` `b19beb34` (protocol 7, schema 13; includes movement parity, the hosted skeleton cave,
Lighthaven interior and three sewer levels, the `monster_attack` deferral fix, centred arrivals, the portal-cooldown retry and the loot countdown) with
`T4C_SOAK_PORTAL_WALKERS=8` and the default 120 s reconnect schedule. Fresh disposable PostgreSQL 16 cluster on loopback 57211 (acceptance-cluster
tooling), one `ServerMain --online` on 18847 from the worktree jar copied to a scratch runtime, private control file, side sampler reading the report's
`serverRuntime.latest` and the server process every 300 s. Wrapper exit 0, `/admin/shutdown` 200, server exit 0, cluster destroyed, ports free. A second,
30-minute run on the fixed build used `T4C_SOAK_RECONNECT_INTERVAL_SECONDS=900`. Local PG16 evidence only; native adapters, not rendered clients.

| Metric | T4C-0198 (2 h, protocol 4, no walking, old build) | T4C-0308 E (12 min, 8 walkers) | **T4C-0315 two-hour run (this build)** | T4C-0315 30-min steady run (fixed build, 900 s reconnects) |
| --- | --- | --- | --- | --- |
| Duration | 7,200.0 s | 720 s | 7,200.1 s | 1,800.1 s |
| Time at 20 adapters | 3,492.8 s (48.5%) | 403.0 s (56.0%) | **3,567.8 s (49.6%)** | **1,724.0 s (95.8%)** |
| Time at 19 or more | 7,198.9 s (about 100%) | 97.3% | 7,068.1 s (98.2%) | 1,789.9 s (99.4%) |
| Time at 18 / 17 or fewer | n/a | n/a | 122.4 s / about 9 s (startup) | 0.6 s / about 9 s (startup) |
| Adapter failures | 0 | 0 | **2** (`position_not_walkable`, players 3 and 9) | 0 |
| Reconnects | 1,151 | 96 | 1,150 | 20 |
| Database rollbacks / deadlocks | 56 / 0 | 0 / 0 | 0 / 0 | 0 / 0 (delta; the cluster counter also holds a server test run) |
| "Online persistence failed" lines | n/a | 0 | 0 | 0 |
| `/health` failures / mean | 0 / 5.990 ms | 0 / 1.18 ms | 0 / 0.647 ms | 0 / 0.981 ms |
| Tick rate (wall-clock proxy) | 33.999 Hz | 34.000 Hz | 34.000 Hz | 34.000 Hz |
| Ticks completed / failed | 244,720 / 0 | n/a | 244,928 / 0 | 61,504 / 0 |
| Tick time mean / max / over budget | 0.555 ms / 147.5 ms / 2 | 2.57 ms / 18.7 ms / 0 | 2.48 ms / 35.9 ms / 1 | 2.70 ms / 19.1 ms / 0 |
| Max deadline debt / deadline resets | 134.8 ms / 1 | n/a | 17.4 ms / 0 | n/a / 0 |
| Operator samples (ok / failed) | 1,428 / 0 | n/a | 1,425 / 0 | n/a / 0 |
| Peak heap used (JVM pools) | 612 MiB | n/a | 654 MiB (maximum over 1,425 operator samples) | 624 MiB (358 samples) |
| Process CPU | 10.9% of a core | 36.9% | 2,645.7 s over 7,205 s = 36.7% of a core | 766 s over 1,815 s |
| Trips started / completed / aborted / leg timeouts | n/a | 28 / 22 / 0 / 1 | 296 / 286 / 2 / **0** (8 in flight at the end) | 72 / 69 / 0 / 0 |
| Region transfers out / in (walkers); observed by all adapters | 0 | 25 / 24; 49 | 314 / 311; 625 | 78 / 76; 154 |
| Location epoch changes (all adapters) | n/a | 54 | 666 | n/a |
| Attacks / recoveries | 6,428 / 41 | 446 / 0 | 2,767 / 20 | 779 / 2 |
| Commands accepted / rejected | 4,461 / 2,008 | n/a | 1,867 / 920 (cooldown, contention, stale revision) | 528 / 253 |
| HP changes | 1,785 | n/a | 8,514 | 1,854 |
| Command latency buckets (`<=50`, `100`, `250`, `500`, `1000` ms; none slower) | 0, 0, 1,573, 4,877, 19 | n/a | 0, 0, 289, 2,476, 22 | 0, 0, 70, 707, 4 |
| HTTP non-2xx | n/a | `/attack` 409 x190, `/input` 409 x48 | `/attack` 409 x920, `/input` 409 x538 (no `/join` 409, no 401) | `/attack` 409 x253, `/input` 409 x143 |
| Application response bytes (not wire) | n/a | n/a | about 17.6 GB, 2.44 MB/s total, 122 KB/s per adapter (`/state` is 97%) | n/a |
| First connect per adapter | n/a | n/a | at most 447 ms after open | n/a |

Heap and CPU over time (side sampler every 300 s, 24 samples, two-hour run): used heap is a sawtooth between collections (52 to 620 MiB at the 300 s samples, 654 MiB peak over all operator samples);
the per-window minimum stays 52 to 78 MiB from the first hour to the second, and the mean is 273 MiB in the first hour against 325 MiB in the
second (collection timing, no trend). Non-heap grew 42.2 to 49.0 MiB (+1 MiB in the second hour). Process working set went 842 to 879 MiB
(+37 MiB overall). CPU per 300 s was 84.8 s in the first sample (start-up) then 89 to 123 s, averaging
108.9 s in the first hour and 112.1 s in the second (36 to 37% of a core). No growing queues or resources were found, and the old T4C-0198 tick maximum of
147 ms and 56 rollbacks did not recur.

**Result against the tool's own criterion: the two-hour run FAILED it (2 adapter failures).** Everything else was clean (0 server errors, 0 rollbacks, 0
persistence lines, 0 transfer failures, 0 leg timeouts, server and wrapper exit 0). The two failures were `position_not_walkable` on two different
adapters (player 3 on a portal trip at epoch 77: abs 2866.99997,1080.99999; player 9 with no transfers: abs 2797.00003,1104.0000076), one observation each.

Root cause (not a data race, no guard weakened in spirit, but one latent server defect found by it). Both positions are the exact **midpoint of a diagonal
glide** between two adjacent tile centres. The server moves players with single-player's grid rule, which tests the static foot collision (plus an
authored portal source) of the tile being entered; its wall-slide step may enter a tile whose tile *above* is blocked when the departure tile had the same
block. The guard asked `PreviewMap.canStand`, which folds the above-foot tile into the block and so is stricter than the rule that moved the player.
The two tiles involved are 2866,1080 (foot free, tile above 2866,1079 blocked) and the diagonal 2797,1103 to 2796,1104 beside foot-blocked 2797,1104.
So the guard disagreed with the authoritative rule on tiles the rule lets players cross, and with four players crossing them rarely it fired twice in 2 h
(never in the earlier 12-minute runs). The disagreement is not only cosmetic: **`PreviewWorld.joinAt` used the same `canStand` check, so a character
that logged out resting on such a tile was refused on rejoin (409 `position`) and stayed locked out.** A sweep over every tile of the starting crop with
eight directions found reachable rest positions the old join refused (the first was 2932.5,1056.5, a few tiles from the Lighthaven door arrival).

Fix (`T4C-0315`): in grid mode `joinAt` accepts any position the movement rule can rest on (`PreviewMap.canRestOnGrid`; the swept mode keeps `canStand`);
the soak's guard now checks the server's own lattice (`PreviewMap.onGridPath`: tile centres the rule can rest on or the straight glide between two
adjacent such centres, diagonals included) and still fails a position off the lattice or in a foot-blocked tile. `GridRestPositionTest` pins both: every
reachable rest in the starting crop is rejoinable and on the lattice (the sweep covers over 100,000 rests and reaches a rest `canStand` refuses), the two
observed positions are accepted, and the T4C-0308 row-boundary walk (2785.3 or 2785.7 with y on a whole number) and a foot-blocked tile centre are
still rejected. Existing saves need no clamp or migration (the check only became more permissive for positions the rule itself produces). Full server
suite on a disposable PG16 cluster: 325 tests, 0 failures.

The 30-minute steady run on the fixed build had **0 adapter failures**, 0 rollbacks, 0 persistence lines, 69 completed portal trips and 154 observed
transfers; it is the first measurement of a plateau at twenty: 1,724.0 s at twenty (95.8%), only the opening minute and the single scheduled
reconnect round below it. It was not repeated for two hours.

What this does and does not prove against the M5 bullets. It shows, on one 2 h run and one 30 min run on local PG16: 20 native adapters with 8 of them
taking paced portal trips through the cave, Lighthaven door and sewer stairs keep the tick loop inside budget (1 over-budget callback in 244,928, max
35.9 ms), the server process flat in CPU and heap (no leak signal), no database rollbacks or deadlocks, no persistence failures with a named operation,
and zero transfer failures over 625 observed transfers. It does **not** prove: (1) a 2 h run with zero adapter failures (the 2 h run had 2, now explained
and fixed, validated for 30 minutes only); (2) a 2 h plateau at twenty (49.6% at twenty is the reconnect-churn ceiling of the default mode, and the
steady mode ran only 30 minutes); (3) wire bandwidth or database operation/pool-wait latency (only application body bytes and adapter-inclusive command
latency were recorded); (4) anything about rendered clients, quests, loot pickup, shops or NPC topics (the soak does none of these); (5) TLS/remote. The
current M5 mixed-session bullet requires two real rendered clients with no duration minimum, so no M5 or ACCEPTANCE box was ticked from this evidence.
Not touched: `OnlineRetaliation`, shop and UI wiring.
