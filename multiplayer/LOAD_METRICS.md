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
