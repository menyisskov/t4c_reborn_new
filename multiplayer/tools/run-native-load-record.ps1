<#
.SYNOPSIS
  Full-metric load record (T4C-0352, milestone M5): a twenty-adapter native soak on a disposable cluster, with the
  private operator metrics sampled throughout, and a written summary of every measure the M5 record asks for.

.DESCRIPTION
  Creates ONE new acceptance cluster (random t4c_acc_ id, WSL Ubuntu PostgreSQL 16) on -DbPort, a copied runtime in TEMP,
  and ONE ServerMain --online child on -ServerPort (game socket on -ServerPort + 1). Refuses 55432, 55433, 18765, 18766,
  18770, 18771 and any listening port. Provisions twenty characters beside the opening NPC (NativeFaultDrill provision,
  greet), runs NativeAdapterSoak for -Seconds with T4C_SOAK_CONTROL_FILE set so /admin/metrics is sampled every 5 s,
  samples the server process's Windows working set and CPU every 15 s, stops the server through the private
  /admin/shutdown (must exit 0) and writes load-record.txt with:
    tick duration and deadline debt, process CPU (share of one core) and heap/non-heap/working-set memory,
    game-socket and HTTP application bandwidth (bytes/s and per adapter), database operation latency histogram,
    pool wait and timeouts, occupancy at 20/19+ adapters, command latency, accepted/rejected commands, region
    transfers, and every adapter error kind with its count.
  Everything is read from the soak's JSON report and the sampled files; nothing is typed in by hand. The cluster is
  destroyed after a complete run unless -KeepCluster is given. Password handling is that of acceptance-cluster.ps1 RunEnv.
  Portal walkers are chosen with the soak's own T4C_SOAK_PORTAL_WALKERS / _INTERVAL_SECONDS / _DWELL_SECONDS variables.
#>
param(
  [Parameter(Mandatory)][ValidateRange(1024, 65535)][int]$DbPort,
  [Parameter(Mandatory)][ValidateRange(1024, 65534)][int]$ServerPort,
  [ValidateRange(60, 14400)][int]$Seconds = 1800,
  [switch]$KeepCluster,
  [string]$RunId,          # inner use
  [string]$Work            # inner use
)
$ErrorActionPreference = 'Stop'
$protected = 55432, 55433, 18765, 18766, 18770, 18771
foreach ($p in @($DbPort, $ServerPort, ($ServerPort + 1))) { if ($protected -contains $p) { throw "Port $p is protected" } }
if ($DbPort -eq $ServerPort -or $DbPort -eq ($ServerPort + 1)) { throw 'Database and server ports must differ' }
$root = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$cluster = Join-Path $PSScriptRoot 'acceptance-cluster.ps1'

if (-not $RunId) {
  foreach ($p in @($DbPort, $ServerPort, ($ServerPort + 1))) {
    if (Get-NetTCPConnection -State Listen -LocalPort $p -ErrorAction SilentlyContinue) { throw "Port $p is already listening" }
  }
  $created = (& $cluster -Action Create -Port $DbPort | Out-String)
  if ($created -notmatch 'CREATE_OK id=(t4c_acc_[0-9a-f]{12})') { throw 'Cluster creation did not report an id' }
  $id = $Matches[1]
  $work = Join-Path $env:TEMP ("t4c-0352-load-" + $id)
  Write-Host "cluster $id on $DbPort; evidence $work"
  $passed = $false
  try {
    $previous = $ErrorActionPreference; $ErrorActionPreference = 'Continue'
    try {
      & $cluster -Action RunEnv -RunId $id -Command 'powershell.exe' -CommandArgs @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', $PSCommandPath,
          '-DbPort', $DbPort, '-ServerPort', $ServerPort, '-Seconds', $Seconds, '-RunId', $id, '-Work', $work)
      $code = $LASTEXITCODE
    } finally { $ErrorActionPreference = $previous }
    if ($code -ne 0) { throw "Inner run failed ($code)" }
    $passed = $true
  } finally {
    if ($passed -and -not $KeepCluster) { & $cluster -Action Destroy -RunId $id }
    else { Write-Warning "Cluster $id kept for inspection (destroy later with acceptance-cluster.ps1 -Action Destroy)" }
  }
  return
}

# ---- inner
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
$log = Join-Path $Work 'load-record.txt'
$t0 = Get-Date
function Say([string]$m) { $line = ('[t+{0,6:N1}s] {1}' -f ((Get-Date) - $t0).TotalSeconds, $m); Write-Host $line; Add-Content -LiteralPath $log -Value $line }
$endpoint = "http://127.0.0.1:$ServerPort"
function Run-Java([string[]]$a, [string]$logName) {
  $previous = $ErrorActionPreference; $ErrorActionPreference = 'Continue'
  try { & java '-Djava.awt.headless=true' @a 2>&1 | ForEach-Object { "$_" } | Tee-Object -FilePath (Join-Path $Work $logName) | Out-Null; $code = $LASTEXITCODE }
  finally { $ErrorActionPreference = $previous }
  return $code
}
function Drill-Tool([string[]]$a) { $code = Run-Java (@('-cp', $probeCp, 'com.perso.T4C.server.NativeFaultDrill') + $a) "drill-$($a[0]).log"; if ($code -ne 0) { throw "NativeFaultDrill $($a[0]) failed ($code)" } }
function Test-Health { try { $r = Invoke-WebRequest -Uri "$endpoint/health" -UseBasicParsing -TimeoutSec 2; return ($r.StatusCode -eq 200 -and $r.Content -match 'local-persistent') } catch { return $false } }

$server = $null; $soak = $null; $ok = $false
$profiles = Join-Path $Work 'profiles'
try {
  & javac --release 21 -proc:none -cp $cp -d (Join-Path $Work 'probe') (Join-Path $PSScriptRoot 'NativeFaultDrill.java')
  if ($LASTEXITCODE -ne 0) { throw 'javac failed' }
  $controlFile = Join-Path $Work 'control.token'
  $env:T4C_CONTROL_FILE = $controlFile; $env:T4C_BIND_ADDRESS = '127.0.0.1'
  try {
    $server = Start-Process -FilePath 'java' -ArgumentList @('-Djava.awt.headless=true', '-jar', $jar, '--online', $map, $ServerPort) -WorkingDirectory $rt `
      -RedirectStandardOutput (Join-Path $Work 'server.out.log') -RedirectStandardError (Join-Path $Work 'server.err.log') -PassThru -WindowStyle Hidden | ForEach-Object { $null = $_.Handle; $_ }
  } finally { Remove-Item Env:\T4C_CONTROL_FILE, Env:\T4C_BIND_ADDRESS -ErrorAction SilentlyContinue }
  $until = (Get-Date).AddSeconds(60)
  while (-not (Test-Health)) { if ($server.HasExited) { throw 'Server exited early' }; if ((Get-Date) -gt $until) { throw 'Server not healthy' }; Start-Sleep -Milliseconds 300 }
  Say "server healthy (pid $($server.Id)); mode: $Seconds s soak, walkers=$($env:T4C_SOAK_PORTAL_WALKERS)"
  Drill-Tool @('provision', $profiles, $endpoint); Drill-Tool @('greet', $profiles, $endpoint); Say 'twenty characters provisioned and greeted'
  $report = Join-Path $Work 'soak-report.json'
  $env:T4C_SOAK_CONTROL_FILE = $controlFile
  try {
    $soak = Start-Process -FilePath 'java' -ArgumentList @('-Djava.awt.headless=true', '-cp', $cp, (Join-Path $PSScriptRoot 'NativeAdapterSoak.java'), $endpoint, $profiles, $Seconds, $map, $report, $server.Id) `
      -RedirectStandardOutput (Join-Path $Work 'soak.out.log') -RedirectStandardError (Join-Path $Work 'soak.err.log') -PassThru -WindowStyle Hidden | ForEach-Object { $null = $_.Handle; $_ }
  } finally { Remove-Item Env:\T4C_SOAK_CONTROL_FILE -ErrorAction SilentlyContinue }
  Say "soak started (pid $($soak.Id))"
  # Windows view of the server process every 15 s (resident memory is not visible to the JVM's own heap counters).
  $samples = New-Object System.Collections.Generic.List[object]
  while (-not $soak.HasExited) {
    try { $server.Refresh(); $samples.Add([pscustomobject]@{ Seconds = [int]((Get-Date) - $t0).TotalSeconds; WorkingSetMB = [math]::Round($server.WorkingSet64 / 1MB, 1); PrivateMB = [math]::Round($server.PrivateMemorySize64 / 1MB, 1); CpuSeconds = [math]::Round($server.TotalProcessorTime.TotalSeconds, 2); Threads = $server.Threads.Count; Handles = $server.HandleCount }) } catch { }
    Start-Sleep -Seconds 15
  }
  $samples | Export-Csv -NoTypeInformation -Path (Join-Path $Work 'process-samples.csv')
  Say "soak exited with code $($soak.ExitCode)"
  $soakCode = $soak.ExitCode; $soak = $null
  # graceful stop: must exit 0
  $token = (Get-Content $controlFile -Raw).Trim()
  $resp = Invoke-WebRequest -Uri "$endpoint/admin/shutdown" -Method Post -Headers @{ Origin = $endpoint; Authorization = "Bearer $token" } -ContentType 'text/plain' -Body '' -UseBasicParsing -TimeoutSec 5
  $exited = $server.WaitForExit(100000)
  Say "graceful shutdown: HTTP $($resp.StatusCode), exited=$exited, exit code $(if ($exited) { $server.ExitCode } else { 'n/a' })"
  if (-not $exited -or $server.ExitCode -ne 0) { throw 'Graceful shutdown did not exit 0' }
  $errBytes = (Get-Item (Join-Path $Work 'server.err.log')).Length
  Say "server stderr bytes: $errBytes"
  $server = $null

  # ---- the record
  $r = Get-Content $report -Raw | ConvertFrom-Json
  & (Join-Path $PSScriptRoot 'summarize-load-record.ps1') -Report $report -ProcessSamples (Join-Path $Work 'process-samples.csv') -ServerStderrBytes $errBytes | ForEach-Object { Say $_ }
  if ($soakCode -ne 0) { Say "NOTE: the soak tool exited with code $soakCode (its own criteria); see soak-report.json" }
  $ok = $true
} catch { Say "FAILURE: $($_.Exception.Message)"; throw } finally {
  Remove-Item Env:\T4C_SOAK_CONTROL_FILE -ErrorAction SilentlyContinue
  foreach ($proc in @($soak, $server)) { if ($proc -and -not $proc.HasExited) { Stop-Process -Id $proc.Id -Force } }
  Remove-Item $controlFile -ErrorAction SilentlyContinue
  if ($ok) { Remove-Item (Join-Path $profiles '*.json') -ErrorAction SilentlyContinue }
  Say ("END " + $(if ($ok) { 'load record complete' } else { 'load record failed or run aborted' }))
}
