<#
.SYNOPSIS
  Counts the evidence in the output directory of run-rendered-mixed-session.ps1 (T4C-0363). Reads only that directory.

.DESCRIPTION
  Per client log: ERROR / WARN / Exception / stack-trace line counts, "M5 DOWN" disconnect counts, every "M5 COMPARE" verdict, the
  M5 SAMPLE heap range. Per server log (all restarts): "Online persistence failed" lines grouped by operation, other ERROR/WARN/Exception
  lines. Process samples (samples.csv): working set and CPU range per process. Prints to the console; never reads profiles or tokens.
#>
param([Parameter(Mandatory = $true)][string]$OutDir)
$ErrorActionPreference = 'Stop'
Write-Output "== client logs"
foreach ($c in 'm5A', 'm5B') {
  $lines = @(Get-Content -LiteralPath (Join-Path $OutDir "client-$c.out.log"), (Join-Path $OutDir "client-$c.err.log") -ErrorAction SilentlyContinue)
  $err = @($lines | Where-Object { $_ -match '\bERROR\b' }).Count
  $warn = @($lines | Where-Object { $_ -match '\bWARN\b' }).Count
  $exc = @($lines | Where-Object { $_ -match 'Exception|^\s+at com\.' }).Count
  $down = @($lines | Where-Object { $_ -match 'M5 DOWN' }).Count
  $heap = @($lines | Where-Object { $_ -match 'M5 SAMPLE' } | ForEach-Object { if ($_ -match 'heapMB (\d+)') { [int]$Matches[1] } })
  $range = if ($heap.Count) { ('{0}..{1} MB over {2} samples' -f ($heap | Measure-Object -Minimum).Minimum, ($heap | Measure-Object -Maximum).Maximum, $heap.Count) } else { 'no samples' }
  Write-Output ("{0}: {1} lines, ERROR {2}, WARN {3}, exception/stack lines {4}, disconnects (M5 DOWN) {5}, heap {6}" -f $c, $lines.Count, $err, $warn, $exc, $down, $range)
  $lines | Where-Object { $_ -match 'M5 COMPARE|M5 PK (after|final)|M5 RK RETRY|M5 QUEST DONE|M5 SESSION SUMMARY|TIMEOUT' } | ForEach-Object { '  ' + $_.Substring(0, [Math]::Min(260, $_.Length)) }
}
Write-Output "== server logs"
$all = @()
foreach ($d in Get-ChildItem -LiteralPath $OutDir -Directory -Filter 'server-state-*') {
  foreach ($f in 'stdout.log', 'stderr.log') {
    $p = Join-Path $d.FullName $f
    if (Test-Path -LiteralPath $p) { $all += , @($d.Name, $f, @(Get-Content -LiteralPath $p)) }
  }
}
$ops = @{}
foreach ($entry in $all) {
  $other = @($entry[2] | Where-Object { $_ -match '\bERROR\b|\bWARN\b|Exception' -and $_ -notmatch 'persistence failed' }).Count
  $pf = @($entry[2] | Where-Object { $_ -match 'persistence failed' })
  foreach ($l in $pf) { $op = if ($l -match 'operation=([A-Za-z_]+)') { $Matches[1] } else { 'unparsed' }; $ops[$op] = 1 + [int]$ops[$op] }
  Write-Output ("{0}/{1}: {2} lines, persistence-failed {3}, other ERROR/WARN/Exception {4}" -f $entry[0], $entry[1], $entry[2].Count, $pf.Count, $other)
}
if ($ops.Count) { Write-Output ("persistence-failed by operation: " + (($ops.GetEnumerator() | Sort-Object Name | ForEach-Object { "$($_.Name)=$($_.Value)" }) -join ', ')) }
Write-Output "== process samples (samples.csv)"
$rows = @(Import-Csv -LiteralPath (Join-Path $OutDir 'samples.csv'))
foreach ($g in $rows | Group-Object process) {
  $ws = $g.Group | ForEach-Object { [int]$_.workingset_mb }
  $cpu = @($g.Group | Where-Object { $_.cpu_percent_of_one_core -ne '' } | ForEach-Object { [double]$_.cpu_percent_of_one_core })
  Write-Output ("{0}: {1} samples, working set {2}..{3} MB, CPU {4}" -f $g.Name, $g.Count, ($ws | Measure-Object -Minimum).Minimum, ($ws | Measure-Object -Maximum).Maximum,
    $(if ($cpu.Count) { '{0:N1}..{1:N1}% of one core, mean {2:N1}%' -f ($cpu | Measure-Object -Minimum).Minimum, ($cpu | Measure-Object -Maximum).Maximum, ($cpu | Measure-Object -Average).Average } else { 'n/a' }))
}
