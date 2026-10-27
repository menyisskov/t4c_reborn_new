# Load observations (T4C-0198)

Delivery status (2026-10-27): this document describes pending implementation in the
working tree based on `397d9ace`. That runtime/tooling is not yet merged. Recorded
checks apply only to the stated build; this documentation does not mark a milestone
complete or make the corresponding code available in an earlier release.

Implemented groundwork: `SimulationLoop.metrics()` and
`new ServerRuntimeMetrics(loop).snapshot()`. These methods are not yet wired to an
HTTP endpoint or soak report. They do not establish that the sustained 20-player
Beta performance gate has passed.

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

## Integration and remaining acceptance evidence

The parent task still needs to expose a bounded, appropriately authorized operator
observation route and record samples in the 20-client soak harness. Keep all player,
account, session, host, and credential identifiers out of this snapshot. This is
operator data, not public gameplay state.

Still required: network request/response byte counts and throughput, database
operation latency/error and pool-wait observations, process resident/native memory
if required by the capacity budget, and sustained native-client load evidence with
latency/deadline observations and reconnect/restart checks. A wall-clock ticks-per-
second proxy alone cannot substitute for these measurements. CPU/memory snapshots
and the scheduler counters here are groundwork, not a completed benchmark.
