<#
.SYNOPSIS
  Operator for the rendered two-client M5 mixed session with injected faults (T4C-0363).

.DESCRIPTION
  Creates ONE disposable acceptance cluster (acceptance-cluster.ps1), starts the SERVER from an extracted server package, provisions two
  level-30 warriors beside Warden Aelric, starts two real rendered clients (OnlineLiveRunDriver, scene m5) from the CLIENT package's
  jars, and performs every fault the clients ask for through marker files: a graceful server restart, a forced server kill, a database
  crash (immediate PostgreSQL stop), a hard kill right after a pickup claim and a hard kill right after a corpse recovery request.
  Server, provisioner and clients start from a scrubbed process environment (rendered-session-launch.ps1). It samples process memory and
  CPU every minute, saves the committed database rows around each fault (LiveRunProvision accept-report) and writes summary.txt.

  Refuses the protected ports 18765, 18766, 18770, 18771, 55432, 55433 and any port that is already listening. It signals only
  processes it started (and the cluster through the ownership-verified helper). Passwords are never read here; the database values
  reach the children through acceptance-cluster.ps1 -Action RunEnv. Profiles (account keys) are deleted at the end.

.EXAMPLE
  ./run-rendered-mixed-session.ps1 -ClientPackage 'C:\x\client pkg' -ServerPackage 'C:\x\server pkg' -DriverClasses C:\x\driver-classes `
    -ProvisionClasses C:\x\prov-classes -DbPort 57311 -ServerPort 18861 -OutDir C:\x\run1
#>
param(
  [Parameter(Mandatory = $true)][string]$ClientPackage,
  [Parameter(Mandatory = $true)][string]$ServerPackage,
  [Parameter(Mandatory = $true)][string]$DriverClasses,
  [Parameter(Mandatory = $true)][string]$ProvisionClasses,
  [Parameter(Mandatory = $true)][int]$DbPort,
  [Parameter(Mandatory = $true)][int]$ServerPort,
  [Parameter(Mandatory = $true)][string]$OutDir,
  [int]$KillRace = 1,
  [int]$WindowXA = 10,
  [int]$WindowXB = 1300,
  [string]$DriverProps = '',
  [switch]$DeathOnly,
  [switch]$KillRecover,
  [switch]$KeepCluster
)
$ErrorActionPreference = 'Stop'
$here = $PSScriptRoot
$cluster = Join-Path $here 'acceptance-cluster.ps1'
$launcher = Join-Path $here 'rendered-session-launch.ps1'
$psExe = Join-Path $env:SystemRoot 'System32\WindowsPowerShell\v1.0\powershell.exe'
$javaExe = (Get-Command java -ErrorAction Stop).Source
$protected = 18765, 18766, 18770, 18771, 55432, 55433
foreach ($p in @($DbPort, $ServerPort, ($ServerPort + 1))) {
  if ($protected -contains $p) { throw "Port $p is protected" }
  if (Get-NetTCPConnection -State Listen -LocalPort $p -ErrorAction SilentlyContinue) { throw "Port $p is already listening" }
}
if (Test-Path -LiteralPath $OutDir) { throw 'OutDir must not exist' }
New-Item -ItemType Directory -Path $OutDir | Out-Null
$OutDir = (Resolve-Path -LiteralPath $OutDir).Path
$sync = Join-Path $OutDir 'sync'; $shots = Join-Path $OutDir 'shots'; $profiles = Join-Path $OutDir 'private-profiles'
$scratch = Join-Path $env:TEMP ('t4c-m5-scratch-' + [guid]::NewGuid().ToString('N'))
foreach ($d in $sync, $shots, $profiles, $scratch) { New-Item -ItemType Directory -Force -Path $d | Out-Null }
$events = Join-Path $OutDir 'events.log'
$script:specN = 0
$script:id = $null
$script:serverPid = 0
$script:serverN = 0
$script:clients = @{}
$script:failed = $true
$script:lastCpu = @{}
$script:lastSample = [DateTime]::MinValue
$script:t0 = Get-Date
$samples = Join-Path $OutDir 'samples.csv'
'time_s,process,pid,workingset_mb,cpu_percent_of_one_core' | Out-File -Encoding ascii $samples

function Log([string]$text) {
  $line = ('{0:HH:mm:ss.fff} +{1,6:N1}s {2}' -f (Get-Date), ((Get-Date) - $script:t0).TotalSeconds, $text)
  Write-Host $line
  Add-Content -LiteralPath $events -Value $line
}

function Invoke-Launch($spec, [bool]$db) {
  $script:specN++
  $path = Join-Path $OutDir ('spec-{0:00}.json' -f $script:specN)
  ($spec | ConvertTo-Json -Depth 4) | Out-File -Encoding ascii $path
  # A started server/client keeps inherited handles open for its whole life, so never read this child's output through a pipe
  # (a pipeline would wait for EOF forever): start it with Process.Start and wait for the process itself.
  $psi = New-Object System.Diagnostics.ProcessStartInfo
  $psi.FileName = $psExe
  $psi.UseShellExecute = $false
  $inner = "@('-NoProfile','-ExecutionPolicy','Bypass','-File','$launcher','-SpecFile','$path')"
  if ($db) {
    $psi.Arguments = "-NoProfile -ExecutionPolicy Bypass -Command `"& '$cluster' -Action RunEnv -RunId $($script:id) -Command '$psExe' -CommandArgs $inner`""
  } else {
    $psi.Arguments = "-NoProfile -ExecutionPolicy Bypass -File `"$launcher`" -SpecFile `"$path`""
  }
  $proc = [System.Diagnostics.Process]::Start($psi)
  $proc.WaitForExit()
  if ($proc.ExitCode -ne 0) { throw "launcher wrapper exited $($proc.ExitCode)" }
  $result = (Get-Content -LiteralPath ($path + '.result') -Raw).Trim()
  Remove-Item -LiteralPath $path -ErrorAction SilentlyContinue
  return $result
}

function New-Spec([string]$file, [string]$argLine, [string]$cwd, [string]$name, [bool]$passDb, [bool]$wait, [string[]]$env) {
  return [ordered]@{
    filePath = $file; argumentLine = $argLine; workingDirectory = $cwd
    stdoutFile = (Join-Path $OutDir ($name + '.out.log')); stderrFile = (Join-Path $OutDir ($name + '.err.log'))
    scratchRoot = $scratch; javaExe = $javaExe; passDb = $passDb; wait = $wait; extraEnv = $env
  }
}

function Get-Health {
  try { return (Invoke-WebRequest -UseBasicParsing -TimeoutSec 3 -Uri ("http://127.0.0.1:$ServerPort/health")).StatusCode } catch { return 0 }
}

function Start-Realm {
  # A hard-killed server's listener can take a while to disappear; start only when the HTTP port is free.
  $freeBy = (Get-Date).AddSeconds(90)
  while (Get-NetTCPConnection -State Listen -LocalPort $ServerPort -ErrorAction SilentlyContinue) {
    if ((Get-Date) -gt $freeBy) { throw "port $ServerPort is still occupied 90 s after the stop" }
    Start-Sleep -Milliseconds 500
  }
  $script:serverN++
  $state = Join-Path $OutDir ('server-state-' + $script:serverN)
  $argLine = '-NoProfile -ExecutionPolicy Bypass -File "' + (Join-Path $ServerPackage 'start-server.ps1') + '" -Port ' + $ServerPort + ' -StateDirectory "' + $state + '"'
  $spec = New-Spec $psExe $argLine $OutDir ('server-start-' + $script:serverN) $true $true @()
  $exit = Invoke-Launch $spec $true
  if ($exit -ne '0') { throw "start-server.ps1 exited $exit" }
  $session = Get-Content -LiteralPath (Join-Path $state 'session.json') -Raw | ConvertFrom-Json
  $script:serverPid = [int]$session.processId
  $deadline = (Get-Date).AddSeconds(60)
  while ((Get-Health) -ne 200) { if ((Get-Date) -gt $deadline) { throw 'server never became healthy' }; Start-Sleep -Milliseconds 300 }
  Log ("server #{0} up, pid {1}, port {2}, state {3}" -f $script:serverN, $script:serverPid, $ServerPort, $state)
}

function Stop-RealmGraceful {
  $state = Join-Path $OutDir ('server-state-' + $script:serverN)
  $argLine = '-NoProfile -ExecutionPolicy Bypass -File "' + (Join-Path $ServerPackage 'stop-server.ps1') + '" -StateDirectory "' + $state + '"'
  $spec = New-Spec $psExe $argLine $OutDir ('server-stop-' + $script:serverN) $false $true @()
  $exit = Invoke-Launch $spec $false
  Log ("graceful stop of server #{0}: stop-server.ps1 exit {1}" -f $script:serverN, $exit)
  return $exit
}

function Stop-RealmKill {
  $sw = [Diagnostics.Stopwatch]::StartNew()
  Stop-Process -Id $script:serverPid -Force
  Log ("server #{0} pid {1} hard-killed (Stop-Process -Force took {2} ms)" -f $script:serverN, $script:serverPid, $sw.ElapsedMilliseconds)
}

function Save-Report([string]$name) {
  $colbin = Join-Path $ServerPackage 'assets\maps\worldmap\worldmap.colbin'
  $cp = '"' + $ProvisionClasses + ';' + (Join-Path $ServerPackage 't4c-server.jar') + ';' + (Join-Path $ServerPackage 'lib\*') + '"'
  $spec = New-Spec $javaExe ('-cp ' + $cp + ' com.perso.T4C.server.LiveRunProvision "' + $colbin + '" accept-report "' + $OutDir + '"') $OutDir ('db-' + $name) $true $true @()
  $exit = Invoke-Launch $spec $true
  Log ("database report '{0}' saved (exit {1})" -f $name, $exit)
}

function Get-ChildJava([int]$parent) {
  $c = Get-CimInstance Win32_Process -Filter "ParentProcessId=$parent" -ErrorAction SilentlyContinue | Where-Object { $_.Name -eq 'java.exe' } | Select-Object -First 1
  if ($c) { return [int]$c.ProcessId } else { return 0 }
}

function Sample-Processes {
  $now = Get-Date
  if (($now - $script:lastSample).TotalSeconds -lt 60) { return }
  $dt = ($now - $script:lastSample).TotalSeconds
  $script:lastSample = $now
  $targets = @{ server = $script:serverPid }
  foreach ($k in $script:clients.Keys) { $targets[$k] = $script:clients[$k].javaPid }
  foreach ($name in $targets.Keys) {
    $procId = $targets[$name]
    if (-not $procId) { continue }
    $p = Get-Process -Id $procId -ErrorAction SilentlyContinue
    if (-not $p) { continue }
    $cpu = $p.TotalProcessorTime.TotalSeconds
    $pct = ''
    if ($script:lastCpu.ContainsKey($name) -and $script:lastCpu[$name].pid -eq $procId -and $dt -lt 3600 -and $dt -gt 1) {
      $pct = [math]::Round(100 * ($cpu - $script:lastCpu[$name].cpu) / $dt, 1)
    }
    $script:lastCpu[$name] = @{ pid = $procId; cpu = $cpu }
    ('{0},{1},{2},{3},{4}' -f [int](($now - $script:t0).TotalSeconds), $name, $procId, [int]($p.WorkingSet64 / 1MB), $pct) | Add-Content -Encoding ascii $samples
  }
}

function Wait-Files([string[]]$names, [int]$timeoutSec, [string]$what, [bool]$fatal = $true, [int]$pollMs = 100) {
  $deadline = (Get-Date).AddSeconds($timeoutSec)
  while ($true) {
    $missing = @($names | Where-Object { -not (Test-Path -LiteralPath (Join-Path $sync $_)) })
    if ($missing.Count -eq 0) { return $true }
    Sample-Processes
    if ((Get-Date) -gt $deadline) {
      Log ("TIMEOUT waiting for {0} (missing {1})" -f $what, ($missing -join ', '))
      if ($fatal) { throw "timeout: $what" }
      return $false
    }
    $alive = @($script:clients.Keys | Where-Object { Get-Process -Id $script:clients[$_].javaPid -ErrorAction SilentlyContinue })
    if ($alive.Count -eq 0) { Log 'both clients have exited'; if ($fatal) { throw "clients exited while waiting for $what" }; return $false }
    Start-Sleep -Milliseconds $pollMs
  }
}

try {
  $out = & $cluster -Action Create -Port $DbPort | Out-String
  if ($out -notmatch 'CREATE_OK id=(t4c_acc_[0-9a-f]{12})') { throw 'cluster create failed' }
  $script:id = $Matches[1]
  Log ("cluster {0} on loopback {1}" -f $script:id, $DbPort)

  Start-Realm
  # Provision the two warriors (level 30, beside Warden Aelric) with the provisioner compiled against the SERVER package jar.
  $colbin = Join-Path $ServerPackage 'assets\maps\worldmap\worldmap.colbin'
  $cp = '"' + $ProvisionClasses + ';' + (Join-Path $ServerPackage 't4c-server.jar') + ';' + (Join-Path $ServerPackage 'lib\*') + '"'
  $spec = New-Spec $javaExe ('-Dbeta.nofiller=true -cp ' + $cp + ' com.perso.T4C.server.LiveRunProvision "' + $colbin + '" beta "' + $profiles + '"') $OutDir 'provision' $true $true @()
  $exit = Invoke-Launch $spec $true
  $provOut = Get-Content -LiteralPath (Join-Path $OutDir 'provision.out.log') -Raw
  if ($exit -ne '0' -or $provOut -notmatch 'PROVISION_OK') { throw 'provisioning failed' }
  if ($provOut -notmatch 'skeletonGround=(\d+),(\d+)') { throw 'no skeletonGround in the provisioner output' }
  $gx = $Matches[1]; $gy = $Matches[2]
  Log ("provisioned two characters; skeleton ground {0},{1}" -f $gx, $gy)
  Save-Report 'initial'

  # The two rendered clients, from the CLIENT package's jars.
  $clientCp = '"' + (Join-Path $ClientPackage 'app\T4C.jar') + ';' + (Join-Path $ClientPackage 'lib\*') + ';' + $DriverClasses + '"'
  $props = '-Dm5.killRace=' + $KillRace + ' ' + $(if ($KillRecover) { '-Dm5.killRecover=true ' } else { '' }) + $(if ($DriverProps) { $DriverProps + ' ' } else { '' })
  foreach ($who in @(@('m5A', 'player-a.json', $WindowXA), @('m5B', 'player-b.json', $WindowXB))) {
    $argLine = $props + '-cp ' + $clientCp + ' com.perso.T4C.screens.OnlineLiveRunDriver http://127.0.0.1:' + $ServerPort + ' "' + (Join-Path $profiles $who[1]) + '" "' + $shots + '" ' + $who[0] + ' m5 "' + $sync + '" ' + $who[2] + ' 20 ' + $gx + ' ' + $gy
    $spec = New-Spec $javaExe $argLine $ClientPackage ('client-' + $who[0]) $false $false @()
    $wrapper = [int](Invoke-Launch $spec $false)
    Start-Sleep -Seconds 4
    $jp = Get-ChildJava $wrapper
    $script:clients[$who[0]] = @{ wrapper = $wrapper; javaPid = $jp }
    Log ("client {0} started (wrapper {1}, java {2})" -f $who[0], $wrapper, $jp)
  }

  if (-not $DeathOnly) {
  # Cycle g: graceful restart. The clients capture their committed state, signal, and wait for the disconnect.
  Wait-Files @('m5A.stop-ready-g', 'm5B.stop-ready-g') 3000 'both windows ready for the graceful restart' | Out-Null
  Save-Report 'before-graceful'
  Log 'FAULT g: graceful stop through stop-server.ps1'
  Stop-RealmGraceful | Out-Null
  Wait-Files @('m5A.down-g', 'm5B.down-g') 120 'both windows show disconnected (g)' $false | Out-Null
  Start-Sleep -Seconds 3
  Start-Realm
  New-Item -ItemType File -Path (Join-Path $sync 'server-restarted-g') | Out-Null
  Log 'FAULT g: restarted, marker written'

  # Cycle k: forced kill.
  Wait-Files @('m5A.stop-ready-k', 'm5B.stop-ready-k') 900 'both windows ready for the forced kill' | Out-Null
  Save-Report 'before-kill'
  Log 'FAULT k: Stop-Process -Force on the server'
  Stop-RealmKill
  Save-Report 'right-after-kill'
  Wait-Files @('m5A.down-k', 'm5B.down-k') 120 'both windows show disconnected (k)' $false | Out-Null
  Start-Sleep -Seconds 12
  Start-Realm
  New-Item -ItemType File -Path (Join-Path $sync 'server-restarted-k') | Out-Null
  Log 'FAULT k: restarted, marker written'

  # Cycle d: database crash (immediate stop of the cluster's own postmaster); the server keeps running.
  Wait-Files @('m5A.stop-ready-d', 'm5B.stop-ready-d') 900 'both windows ready for the database crash' | Out-Null
  Save-Report 'before-dbcrash'
  Log 'FAULT d: acceptance-cluster Crash (immediate stop)'
  & $cluster -Action Crash -RunId $script:id | Out-Null
  Wait-Files @('m5A.down-d', 'm5B.down-d') 170 'both windows show disconnected (d)' $false | Out-Null
  Log ("server health during the outage: {0}; server process alive: {1}" -f (Get-Health), [bool](Get-Process -Id $script:serverPid -ErrorAction SilentlyContinue))
  Start-Sleep -Seconds 5
  & $cluster -Action Start -RunId $script:id | Out-Null
  Log 'FAULT d: database started again (crash recovery)'
  Start-Sleep -Seconds 3
  Log ("server health after the database is back: {0}" -f (Get-Health))
  Save-Report 'after-dbcrash'
  New-Item -ItemType File -Path (Join-Path $sync 'server-restarted-d') | Out-Null

  # Cycle pk: hard kill right after the pickup claim click (the driver writes kill-now-pk).
  Wait-Files @('kill-now-pk') 2400 'the pickup-claim kill marker' $true 5 | Out-Null
  Stop-RealmKill
  Log ("FAULT pk: killed after the claim click; marker content: {0}" -f ((Get-Content (Join-Path $sync 'kill-now-pk') -Raw).Trim()))
  Save-Report 'right-after-pk-kill'
  Wait-Files @('m5A.stop-ready-pk', 'm5B.stop-ready-pk') 300 'both windows ready (pk)' | Out-Null
  Wait-Files @('m5A.down-pk', 'm5B.down-pk') 120 'both windows show disconnected (pk)' $false | Out-Null
  Start-Sleep -Seconds 12
  Start-Realm
  New-Item -ItemType File -Path (Join-Path $sync 'server-restarted-pk') | Out-Null
  Log 'FAULT pk: restarted, marker written'

  }
  if ($KillRecover) {
    Wait-Files @('kill-now-rk') 2400 'the corpse-recovery kill marker' $true 5 | Out-Null
    Stop-RealmKill
    Log ("FAULT rk: killed after the recovery request; marker content: {0}" -f ((Get-Content (Join-Path $sync 'kill-now-rk') -Raw).Trim()))
    Save-Report 'right-after-rk-kill'
    Wait-Files @('m5A.stop-ready-rk', 'm5B.stop-ready-rk') 300 'both windows ready (rk)' | Out-Null
    Wait-Files @('m5A.down-rk', 'm5B.down-rk') 120 'both windows show disconnected (rk)' $false | Out-Null
    Start-Sleep -Seconds 12
    Start-Realm
    New-Item -ItemType File -Path (Join-Path $sync 'server-restarted-rk') | Out-Null
    Log 'FAULT rk: restarted, marker written'
  }

  Wait-Files @('m5A.done', 'm5B.done') 1800 'both scripts finished' | Out-Null
  Log 'both client scripts finished'
  Save-Report 'final'
  $exit = Stop-RealmGraceful
  $script:failed = ($exit -ne '0')
  # Let the clients leave (the scripts exit the application themselves).
  $deadline = (Get-Date).AddSeconds(90)
  while ((Get-Date) -lt $deadline -and @($script:clients.Keys | Where-Object { Get-Process -Id $script:clients[$_].javaPid -ErrorAction SilentlyContinue }).Count -gt 0) { Start-Sleep -Seconds 1 }
} catch {
  Log ("ABORT: {0}" -f $_.Exception.Message)
  throw
} finally {
  foreach ($k in @($script:clients.Keys)) {
    $jp = $script:clients[$k].javaPid
    if ($jp -and (Get-Process -Id $jp -ErrorAction SilentlyContinue)) { Log "stopping leftover client $k ($jp)"; Stop-Process -Id $jp -Force }
  }
  if ($script:serverPid -and (Get-Process -Id $script:serverPid -ErrorAction SilentlyContinue) -and $script:failed) {
    Log 'server still running after a failed run: asking for a graceful stop'
    try { Stop-RealmGraceful | Out-Null } catch { Log 'graceful stop failed; killing the server this script started'; Stop-Process -Id $script:serverPid -Force }
  }
  Remove-Item -LiteralPath $profiles -Recurse -Force -ErrorAction SilentlyContinue
  Remove-Item -LiteralPath $scratch -Recurse -Force -ErrorAction SilentlyContinue
  if ($script:id) {
    if ($KeepCluster -or $script:failed) { Log ("cluster {0} kept (destroy with acceptance-cluster.ps1 -Action Destroy)" -f $script:id) }
    else { & $cluster -Action Destroy -RunId $script:id | ForEach-Object { Log "cluster: $_" } }
  }
}
