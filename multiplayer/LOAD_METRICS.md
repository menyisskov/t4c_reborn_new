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

### Bounded HTTP application traffic (T4C-0227)

`GET /admin/metrics` also returns `httpTraffic` from the same private, loopback-only
operator route. Sampling is read-only and creates no polling thread or reset. Each
request that reaches `PreviewServer.handle` increments `inflight`, then contributes
one completed observation after its exchange closes and its admission permit is
released. Requests rejected by the HTTP executor before handler dispatch are outside
this measurement. The metrics request being sampled is still inflight in its own
snapshot and appears among completed requests in a later sample.

`requestBodyBytesRead` counts bytes successfully returned by the application's
bounded body reads, including bytes read before an I/O exception. It excludes
headers and any JDK request-body drain after the handler. `responseBodyBytesFullyWritten`
adds a response body's byte length only when its entire application output-stream
write returns successfully. A partial or failed write increases `responseFailures`
but contributes no guessed partial byte count. These byte counters describe completed
handlers and are lower bounds on application work; they are **not** TCP, TLS, header,
wire bandwidth, client receipt, or delivery acknowledgements. `readFailures` counts
body-read I/O exceptions. `headersSent` counts successful returns from
`sendResponseHeaders`, not proof of client receipt. `selectedStatus` is the last
status the application selected for a response attempt; a failed send or a later
error-response attempt can make it differ from the status a client saw.

`handlerLifetimeBuckets` count time from handler dispatch through the exchange
close and permit release, using monotonic time. Upper bounds are 1, 5, 20, 100,
500, 2,000 and 10,000 milliseconds; the final bucket is over 10,000 ms. This
includes body waits, world/database work and close-time draining, but excludes
socket acceptance, executor admission wait, client command preparation and network
delivery after the application write. `completed` and the histogram count each
finished handler once. Snapshots are synchronized observations of these counters;
the JVM and simulation fields are sampled separately.

Rows use only fixed known route, GET/POST/OTHER method, and selected status labels;
unknown routes and codes collapse to `other`. No raw path, query, IP address,
account, token, header or payload enters a label. Fixed-size arrays and bounded
histograms retain no request history. These numbers support local interval deltas
between snapshots of one server process, not a bandwidth or latency percentile
claim across restarts.

The optional native soak sampler keeps a finite 1 MiB whole-response cap and
two-second deadline. A measured dense upper schema of all 897 fixed
route/method/status rows with `Long.MAX_VALUE` numeric values serialized to
434,724 bytes; a representative dense valid snapshot was 433,111 bytes.
Both exceed the old 64 KiB consumer cap, which could have marked metrics
unavailable during a long varied run. The larger cap is still bounded and
does not turn these application counters into wire-bandwidth measurements.

Keep all player, account, session, host, and credential identifiers out of this
snapshot. This is operator data, not public gameplay state.

T4C-0227 left these unmeasured; T4C-0352 (next section) adds game-socket bytes, database operation time,
pool wait and timeouts, and a Windows working-set sampler, and records one 30-minute run with all of them. Still not
measured: NIC-level bytes including TCP/IP headers, TLS framing (no TLS run), remote latency, process native memory beyond
the working set, and a sustained run on the integrated release candidate. A wall-clock ticks-per-second proxy alone cannot
substitute for these measurements.

### Game-socket bytes and database operation time (T4C-0352)

`GET /admin/metrics` now also returns `gameSocket` and `database` objects next to `httpTraffic`. Both are
fixed-cardinality counters with no per-connection, per-route, per-account or per-statement label and no history.

- `gameSocket`: `connectionsAccepted`, `connectionsRefusedFull`, `connectionsOpen`, `bytesRead`, `bytesWritten`. The counters
  wrap the accepted TCP socket's streams, so they are the application-protocol bytes the server actually read from and
  handed to the socket (every frame header and payload byte, the version handshake included). They exclude TCP/IP headers,
  retransmissions and TLS framing, which the JVM cannot see; on loopback there is no other wire. This is the native
  clients' real traffic. `httpTraffic` bodies also count these requests, because socket requests are dispatched through
  the same handlers, but without the frame JSON around each body, so the socket figure is the larger and more honest one.
- `database` (persistent server only, absent when the store is an injected connection factory): taken from the owned
  pool's metrics hook. `operations` and its histogram are the time a pooled connection was held from borrow to return, i.e.
  one store operation's SQL, transaction and driver round trips, in **whole milliseconds** as the pool reports them (an
  operation under 1 ms lands in the first bucket, so the mean is only as precise as that rounding). `acquired` and the
  `acquire*` fields are the pool wait in nanoseconds; `acquireTimeouts` counts failed acquisitions; `pool*` are the gauges
  at the moment of the sample (active, idle, pending, total, maximum); `connectionsCreated` counts connections opened after
  the tracker exists (the fail-fast first one is not counted). Histogram bounds are 1, 5, 20, 100, 500, 2,000 ms and an
  overflow bucket. A pool-level view cannot name which operation was slow, and it does not include time queued for a
  worker thread before the borrow.

`run-native-load-record.ps1` runs a twenty-adapter soak on a disposable cluster with the operator file set, samples the
server's Windows working set, private bytes, CPU seconds, thread and handle counts every 15 s, stops the server through the
private stop (must exit 0) and `summarize-load-record.ps1` turns the soak report into the record lines below. All rates are
differences between the first and last operator snapshot of one server process; nothing is estimated by hand.

#### Recorded 30-minute record (T4C-0352, 2026-10-30, local PG16 WSL, protocol 8, schema 13, `origin/main` 3e147d8b plus this branch)

Twenty native adapters (default 120 s reconnect churn), eight of them taking paced portal trips (30 s interval, 10 s dwell),
disposable cluster `t4c_acc_115677693125` on 57703 (destroyed), server on 18841. The soak tool, the graceful stop (HTTP 200,
exit 0) and the run all finished clean; server stderr was empty (0 bytes).

| Measure | Result |
| --- | --- |
| Duration, occupancy | 1,800.1 s; maximum 20; 930.7 s (51.7%) at 20 adapters, 1,760.6 s (97.8%) at 19 or more, 0.8 s with none (the 20 are never all connected at once because each reconnects every 120 s on purpose) |
| Tick duration | 61,154 ticks completed, 0 failed, 0 over budget; observed 34.000 ticks/s against the 34 Hz step (29.412 ms); mean callback 1.258 ms; maximum callback 22.57 ms |
| Tick debt | maximum start lateness 21.32 ms, maximum deadline debt 0 ms, 0 deadline resets, 0 ms discarded schedule |
| CPU | 16.8% of one core (0.6% of 28 logical processors) over the interval |
| Memory | JVM heap used maximum 607.6 MB of 1,024 MB committed (340.4 MB at the end); non-heap 47.9 MB; Windows working set 808.8 MB at the first sample, 850 to 863 MB from minute 10 to the end (max 863.3 MB, last 856.2 MB); threads 82 to 91 (max 96); handles 699 to 814, flat after the first minutes. No growth after warm-up |
| Bandwidth (game socket) | server to clients 3,500.3 KiB/s total = 175.0 KiB/s per adapter (6,148.3 MiB over the run); clients to server 25.3 KiB/s total (44.5 MiB); 312 connections accepted over the run (the churn), 0 refused full, 20 open at the end. Application-protocol bytes, no TCP/IP headers |
| Bandwidth (handler bodies) | 5,668,659,053 B written and 2,974,304 B read by 626,298 completed handlers, 0 read or response failures (the unframed view of the same traffic) |
| Database operation latency | 157,169 operations (85.8 per second): mean 0.941 ms, maximum 148 ms; <=1 ms 110,660, <=5 ms 45,993, <=20 ms 492, <=100 ms 22, <=500 ms 2, none above 500 ms |
| Database pool wait | 157,169 acquisitions: mean 0.014 ms, maximum 8.27 ms; <=1 ms 157,130, <=5 ms 36, <=20 ms 3; 0 acquire timeouts; pool never above 8 connections (4 total, 0 active, 0 pending at the end; 12 connections created after startup) |
| Database server | 224,240 commits, 0 rollbacks, 0 deadlocks, +1.49 MB, backend connections 7 to 5 |
| Command latency (adapter send to result) | 988 observations: <=250 ms 92, <=500 ms 880, <=1,000 ms 16, none above 1,000 ms |
| Gameplay | 982 attacks (679 accepted, 309 rejected commands of all kinds), 6 recoveries, 2,327 HP changes, 273,606 movement observations, 248 region transfers by the eight walkers (114 of 122 started trips completed, 1 aborted, 0 leg timeouts) |
| Errors and recovery | 0 adapter failures, no adapter error kind, 0 health failures (mean health request 0.86 ms), 0 operator-sample failures of 358; 272 reconnects, all the scheduled churn (13 or 14 per adapter), each rejoined without a failure |

This completes the missing bandwidth and database-latency measures and is clean, but it is a 30-minute native-adapter run on the
development build (not the integrated release candidate, not rendered clients, not remote). The two-hour run in T4C-0315
remains the long-duration baseline (flat CPU and heap, 625 transfers); compare, do not merge, the two.

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
