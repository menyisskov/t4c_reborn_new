# Load observations (T4C-0198)

Delivery status (2026-10-27): operator endpoint and load-report integration are implemented and
verified, being delivered as T4C-0198. These checks do not complete the sustained Beta gate.

Implemented for T4C-0198: `SimulationLoop.metrics()` and
`ServerRuntimeMetrics.snapshot()` are exposed through local operator
`GET /admin/metrics`. The route requires the private shutdown control token and an
actual loopback peer; it does not enqueue work on the simulation thread. Public
`GET /health` keeps its existing response. These measurements do not establish that
the sustained 20-player Beta performance gate has passed.

## Simulation contract

All `*Nanos` fields are monotonic durations in nanoseconds. `stepNanos` is the
configured scheduling interval; gameplay still receives the original fixed delta.
Counters start at zero per loop instance and are never reset by sampling.

| Field | Meaning |
| --- | --- |
| completedTicks | Successfully returned simulation callbacks only |
| failedTicks | Callbacks that threw; the original exception still propagates |
| measuredTicks | Completed plus failed callbacks |
| totalTickNanos / maxTickNanos / lastTickNanos | Callback elapsed time, including failed callbacks |
| overBudgetTicks | Callbacks taking strictly longer than one scheduling interval |
| lateStarts / totalStartLatenessNanos / maxStartLatenessNanos | Positive start delay relative to the scheduled deadline; includes catch-up scheduling debt, not just OS wake-up latency |
| lastDeadlineDebtNanos / maxDeadlineDebtNanos | Positive delay beyond the next tick deadline after a successful callback, before any schedule reset |
| deadlineResets | Existing catch-up policy reset count: debt strictly greater than four intervals |
| discardedScheduleNanos | Total amount by which resets moved the next scheduled deadline forward; this includes the interval until the new deadline |
| elapsedNanos | Elapsed monotonic time since run began, frozen when run exits |
| running | Run began and has not exited, including time in callbacks or parking |
| consistent | Counter fields were sampled without a concurrent counter update |

Callback and post-callback scheduling counters publish in separate short phases.
A consistent observation can therefore contain the latest completed callback and
the preceding scheduling observation. No callback is duplicated, skipped, or
counted as successful by instrumentation. Failed callbacks have timing observations
but do not advance scheduling counters. Schedule resets do **not** count fictitious
gameplay ticks and do not change the gameplay delta.

Storage is fixed-size scalar state. The existing Tick allocation remains; there are
no added per-tick records, histories, labels, or maps. Cumulative duration sums
saturate at Long.MAX_VALUE. A reader tries at most three times, never locks or waits
for the simulation, and returns `consistent=false` if those attempts overlap
writes. Report or discard such a sample rather than treating it as an exact
interval boundary. Snapshot allocation happens only on observation.

## Process contract

`ServerRuntimeMetrics.Snapshot` contains:

- `sampledAtNanos`: process-local monotonic sample time, for interval differences;
  it is not an epoch timestamp and cannot be compared across processes/restarts.
- `processUptimeMillis`: JVM uptime in milliseconds.
- `processCpuTimeNanos`: cumulative CPU time of this process, nullable when the
  JDK/OS cannot provide it. Never substitute zero for unavailable data.
- `availableProcessors`: processors currently reported available to the JVM.
- `heapUsedBytes`, `heapCommittedBytes`, `heapMaxBytes` and the equivalent
  `nonHeap*Bytes`: standard JDK memory-pool observations in bytes. A maximum of
  -1 means undefined. Heap/non-heap usage is **not** resident process memory.
- `simulation`: the simulation snapshot above.

The sampler reads cached JDK management beans on demand. It does not wait to
average CPU usage, start background work, force GC, or read files/network data.
The JDK and simulation observations are sequential, not one atomic system-wide
measurement. Sampling should happen outside the simulation callback.

For samples from the same process, CPU percent of one core is
`100 * delta(processCpuTimeNanos) / delta(sampledAtNanos)`; it can exceed 100%.
Normalize by available processors only if explicitly labeling that metric as a
share of available CPU capacity. Do not compute a delta across restarts, missing
CPU observations, or a nonpositive time interval. Tick mean is
`delta(totalTickNanos) / delta(measuredTicks)` for nonzero measured ticks.
Maxima are lifetime maxima; these aggregates cannot yield percentiles.

## Operator endpoint and soak reporting

`GET /admin/metrics` returns only the bounded scalar process and simulation snapshot
described above. A missing or invalid bearer credential is refused. The route is
for local operator use even when the gameplay endpoint uses remote TLS. Do not log,
publish, or copy the private control token into reports.

The native adapter soak can read the private token file through the optional
`T4C_SOAK_CONTROL_FILE` environment variable. It samples once at startup and on
each five-second report cycle, without printing the token or file path. Its JSON
`serverRuntime` object records availability, successful/failed sample counts,
and bounded `initial`, `latest`, and numeric lifetime `max` snapshots. Without the
variable, `available` is false and no operator request is made. A failed operator
request is recorded as unavailable until a successful sample arrives. A snapshot
may have `simulation.consistent=false`; treat that sample as approximate and do
not calculate an exact interval from it.

## Integration and remaining acceptance evidence

Keep all player, account, session, host, and credential identifiers out of this
snapshot. This is operator data, not public gameplay state.

Still required: network request/response byte counts and throughput, database
operation latency/error and pool-wait observations, process resident/native memory
if required by the capacity budget, and sustained native-client load evidence with
latency/deadline observations and reconnect/restart checks. A wall-clock ticks-per-
second proxy alone cannot substitute for these measurements. CPU/memory snapshots
and the scheduler counters here are groundwork, not a completed benchmark.

## Reproduction and recorded evidence

Full PostgreSQL reactor `mvn -B -f multiplayer/pom.xml verify` passed: 18 domain and 83 server
checks, zero failures/errors/skips. Simulation tests deterministically inject a 600 ms stall and
callback failure, preserving fixed gameplay deltas and measuring actual debt/reset behavior.
The existing HTTP suite verifies private-token refusal, valid scalar units, no token echo and
unchanged public health. Log: `$env:TEMP/t4c-0198-integrated-verify.log`.

After restart, a 20-second 20-native-adapter run with the operator file recorded five successful
samples, zero metric/adapter failures and no credential/path in the report. All 20 were connected
for 19.012 seconds. A separate 10-second run without that variable reported metrics unavailable,
samples 0/failures 0 and normal gameplay; all 20 were connected for 9.097 seconds. Reports:
`target/native-adapter-soak-0198-metrics-smoke.json` and
`target/native-adapter-soak-0198-no-control-smoke.json`. These validate reporting, not sustained play.

A focused HTTP shim reproduced a 200-header/stalled-body response exceeding the old 2 s request
setting: 5.123 seconds. Explicit future deadline/cancellation reduced the same check to 2.021 seconds
and one failed metric sample. Reproduce using `tools/NativeAdapterSoakDeadlineProbe.java` as
shown in tools/NATIVE_ADAPTER_SOAK.md. The response cap is 64 KiB and token-file reads are bounded;
raw errors, credentials and file paths are not included in reports. No mutation retries are added.

## Combined source check after loot groundwork integration

On the integrated source containing the separate T4C-0201/0202 loot-rule delivery,
the focused desktop package passed 19 tests and the PostgreSQL reactor passed 21
domain plus 86 server tests, with zero failures, errors or skips. Logs:
`$env:TEMP/t4c-0198-combined-desktop.log` and
`$env:TEMP/t4c-0198-combined-server.log`. The durable stalled-body probe returned
in 2.026 seconds with one failed sample, staying within its bounded deadline.

The restarted matching QA server then completed a 15-second 20-native-adapter
reporting check: all 20 adapters were connected for 13.975 seconds, four private
operator samples succeeded with zero metric/adapter/health failures, and the report
contained no control token or path. It recorded eight attacks, six accepted commands,
three HP changes and an observed 34.029 Hz tick rate. The report is
`target/native-adapter-soak-0198-combined-smoke.json` (ignored local evidence).
This check does not replace the two-hour mixed Beta run or rendered-client QA.

## Retained two-hour native-adapter baseline (T4C-0198)

Read-only assessment of retained %TEMP%/t4c-0198-soak-t4c_soak_0198_4e09eaef1c6a/soak-report.json and status.json on 2026-10-27. The report SHA-256 is 76B0DD1C0B1A29E06CAEF09073685FFDE94C2BE90227B74AC9796C8AC62E03F7 and status SHA-256 FF53DB2F7DB08134F72A9575C0B7D78CC3982F4810B8E092F84016050C56FDF1. Metadata records immutable source 0fb90aeb5ff61a937b86d78b03077d1e099f863a, protocol 4/schema 10, server JAR SHA-256 FC54A617660D0DDEAD2757FE59C0B3EA205DF640A49A33E799AEC964C71BEEC8, client JAR C7CAA3512E24442C7641044520B5E543522C3C5987590FB9C64DD8D8DB751890, and soak source 6C761B92C68C0B572E4005BB2568ED8E8A53E2F9EADE24364DB3424394653003. Those three file hashes matched the retained runtime and current harness source. Private control/profiles were not read or copied during assessment.

| Measure | Retained result |
| --- | --- |
| Duration and occupancy | 7,200.031 s; maximum 20; 3,492.784 s (58.213 min, 48.511%) at 20, 3,706.076 s (61.768 min) at 19; brief 16/0 transitions |
| Lifecycle and failures | Adapter/server exit 0; supervisor/server/adapter PIDs gone; loopback18770 listener absent; 0 health failures, 0 metric sample failures, 0 player failures/errors; all three stderr logs empty |
| Activity | 6,428 attacks, 41 recoveries; 4,461 accepted/2,008 rejected commands; 1,151 reconnects; 525,830 movement observations; 1,785 HP changes; 0 world transfers; 0 pending commands at report |
| Command latency | 6,469 observations: 1,573 <=250 ms, 4,877 <=500 ms, 19 <=1000 ms, none above1000; no exact percentile or DB-operation latency implied |
| Health and ticks | Mean health request 5.990 ms; 33.999 observed ticks/s; 244,720 completed/0 failed ticks; mean callback 0.555 ms, maximum147.470 ms; 2 over-budget callbacks, 1 deadline reset, maximum start lateness121.637 ms, maximum deadline debt134.751 ms |
| Operator JVM metrics | 1,428 successful/0 failed samples; process CPU delta 10.889% of one core on average (28 reported available processors); used heap latest548.189 MiB/peak612.172 MiB; nonheap peak40.231 MiB. These are JVM memory pools, not resident memory. |
| Database snapshot counters | Deadlocks0 before/after; rollbacks0→56, commits195→285,037, DB available at both endpoints. Snapshot counters do not identify the cause of 56 rollbacks or provide per-operation latency/pool waits. |

The loop was running and internally consistent at the last operator sample; the report subsequently finished and the supervisor recorded both child exit codes 0. The sampled simulation had one schedule reset and two callbacks over the 29.412 ms step budget; its lifetime maxima are isolated spikes, not latency percentiles. Twenty connected clients did not remain present for two full hours, primarily because the harness intentionally reconnected each adapter 57–58 times. This ran native network adapters, not 20 rendered clients, and its activity omitted quest, loot, travel, mixed two-player play and actual network loss. It lacks byte throughput, DB operation/pool-wait latency and resident/native-memory observations. Preserve previous short-run, deadline-reproduction and failed diagnostic evidence above. This is a completed narrow baseline with useful measurements, not the final mixed 20-player Beta or M5 acceptance pass.
