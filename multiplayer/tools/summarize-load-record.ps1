<#
.SYNOPSIS
  Turns a NativeAdapterSoak JSON report (sampled with T4C_SOAK_CONTROL_FILE) into the M5 load-record lines (T4C-0344).

.DESCRIPTION
  Pure arithmetic over the report's own numbers; nothing is estimated. Rates divide the difference between the
  `latest` and `initial` operator snapshots by the difference of their monotonic sample times, so a snapshot pair from
  one server process is required. Every line states what a number is and what it is not (for example the game-socket
  byte counts are application-protocol bytes read from and written to the TCP socket, not NIC/wire bytes, and the
  database "operation" is the time one pooled connection was held, in whole milliseconds).
#>
param(
  [Parameter(Mandatory)][string]$Report,
  [string]$ProcessSamples,
  [long]$ServerStderrBytes = -1
)
$ErrorActionPreference = 'Stop'
$r = Get-Content -LiteralPath $Report -Raw | ConvertFrom-Json
function F([double]$v, [int]$d = 1) { [math]::Round($v, $d).ToString([Globalization.CultureInfo]::InvariantCulture) }
$sec = $r.elapsedMillis / 1000.0
"RECORD report finished=$($r.finished); elapsed $(F $sec 1) s ($(F ($sec / 60) 2) min); protocol $($r.protocolVersion)"

# --- occupancy
$c = @($r.connectedMillisByCount); $total = ($c | Measure-Object -Sum).Sum
$at20 = $c[20]; $at19up = $c[19] + $c[20]
"OCCUPANCY maximum $($r.maximumSnapshotPlayers); at 20 adapters $(F ($at20 / 1000) 1) s ($(F (100 * $at20 / $total) 1)%); at 19 or more $(F ($at19up / 1000) 1) s ($(F (100 * $at19up / $total) 1)%); at 0 $(F ($c[0] / 1000) 1) s (the join phase before the first adapter connects)"

# --- adapters and errors
$p = @($r.players)
$sum = { param($n) ($p | Measure-Object -Property $n -Sum).Sum }
$kinds = @{}
foreach ($x in $p) { foreach ($e in @($x.errors)) { if ($e) { $kinds[$e] = 1 + [int]$kinds[$e] } } }
$kindText = if ($kinds.Count) { (($kinds.GetEnumerator() | Sort-Object Name | ForEach-Object { "$($_.Name) x$($_.Value) adapters" }) -join '; ') } else { 'none' }
"ADAPTERS $($p.Count) adapters; attacks $(& $sum 'attacks'); recoveries $(& $sum 'recoveries'); accepted commands $(& $sum 'accepted'); rejected commands $(& $sum 'rejected'); HP changes $(& $sum 'hpChanges'); movement observations $(& $sum 'movementObservations'); reconnects $(& $sum 'reconnects'); region transfers $(& $sum 'regionTransfers'); adapter failures $(& $sum 'failures')"
"ERRORS adapter error kinds: $kindText; health request failures $($r.healthFailures); mean health request $(F $r.healthMeanMillis 3) ms"
$buckets = @(0) * 8; foreach ($x in $p) { for ($i = 0; $i -lt 8; $i++) { $buckets[$i] += [long]$x.latencyBuckets[$i] } }
$lb = @($r.latencyBucketsMillis); $obs = ($buckets | Measure-Object -Sum).Sum
"COMMAND LATENCY (adapter send to result) $obs observations: " + ((0..7 | ForEach-Object { "$($lb[$_]) ms $($buckets[$_])" }) -join ', ') + '  (bucketed; no percentile is implied beyond the bucket bounds)'
if ($r.portalWalk) { "PORTAL WALK walkers $($r.portalWalk.walkers), interval $($r.portalWalk.intervalSeconds) s, dwell $($r.portalWalk.dwellSeconds) s" }

# --- server runtime (operator metrics)
$rt = $r.serverRuntime
if (-not $rt.available) { "RUNTIME operator metrics were NOT available (no T4C_SOAK_CONTROL_FILE); no tick, CPU, memory, bandwidth or database numbers"; return }
$i0 = $rt.initial; $i1 = $rt.latest; $mx = $rt.max
"RUNTIME operator samples $($rt.samples) ok, $($rt.failures) failed"
$dt = ($i1.sampledAtNanos - $i0.sampledAtNanos) / 1e9
$s0 = $i0.simulation; $s1 = $i1.simulation
$ticks = $s1.measuredTicks - $s0.measuredTicks
"TICKS interval $(F $dt 1) s: $($s1.completedTicks - $s0.completedTicks) completed, $($s1.failedTicks - $s0.failedTicks) failed ticks; observed $(F ($ticks / $dt) 3) ticks/s (step $(F ($s1.stepNanos / 1e6) 3) ms = $(F (1e9 / $s1.stepNanos) 3) Hz); mean callback $(F (($s1.totalTickNanos - $s0.totalTickNanos) / [math]::Max(1, $ticks) / 1e6) 3) ms; lifetime max callback $(F ($mx.simulation.maxTickNanos / 1e6) 2) ms; over-budget callbacks $($s1.overBudgetTicks) (callback longer than one step)"
"TICK DEBT lifetime max start lateness $(F ($mx.simulation.maxStartLatenessNanos / 1e6) 2) ms; max deadline debt $(F ($mx.simulation.maxDeadlineDebtNanos / 1e6) 2) ms; deadline resets $($s1.deadlineResets) (debt over four steps); discarded schedule $(F ($s1.discardedScheduleNanos / 1e6) 1) ms; consistent samples only if the report says so"
if ($null -ne $i0.processCpuTimeNanos -and $null -ne $i1.processCpuTimeNanos) {
  $cpu = 100.0 * ($i1.processCpuTimeNanos - $i0.processCpuTimeNanos) / ($i1.sampledAtNanos - $i0.sampledAtNanos)
  "CPU server process $(F $cpu 1)% of one core over the interval ($(F ($cpu / $i1.availableProcessors) 1)% of $($i1.availableProcessors) logical processors); the interval includes the join phase"
}
"MEMORY JVM heap used at the last sample $(F ($i1.heapUsedBytes / 1MB) 1) MB (lifetime max $(F ($mx.heapUsedBytes / 1MB) 1) MB, committed max $(F ($mx.heapCommittedBytes / 1MB) 1) MB); non-heap used $(F ($i1.nonHeapUsedBytes / 1MB) 1) MB (max $(F ($mx.nonHeapUsedBytes / 1MB) 1) MB). Heap use is a sawtooth between collections, not resident memory"
if ($ProcessSamples -and (Test-Path $ProcessSamples)) {
  $ps = @(Import-Csv $ProcessSamples)
  if ($ps.Count -gt 0) {
    $ws = $ps | ForEach-Object { [double]$_.WorkingSetMB }
    "RESIDENT Windows working set of the server process: first $(F $ws[0] 1) MB, last $(F $ws[-1] 1) MB, min $(F ($ws | Measure-Object -Minimum).Minimum 1) MB, max $(F ($ws | Measure-Object -Maximum).Maximum 1) MB over $($ps.Count) samples every 15 s; threads $($ps[0].Threads) to $($ps[-1].Threads) (max $(($ps | ForEach-Object { [int]$_.Threads } | Measure-Object -Maximum).Maximum)); handles $($ps[0].Handles) to $($ps[-1].Handles)"
  }
}
$g0 = $i0.gameSocket; $g1 = $i1.gameSocket
if ($g1) {
  $rd = $g1.bytesRead - $g0.bytesRead; $wr = $g1.bytesWritten - $g0.bytesWritten
  "BANDWIDTH game socket (application protocol bytes the server read from / wrote to its TCP sockets; excludes TCP/IP headers): server to clients $(F ($wr / $dt / 1024) 1) KiB/s ($(F ($wr / $dt / 1024 / [math]::Max(1, $p.Count)) 2) KiB/s per adapter, total $(F ($wr / 1MB) 1) MiB), clients to server $(F ($rd / $dt / 1024) 2) KiB/s (total $(F ($rd / 1MB) 2) MiB); connections accepted $($g1.connectionsAccepted), refused full $($g1.connectionsRefusedFull), open now $($g1.connectionsOpen)"
}
$h0 = $i0.httpTraffic; $h1 = $i1.httpTraffic
if ($h1) { "BANDWIDTH http-handler bodies (application bodies of completed handlers; native game-socket requests are dispatched through the same handlers, so this counts them too and is the smaller, unframed view of the socket figure above; headers and frame JSON excluded): read $($h1.requestBodyBytesRead - $h0.requestBodyBytesRead) B, written $($h1.responseBodyBytesFullyWritten - $h0.responseBodyBytesFullyWritten) B, completed requests $($h1.completed - $h0.completed), read failures $($h1.readFailures), response failures $($h1.responseFailures)" }
$d1 = $i1.database
if ($d1) {
  $d0 = $i0.database
  $ops = $d1.operations - $d0.operations
  $bk = @($d1.operationBuckets); $ub = @($d1.upperBoundsMillis)
  $names = (0..($bk.Count - 1) | ForEach-Object { if ($_ -lt $ub.Count) { "<=$($ub[$_]) ms" } else { ">$($ub[-1]) ms" } })
  "DATABASE OPERATIONS (one pooled connection held from borrow to return, whole ms, includes SQL, transaction and driver round trips) $($d1.operations) total: mean $(F ($d1.operationTotalMillis / [math]::Max(1, $d1.operations)) 3) ms, lifetime max $($d1.operationMaxMillis) ms; buckets: " + ((0..($bk.Count - 1) | ForEach-Object { "$($names[$_]) $($bk[$_])" }) -join ', ') + "; rate over the interval $(F ($ops / $dt) 1) operations/s"
  $ab = @($d1.acquireBuckets)
  "DATABASE POOL WAIT acquisitions $($d1.acquired): mean $(F ($d1.acquireTotalNanos / [math]::Max(1, $d1.acquired) / 1e6) 3) ms, lifetime max $(F ($d1.acquireMaxNanos / 1e6) 2) ms; buckets: " + ((0..($ab.Count - 1) | ForEach-Object { "$($names[$_]) $($ab[$_])" }) -join ', ') + "; acquire timeouts $($d1.acquireTimeouts); pool size at the last sample active $($d1.poolActive) idle $($d1.poolIdle) pending $($d1.poolPending) total $($d1.poolTotal) of $($d1.poolMaximum); connections created after startup $($d1.connectionsCreated)"
} else { "DATABASE operation latency: the server reported none" }
if ($r.databaseBefore -and $r.databaseAfter -and $r.databaseAfter.available) {
  "DATABASE SERVER (pg_stat_database) commits +$($r.databaseAfter.commits - $r.databaseBefore.commits), rollbacks +$($r.databaseAfter.rollbacks - $r.databaseBefore.rollbacks), deadlocks +$($r.databaseAfter.deadlocks - $r.databaseBefore.deadlocks), database size +$(F (($r.databaseAfter.bytes - $r.databaseBefore.bytes) / 1MB) 2) MB, backend connections $($r.databaseBefore.connections) to $($r.databaseAfter.connections)"
}
if ($ServerStderrBytes -ge 0) { "STDERR server stderr bytes at the end: $ServerStderrBytes" }
