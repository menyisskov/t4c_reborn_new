<#
.SYNOPSIS
  Twenty-adapter fault drill (T4C-0310): hard-kill the game server, or abruptly crash PostgreSQL, in the middle of a
  paced NativeAdapterSoak, then check recovery against the committed database.

.DESCRIPTION
  Mode ServerKill   : the owned ServerMain child is terminated with Stop-Process -Force (no /admin/shutdown), restarted
                      on the same database, and the adapters rejoin after real lease expiry.
  Mode DatabaseCrash: the owned acceptance cluster is stopped immediately (acceptance-cluster.ps1 -Action Crash, an
                      immediate-mode pg_ctl stop of the verified postmaster), the server stays running and must fail
                      closed, then the cluster is started again (crash recovery) and the server must recover.

  Mode ShutdownOutage (T4C-0344): PostgreSQL is crashed as in DatabaseCrash and the private operator stop is requested
                      immediately, while sessions are still bound. The stop must NOT report success: the server must exit
                      non-zero within 120 s with a failure line on stderr, committed state must survive, the unreleased
                      leases must expire on their own clock, and a restarted server must serve all twenty adapters again.

  Everything is disposable: a new acceptance cluster (random t4c_acc_ id) on -DbPort, a copied runtime in TEMP, one
  ServerMain --online child on -ServerPort. Refuses 55432, 55433, 18765, 18766, 18770, 18771 and any listening port.
  Password handling is that of acceptance-cluster.ps1 RunEnv (process-scoped, never printed). The cluster is destroyed
  at the end only after a complete run, unless -KeepCluster is given. Evidence goes to a new TEMP directory.

  Pass criteria are written down in NATIVE_FAULT_DRILL.md. The script prints the drill log and the verifier's result.
#>
param(
  [Parameter(Mandatory)][ValidateSet('ServerKill', 'DatabaseCrash', 'ShutdownOutage', 'LostAck')][string]$Mode,
  [Parameter(Mandatory)][ValidateRange(1024, 65535)][int]$DbPort,
  [Parameter(Mandatory)][ValidateRange(1024, 65535)][int]$ServerPort,
  [ValidateRange(120, 3600)][int]$Seconds = 330,
  [ValidateRange(30, 600)][int]$FaultAtSeconds = 90,
  [ValidateRange(3, 120)][int]$OutageSeconds = 12,
  [switch]$KeepCluster,
  [string]$RunId,          # inner use: the cluster id (set when re-invoked through RunEnv)
  [string]$Work            # inner use
)
$ErrorActionPreference = 'Stop'
$protected = 55432, 55433, 18765, 18766, 18770, 18771
foreach ($p in @($DbPort, $ServerPort)) { if ($protected -contains $p) { throw "Port $p is protected" } }
if ($DbPort -eq $ServerPort) { throw 'Database and server ports must differ' }
$root = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$cluster = Join-Path $PSScriptRoot 'acceptance-cluster.ps1'

if (-not $RunId) {
  # ---- outer: create the cluster, run the inner script with the private database environment, destroy.
  foreach ($p in @($DbPort, $ServerPort)) {
    if (Get-NetTCPConnection -State Listen -LocalPort $p -ErrorAction SilentlyContinue) { throw "Port $p is already listening" }
  }
  $created = (& $cluster -Action Create -Port $DbPort | Out-String)
  if ($created -notmatch 'CREATE_OK id=(t4c_acc_[0-9a-f]{12})') { throw 'Cluster creation did not report an id' }
  $id = $Matches[1]
  $work = Join-Path $env:TEMP ("t4c-0310-$($Mode.ToLower())-" + $id)
  Write-Host "cluster $id on $DbPort; evidence $work"
  $passed = $false
  try {
    & $cluster -Action RunEnv -RunId $id -Command 'powershell.exe' -CommandArgs @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', $PSCommandPath,
        '-Mode', $Mode, '-DbPort', $DbPort, '-ServerPort', $ServerPort, '-Seconds', $Seconds, '-FaultAtSeconds', $FaultAtSeconds,
        '-OutageSeconds', $OutageSeconds, '-RunId', $id, '-Work', $work)
    $passed = $true
  } finally {
    if ($passed -and -not $KeepCluster) { & $cluster -Action Destroy -RunId $id }
    else { Write-Warning "Cluster $id kept for inspection (destroy later with acceptance-cluster.ps1 -Action Destroy)" }
  }
  return
}

# ---- inner: T4C_DB_* are set for this process only.
foreach ($n in 'T4C_DB_URL', 'T4C_DB_USER', 'T4C_DB_PASSWORD') { if (-not [Environment]::GetEnvironmentVariable($n)) { throw 'Private database environment missing' } }
New-Item -ItemType Directory -Path $Work -ErrorAction Stop | Out-Null
$rt = Join-Path $Work 'runtime'
New-Item -ItemType Directory -Path (Join-Path $rt 'lib'), (Join-Path $rt 'assets/maps/worldmap'), (Join-Path $rt 'assets/maps/dungeon'), (Join-Path $Work 'probe') | Out-Null
Copy-Item (Join-Path $root 'multiplayer/server/target/t4c-server.jar') (Join-Path $rt 't4c-server.jar')
Copy-Item (Join-Path $root 'multiplayer/server/target/lib/*') (Join-Path $rt 'lib')
$map = Join-Path $rt 'assets/maps/worldmap/worldmap.colbin'
Copy-Item (Join-Path $root 'assets/maps/worldmap/worldmap.colbin') $map
Copy-Item (Join-Path $root 'assets/maps/dungeon/DungeonMap.colbin') (Join-Path $rt 'assets/maps/dungeon/DungeonMap.colbin')
$jar = Join-Path $rt 't4c-server.jar'
$cp = (Join-Path $root 'target/classes') + ';' + (Join-Path $root 'multiplayer/server/target/classes') + ';' +
  (Join-Path $root 'multiplayer/server/target/lib/*') + ';' + (Get-Content (Join-Path $root 'target/classpath.txt') -Raw).Trim()
$probeCp = (Join-Path $Work 'probe') + ';' + $cp
$log = Join-Path $Work 'drill.log'
$t0 = Get-Date
function Say([string]$m) { $line = ('[t+{0,6:N1}s] {1}' -f ((Get-Date) - $t0).TotalSeconds, $m); Write-Host $line; Add-Content -LiteralPath $log -Value $line }
$endpoint = "http://127.0.0.1:$ServerPort"

function Start-Server([int]$n) {
  $env:T4C_CONTROL_FILE = (Join-Path $Work "control-$n.token"); $env:T4C_BIND_ADDRESS = '127.0.0.1'
  try {
    return Start-Process -FilePath 'java' -ArgumentList @('-Djava.awt.headless=true', '-jar', $jar, '--online', $map, $ServerPort) -WorkingDirectory $rt `
      -RedirectStandardOutput (Join-Path $Work "server-$n.out.log") -RedirectStandardError (Join-Path $Work "server-$n.err.log") -PassThru -WindowStyle Hidden | ForEach-Object { $null = $_.Handle; $_ }
  } finally { Remove-Item Env:\T4C_CONTROL_FILE, Env:\T4C_BIND_ADDRESS -ErrorAction SilentlyContinue }
}
function Test-Health { try { $r = Invoke-WebRequest -Uri "$endpoint/health" -UseBasicParsing -TimeoutSec 2; return ($r.StatusCode -eq 200 -and $r.Content -match 'local-persistent') } catch { return $false } }
function Wait-Health([System.Diagnostics.Process]$proc, [int]$limit = 60) {
  $until = (Get-Date).AddSeconds($limit)
  while ((Get-Date) -lt $until) { if ($proc.HasExited) { throw "Server exited early (code $($proc.ExitCode))" }; if (Test-Health) { return }; Start-Sleep -Milliseconds 300 }
  throw 'Server did not become healthy'
}
function Java-Tool([string[]]$a) { & java '-Djava.awt.headless=true' -cp $probeCp com.perso.T4C.server.NativeFaultDrill @a; if ($LASTEXITCODE -ne 0) { throw "NativeFaultDrill $($a[0]) failed ($LASTEXITCODE)" } }

$server = $null; $soak = $null; $monitor = $null
$profiles = Join-Path $Work 'profiles'; $state = Join-Path $Work 'monitor-state.json'; $stopMonitor = Join-Path $Work 'monitor.stop'; $stopSoak = Join-Path $Work 'soak.stop'
$ok = $false
try {
  & javac --release 21 -proc:none -cp $cp -d (Join-Path $Work 'probe') (Join-Path $PSScriptRoot 'NativeFaultDrill.java')
  if ($LASTEXITCODE -ne 0) { throw 'javac failed' }
  Say "mode $Mode; server port $ServerPort; db port $DbPort; soak $Seconds s; fault at $FaultAtSeconds s"
  $server = Start-Server 1; Wait-Health $server; Say "server 1 healthy (pid $($server.Id))"
  Java-Tool @('provision', $profiles, $endpoint) | Out-Null; Say 'twenty characters provisioned at the opening NPC'
  if ($Mode -eq 'LostAck') {
    # Seven rounds: join one character, send the opening greeting, hard-kill our server a few ms later (the kill races the
    # reward commit), look at the static database, restart. Then wait for lease expiry and greet again with every character.
    # Delays in ms between handing the request to the adapter and the kill. A first sweep of 0 to 32 ms always killed
    # before the commit (database empty, reply lost), so the sweep reaches into the 100 to 300 ms range.
    $delays = 0, 8, 25, 40, 55, 70, 85, 100, 120, 150, 200, 300
    for ($i = 0; $i -lt $delays.Count; $i++) {
      $o = Java-Tool @('greet-kill', $profiles, $endpoint, $server.Id, $i, $delays[$i]); Say "round $($i + 1): $o"
      $server.WaitForExit(10000) | Out-Null
      if (-not $server.HasExited) { throw 'Server survived the kill' }
      $l = Java-Tool @('letters', $profiles); Say "  database right after the kill: $l (one digit per character; the killed character is position $($i + 1))"
      $server = Start-Server (2 + $i); Wait-Health $server
    }
    Say 'waiting 32 s so every lease held by a killed session has really expired'; Start-Sleep -Seconds 32
    Java-Tool @('greet', $profiles, $endpoint) | Out-Null; Say 'retried greeting accepted for all twenty characters'
    Java-Tool @('letters', $profiles) | ForEach-Object { Say "  after retry: $_" }
    $token = (Get-Content (Join-Path $Work "control-$(1 + $delays.Count).token") -Raw).Trim()
    $resp = Invoke-WebRequest -Uri "$endpoint/admin/shutdown" -Method Post -Headers @{ Origin = $endpoint; Authorization = "Bearer $token" } -ContentType 'text/plain' -Body '' -UseBasicParsing -TimeoutSec 5
    $exited = $server.WaitForExit(100000)
    Say "final graceful shutdown: HTTP $($resp.StatusCode), exited=$exited, exit code $(if ($exited) { $server.ExitCode } else { 'n/a' })"
    if (-not $exited -or $server.ExitCode -ne 0) { throw 'Final graceful shutdown did not exit 0' }
    $server = $null
    Java-Tool @('verify', $profiles, (Join-Path $Work 'no-monitor-state.json'), (Join-Path $Work 'verify.txt'), 'expect-letters') | ForEach-Object { Say "  [final] $_" }
  } else {
  Java-Tool @('greet', $profiles, $endpoint) | Out-Null; Say 'opening letter greeted for every character'
  $monitor = Start-Process -FilePath 'java' -ArgumentList @('-Djava.awt.headless=true', '-cp', $probeCp, 'com.perso.T4C.server.NativeFaultDrill', 'monitor', $profiles, $state, $stopMonitor) `
    -RedirectStandardOutput (Join-Path $Work 'monitor.out.log') -RedirectStandardError (Join-Path $Work 'monitor.err.log') -PassThru -WindowStyle Hidden | ForEach-Object { $null = $_.Handle; $_ }
  Start-Sleep -Seconds 4
  $env:T4C_SOAK_STOP_FILE = $stopSoak
  $report = Join-Path $Work 'soak-report.json'
  $soak = Start-Process -FilePath 'java' -ArgumentList @('-Djava.awt.headless=true', '-cp', $cp, (Join-Path $PSScriptRoot 'NativeAdapterSoak.java'), $endpoint, $profiles, $Seconds,
      (Join-Path $root 'assets/maps/worldmap/worldmap.colbin'), $report, $server.Id) `
    -RedirectStandardOutput (Join-Path $Work 'soak.out.log') -RedirectStandardError (Join-Path $Work 'soak.err.log') -PassThru -WindowStyle Hidden | ForEach-Object { $null = $_.Handle; $_ }
  Remove-Item Env:\T4C_SOAK_STOP_FILE -ErrorAction SilentlyContinue
  $soakStart = Get-Date; Say "soak started (pid $($soak.Id))"
  Start-Sleep -Seconds $FaultAtSeconds
  Copy-Item $report (Join-Path $Work 'soak-report-before-fault.json') -ErrorAction SilentlyContinue
  Copy-Item $state (Join-Path $Work 'monitor-state-before-fault.json') -ErrorAction SilentlyContinue

  if ($Mode -eq 'ServerKill') {
    Copy-Item $state (Join-Path $Work 'monitor-state-at-fault.json')
    $faultMs = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
    Stop-Process -Id $server.Id -Force; Say "FAULT: server pid $($server.Id) hard-killed (Stop-Process -Force, no shutdown request)"
    $server.WaitForExit(10000) | Out-Null; Say "server process gone: HasExited=$($server.HasExited)"
    if (Test-Health) { throw 'Health still answers after the kill' }
    # The database is static now (nothing writes): everything the monitor saw committed must be there, unchanged.
    Java-Tool @('verify', $profiles, (Join-Path $Work 'monitor-state-at-fault.json'), (Join-Path $Work 'verify-after-kill-before-restart.txt'), 'expect-letters') | ForEach-Object { Say "  [after kill, before restart] $_" }
    Start-Sleep -Seconds $OutageSeconds
    $tStart = Get-Date; $server = Start-Server 2; Wait-Health $server; Say "server 2 healthy $([int]((Get-Date) - $tStart).TotalMilliseconds) ms after launch (pid $($server.Id))"
  } elseif ($Mode -eq 'ShutdownOutage') {
    # T4C-0344: an operator stop that arrives while the database is down must be a VISIBLE failure, never a safe save.
    Copy-Item $state (Join-Path $Work 'monitor-state-at-fault.json')
    $faultMs = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
    & $cluster -Action Crash -RunId $RunId | Out-Null; Say 'FAULT: PostgreSQL stopped abruptly (immediate mode) while 20 adapters play'
    $token1 = (Get-Content (Join-Path $Work 'control-1.token') -Raw).Trim()
    $resp = Invoke-WebRequest -Uri "$endpoint/admin/shutdown" -Method Post -Headers @{ Origin = $endpoint; Authorization = "Bearer $token1" } -ContentType 'text/plain' -Body '' -UseBasicParsing -TimeoutSec 5
    $stopAskedMs = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
    Say "operator stop requested $($stopAskedMs - $faultMs) ms after the crash while sessions were bound: HTTP $($resp.StatusCode)"
    $exited = $server.WaitForExit(120000)
    $stopCode = $(if ($exited) { $server.ExitCode } else { -1 })
    $stopMs = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
    $errText = Get-Content (Join-Path $Work 'server-1.err.log') -Raw -ErrorAction SilentlyContinue
    $failureLines = @(($errText -split "`n") | Where-Object { $_ -match 'persistence failed|shutdown failed|flush incomplete|did not terminate|teardown' } | ForEach-Object { $_.Trim() })
    Say "server after the stop request: exited=$exited, exit code $stopCode, $([int](($stopMs - $stopAskedMs) / 1000)) s after the request; failure lines on stderr: $($failureLines.Count)"
    foreach ($l in ($failureLines | Select-Object -First 6)) { Say "  stderr: $l" }
    if (-not $exited) { throw 'The server did not exit within 120 s of the stop request while the database was down' }
    if ($stopCode -eq 0) { throw 'DEFECT: the stop exited 0 although the database was down and sessions were bound' }
    if ($failureLines.Count -eq 0) { throw 'The failed stop printed no failure line' }
    Say 'visible failure confirmed: non-zero exit and a failure line on stderr'
    Start-Sleep -Seconds 2
    & $cluster -Action Start -RunId $RunId | Out-Null; Say 'PostgreSQL restarted (crash recovery)'
    Java-Tool @('verify', $profiles, (Join-Path $Work 'monitor-state-at-fault.json'), (Join-Path $Work 'verify-after-db-recovery.txt'), 'expect-letters') | ForEach-Object { Say "  [after database recovery] $_" }
    Start-Sleep -Seconds $OutageSeconds
    $tStart = Get-Date; $server = Start-Server 2; Wait-Health $server; Say "server 2 healthy $([int]((Get-Date) - $tStart).TotalMilliseconds) ms after launch (pid $($server.Id)); the lease of every unreleased session must expire on its own clock"
  } else {
    Copy-Item $state (Join-Path $Work 'monitor-state-at-fault.json')
    $faultMs = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
    & $cluster -Action Crash -RunId $RunId | Out-Null; Say 'FAULT: PostgreSQL stopped abruptly (immediate mode) while 20 adapters play'
    $deadline = (Get-Date).AddSeconds($OutageSeconds); $healthDuring = @()
    while ((Get-Date) -lt $deadline) { $healthDuring += $(if (Test-Health) { 'up' } else { 'down' }); Start-Sleep -Seconds 1 }
    Say ("server process alive during outage: $(-not $server.HasExited); /health samples: " + ($healthDuring -join ','))
    & $cluster -Action Start -RunId $RunId | Out-Null; Say 'PostgreSQL restarted (crash recovery)'
    # Committed work seen before the crash must have survived crash recovery (the server is still running and may have moved on).
    Java-Tool @('verify', $profiles, (Join-Path $Work 'monitor-state-at-fault.json'), (Join-Path $Work 'verify-after-db-recovery.txt'), 'expect-letters') | ForEach-Object { Say "  [after database recovery] $_" }
  }
  Copy-Item $report (Join-Path $Work 'soak-report-after-fault.json') -ErrorAction SilentlyContinue
  $remaining = [int]($Seconds - ((Get-Date) - $soakStart).TotalSeconds)
  Say "waiting for the soak to finish ($remaining s left)"
  if (-not $soak.WaitForExit(($remaining + 90) * 1000)) { New-Item $stopSoak -ItemType File | Out-Null; $soak.WaitForExit(60000) | Out-Null }
  Say "soak exited with code $($soak.ExitCode)"
  New-Item $stopMonitor -ItemType File | Out-Null; $monitor.WaitForExit(20000) | Out-Null

  # ---- graceful end: the final server must still stop cleanly through its private control path.
  $token = (Get-Content (Join-Path $Work $(if ($Mode -ne 'DatabaseCrash') { 'control-2.token' } else { 'control-1.token' })) -Raw).Trim()
  $resp = Invoke-WebRequest -Uri "$endpoint/admin/shutdown" -Method Post -Headers @{ Origin = $endpoint; Authorization = "Bearer $token" } -ContentType 'text/plain' -Body '' -UseBasicParsing -TimeoutSec 5
  $exited = $server.WaitForExit(100000)
  Say "final graceful shutdown: HTTP $($resp.StatusCode), exited=$exited, exit code $(if ($exited) { $server.ExitCode } else { 'n/a' })"
  if (-not $exited -or $server.ExitCode -ne 0) { throw 'Final graceful shutdown did not exit 0' }
  $server = $null
  # ---- timeline of live leases (database view) around the fault
  $mon = Get-Content $state -Raw | ConvertFrom-Json
  $rows = $mon.timeline | ForEach-Object { $f = $_ -split ','; [pscustomobject]@{ At = [long]$mon.startedMs + [long]$f[0]; Ok = $f[1] -eq '1'; Active = [int]$f[2] } }
  $atFault = ($rows | Where-Object { $_.At -le $faultMs -and $_.Ok } | Select-Object -Last 1).Active
  $after = @($rows | Where-Object { $_.At -gt $faultMs -and $_.Ok })
  $dip = $after | Where-Object { $_.Active -lt 20 } | Select-Object -First 1
  $troughRow = $after | Sort-Object Active, At | Select-Object -First 1
  $full = if ($troughRow) { $after | Where-Object { $_.At -gt $troughRow.At -and $_.Active -ge 20 } | Select-Object -First 1 } else { $null }
  Say ("live leases at fault: $atFault; first sample below 20 (first lease expiry or release): " + $(if ($dip) { '+{0:N1} s' -f (($dip.At - $faultMs) / 1000) } else { 'never' }) +
    '; lowest live count: ' + $(if ($troughRow) { '{0} at +{1:N1} s' -f $troughRow.Active, (($troughRow.At - $faultMs) / 1000) } else { 'n/a' }) +
    '; all 20 live again: ' + $(if ($full) { '+{0:N1} s' -f (($full.At - $faultMs) / 1000) } else { 'never' }))
  $bad = $rows | Where-Object { -not $_.Ok }
  if ($bad) { Say ("database unreachable for monitor samples: {0}, from +{1:N1} s to +{2:N1} s after the fault" -f @($bad).Count, ((@($bad)[0].At - $faultMs) / 1000), ((@($bad)[-1].At - $faultMs) / 1000)) }
  $series = ($rows | Where-Object { $_.At -ge $faultMs - 2000 } | ForEach-Object { '{0:N0}:{1}' -f (($_.At - $faultMs) / 1000), $_.Active }) -join ' '
  Add-Content -LiteralPath $log -Value "lease series (seconds after fault : live leases, -1 = database unreachable): $series"
  # ---- the soak's own invariants: only outage-class errors are allowed; anything else is a defect
  $sr = Get-Content $report -Raw | ConvertFrom-Json
  $allowed = 'adapter_stopped_after_connect', 'adapter_stopped_before_connect', 'connection_or_reconnect_timeout', 'gameplay_result_unavailable', 'snapshot_stalled'
  $seen = @($sr.players | ForEach-Object { $_.errors } | Sort-Object -Unique)
  $unexpected = @($seen | Where-Object { $allowed -notcontains $_ })
  $totalFail = ($sr.players | Measure-Object -Property failures -Sum).Sum
  Say ("soak: finished=$($sr.finished); adapter failures $totalFail (error kinds: $($seen -join ', ')); reconnects " + (($sr.players | Measure-Object -Property reconnects -Sum).Sum))
  if ($unexpected.Count -gt 0) { throw "Unexpected soak error kinds (not outage-class): $($unexpected -join ', ')" }
  if (-not $full) { throw 'Not all 20 leases were live again after the fault' }
  # Bound: a lease is 30 s from its last renewal (PERSISTENCE.md), so every lease is free by fault + 30 s; the server
  # restart (outage + about 2 s), twenty paced opens (0.5 s each) and the soak's 3 s retry add at most about 25 s.
  if ($Mode -ne 'DatabaseCrash' -and ((($full.At - $faultMs) / 1000) -gt ($OutageSeconds + 60))) { throw 'Adapters rejoined later than the documented lease bound plus restart and pacing' }
  # ---- judge against the committed database
  Java-Tool @('verify', $profiles, $state, (Join-Path $Work 'verify.txt'), 'expect-letters') | ForEach-Object { Say "  [final] $_" }
  }
  $ok = $true
} catch { Say "FAILURE: $($_.Exception.Message)"; throw } finally {
  Remove-Item Env:\T4C_SOAK_STOP_FILE -ErrorAction SilentlyContinue
  foreach ($proc in @($soak, $monitor, $server)) { if ($proc -and -not $proc.HasExited) { Stop-Process -Id $proc.Id -Force } }
  Remove-Item (Join-Path $Work 'control-*.token') -ErrorAction SilentlyContinue
  if ($ok) { Remove-Item (Join-Path $profiles '*.json') -ErrorAction SilentlyContinue }
  Say ("END " + $(if ($ok) { 'verifier passed' } else { 'verifier failed or run aborted' }))
}
