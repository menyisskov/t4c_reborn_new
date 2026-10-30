<#
.SYNOPSIS
  Restored-database gameplay and reconnect check (T4C-0345, milestone M5): populate a disposable PostgreSQL database
  through real native play, back it up with verify_backup.py into a SEPARATE database of the same disposable cluster,
  then start a server on the restore and make the same twenty native adapters play and reconnect there.

.DESCRIPTION
  Everything is disposable and owned by this run: a new acceptance cluster (random t4c_acc_ id, WSL Ubuntu PostgreSQL 16)
  on -DbPort, a copied runtime in TEMP, and ServerMain --online children on -ServerPort. Refuses 55432, 55433, 18765,
  18766, 18770, 18771 and any listening port. The password is handled only by acceptance-cluster.ps1 RunEnv and
  restore-check-backup.sh (process-scoped environment and a mode-0600 PGPASSFILE; never printed, never in an argument).

  Phases (all must pass or the script throws):
   1. Loot/corpse population: NativeLootFaultDrill (real claims, corpse recoveries, hard kills, lease expiry) on the source.
   2. Twenty characters provisioned beside the opening NPC, greeted (letter, quest flag, receipts), then a paced
      NativeAdapterSoak (combat, optional portal walkers). The server is HARD-KILLED 15 s before the soak ends, so the
      dump holds live copied leases.
   3. Source summary (NativeRestoreCheck summary) and a committed-state verification of the source.
   4. verify_backup.py (restore-check-backup.sh): one exported snapshot, pg_dump, restore into a NEW t4c_restore_ database,
      per-table definition/count/hash comparison.
   5. The restore is summarised and must equal the source summary; the committed-state verifier must pass on it.
   6. A server is started on the RESTORE only. The same adapters must be refused/fenced until the copied leases expire,
      then all twenty play (soak), the server is stopped gracefully (exit 0), restarted on the restore (intentional
      reconnect), a second soak runs, and the final verification (exactly one opening letter per character, no lowered revision)
      judges the restore against everything the source sampler once saw committed and everything sampled on the restore.
  The source database is never modified after phase 3 and the restore is never dropped by this script. The cluster is
  destroyed at the end only after a complete run, unless -KeepCluster is given. Evidence goes to a new TEMP directory.
#>
param(
  [Parameter(Mandatory)][ValidateRange(1024, 65535)][int]$DbPort,
  [Parameter(Mandatory)][ValidateRange(1024, 65535)][int]$ServerPort,
  [ValidateRange(60, 1800)][int]$SoakSeconds = 120,
  [ValidateRange(45, 1800)][int]$RestoredSoakSeconds = 90,
  [ValidateRange(30, 600)][int]$ReconnectSoakSeconds = 45,
  [switch]$KeepCluster,
  [string]$RunId,          # inner use
  [string]$Work            # inner use
)
$ErrorActionPreference = 'Stop'
$protected = 55432, 55433, 18765, 18766, 18770, 18771
foreach ($p in @($DbPort, $ServerPort)) { if ($protected -contains $p) { throw "Port $p is protected" } }
if ($DbPort -eq $ServerPort) { throw 'Database and server ports must differ' }
$root = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$cluster = Join-Path $PSScriptRoot 'acceptance-cluster.ps1'

if (-not $RunId) {
  foreach ($p in @($DbPort, $ServerPort)) {
    if (Get-NetTCPConnection -State Listen -LocalPort $p -ErrorAction SilentlyContinue) { throw "Port $p is already listening" }
  }
  $created = (& $cluster -Action Create -Port $DbPort | Out-String)
  if ($created -notmatch 'CREATE_OK id=(t4c_acc_[0-9a-f]{12})') { throw 'Cluster creation did not report an id' }
  $id = $Matches[1]
  $work = Join-Path $env:TEMP ("t4c-0345-restore-" + $id)
  Write-Host "cluster $id on $DbPort; evidence $work"
  $passed = $false
  try {
    & $cluster -Action RunEnv -RunId $id -Command 'powershell.exe' -CommandArgs @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', $PSCommandPath,
        '-DbPort', $DbPort, '-ServerPort', $ServerPort, '-SoakSeconds', $SoakSeconds, '-RestoredSoakSeconds', $RestoredSoakSeconds,
        '-ReconnectSoakSeconds', $ReconnectSoakSeconds, '-RunId', $id, '-Work', $work)
    if ($LASTEXITCODE -ne 0) { throw "Inner run failed ($LASTEXITCODE)" }
    $passed = $true
  } finally {
    if ($passed -and -not $KeepCluster) { & $cluster -Action Destroy -RunId $id }
    else { Write-Warning "Cluster $id kept for inspection (destroy later with acceptance-cluster.ps1 -Action Destroy)" }
  }
  return
}

# ---- inner: T4C_DB_* are set for this process only (they name the SOURCE database).
foreach ($n in 'T4C_DB_URL', 'T4C_DB_USER', 'T4C_DB_PASSWORD') { if (-not [Environment]::GetEnvironmentVariable($n)) { throw 'Private database environment missing' } }
$sourceUrl = $env:T4C_DB_URL
$restoreName = 't4c_restore_' + (-join ((1..12) | ForEach-Object { '{0:x}' -f (Get-Random -Maximum 16) }))
$restoreUrl = $sourceUrl -replace '/t4c_acc_[0-9a-f]{12}', "/$restoreName"
if ($restoreUrl -eq $sourceUrl) { throw 'Could not derive the restore database URL' }
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
$log = Join-Path $Work 'restore-check.log'
$t0 = Get-Date
function Say([string]$m) { $line = ('[t+{0,6:N1}s] {1}' -f ((Get-Date) - $t0).TotalSeconds, $m); Write-Host $line; Add-Content -LiteralPath $log -Value $line }
$endpoint = "http://127.0.0.1:$ServerPort"
$javaExe = (Get-Command java).Source

function Start-Server([string]$name) {
  $env:T4C_CONTROL_FILE = (Join-Path $Work "control-$name.token"); $env:T4C_BIND_ADDRESS = '127.0.0.1'
  try {
    return Start-Process -FilePath 'java' -ArgumentList @('-Djava.awt.headless=true', '-jar', $jar, '--online', $map, $ServerPort) -WorkingDirectory $rt `
      -RedirectStandardOutput (Join-Path $Work "server-$name.out.log") -RedirectStandardError (Join-Path $Work "server-$name.err.log") -PassThru -WindowStyle Hidden | ForEach-Object { $null = $_.Handle; $_ }
  } finally { Remove-Item Env:\T4C_CONTROL_FILE, Env:\T4C_BIND_ADDRESS -ErrorAction SilentlyContinue }
}
function Test-Health { try { $r = Invoke-WebRequest -Uri "$endpoint/health" -UseBasicParsing -TimeoutSec 2; return ($r.StatusCode -eq 200 -and $r.Content -match 'local-persistent') } catch { return $false } }
function Wait-Health([System.Diagnostics.Process]$proc, [int]$limit = 60) {
  $until = (Get-Date).AddSeconds($limit)
  while ((Get-Date) -lt $until) { if ($proc.HasExited) { throw "Server exited early (code $($proc.ExitCode))" }; if (Test-Health) { return }; Start-Sleep -Milliseconds 300 }
  throw 'Server did not become healthy'
}
function Stop-Graceful([System.Diagnostics.Process]$proc, [string]$name) {
  $token = (Get-Content (Join-Path $Work "control-$name.token") -Raw).Trim()
  $resp = Invoke-WebRequest -Uri "$endpoint/admin/shutdown" -Method Post -Headers @{ Origin = $endpoint; Authorization = "Bearer $token" } -ContentType 'text/plain' -Body '' -UseBasicParsing -TimeoutSec 5
  $exited = $proc.WaitForExit(100000)
  Say "graceful shutdown of server ${name}: HTTP $($resp.StatusCode), exited=$exited, exit code $(if ($exited) { $proc.ExitCode } else { 'n/a' })"
  if (-not $exited -or $proc.ExitCode -ne 0) { throw "Graceful shutdown of server $name did not exit 0" }
}
function Check-Tool([string[]]$a) {
  # Windows PowerShell 5.1 turns a native command's stderr into a terminating error; capture it as text instead.
  $previous = $ErrorActionPreference; $ErrorActionPreference = 'Continue'
  try { $out = & java '-Djava.awt.headless=true' -cp $probeCp com.perso.T4C.server.NativeRestoreCheck @a 2>&1 | ForEach-Object { "$_" }; $code = $LASTEXITCODE }
  finally { $ErrorActionPreference = $previous }
  foreach ($l in @($out)) { if ($l) { Add-Content -LiteralPath $log -Value "    $l" } }
  if ($code -ne 0) { throw "NativeRestoreCheck $($a[0]) failed ($code): $((@($out) | Select-Object -Last 3) -join ' | ')" }
  return $out
}
# A native command's stderr must never reach this script's own stderr: RunEnv runs us as a child of a 'Stop' script that
# would turn the first stderr line (an adapter reporting a killed server) into a terminating error. Merge it into stdout.
function Run-Java([string[]]$a, [string]$logName) {
  $previous = $ErrorActionPreference; $ErrorActionPreference = 'Continue'
  try { & java '-Djava.awt.headless=true' @a 2>&1 | ForEach-Object { "$_" } | Tee-Object -FilePath (Join-Path $Work $logName) | Out-Null; $code = $LASTEXITCODE }
  finally { $ErrorActionPreference = $previous }
  return $code
}
function Drill-Tool([string[]]$a) { $code = Run-Java (@('-cp', $probeCp, 'com.perso.T4C.server.NativeFaultDrill') + $a) "drill-$($a[0]).log"; if ($code -ne 0) { throw "NativeFaultDrill $($a[0]) failed ($code)" } }
function Start-Java([string]$name, [string[]]$a, [string]$controlName = '') {
  if ($controlName) { $env:T4C_SOAK_CONTROL_FILE = (Join-Path $Work "control-$controlName.token") }
  try {
  Start-Process -FilePath 'java' -ArgumentList $a -RedirectStandardOutput (Join-Path $Work "$name.out.log") -RedirectStandardError (Join-Path $Work "$name.err.log") -PassThru -WindowStyle Hidden | ForEach-Object { $null = $_.Handle; $_ }
  } finally { Remove-Item Env:T4C_SOAK_CONTROL_FILE -ErrorAction SilentlyContinue }
}
function Soak-Allowed($report, [string]$what) {
  $sr = Get-Content $report -Raw | ConvertFrom-Json
  $allowed = 'adapter_stopped_after_connect', 'adapter_stopped_before_connect', 'connection_or_reconnect_timeout', 'gameplay_result_unavailable', 'snapshot_stalled'
  $seen = @($sr.players | ForEach-Object { $_.errors } | Sort-Object -Unique)
  $unexpected = @($seen | Where-Object { $allowed -notcontains $_ })
  $fail = ($sr.players | Measure-Object -Property failures -Sum).Sum
  Say ("soak ($what): finished=$($sr.finished); adapter failures $fail (kinds: $($seen -join ', ')); reconnects " + (($sr.players | Measure-Object -Property reconnects -Sum).Sum))
  if ($unexpected.Count -gt 0) { throw "Unexpected soak error kinds in ${what}: $($unexpected -join ', ')" }
  return $sr
}

$server = $null; $soak = $null; $monitor = $null; $ok = $false
$profiles = Join-Path $Work 'profiles'
try {
  & javac --release 21 -proc:none -cp $cp -d (Join-Path $Work 'probe') (Join-Path $PSScriptRoot 'NativeFaultDrill.java') (Join-Path $PSScriptRoot 'NativeRestoreCheck.java') (Join-Path $PSScriptRoot 'NativeLootFaultDrill.java')
  if ($LASTEXITCODE -ne 0) { throw 'javac failed' }
  Say "source $($sourceUrl -replace '\?.*$','' -replace '^.*/','') ; restore target $restoreName ; server port $ServerPort ; db port $DbPort"

  # ---- 1. loot claims and corpse recoveries through real hard-killed servers (the drill owns its own server children)
  $code = Run-Java @('-cp', $probeCp, 'com.perso.T4C.server.NativeLootFaultDrill', $rt, $javaExe, $ServerPort, (Join-Path $Work 'loot-drill')) 'loot-drill-console.log'
  if ($code -ne 0) { throw "NativeLootFaultDrill failed ($code); see loot-drill-console.log" }
  Say 'phase 1: loot claim and corpse recovery drill PASS (see loot-drill/report.txt)'
  if (Test-Health) { throw 'A server still answers on the server port after the loot drill' }

  # ---- 2. twenty characters, opening letters, soak, hard kill
  $server = Start-Server 'src'; Wait-Health $server; Say "phase 2: source server healthy (pid $($server.Id))"
  Drill-Tool @('provision', $profiles, $endpoint) | Out-Null
  Check-Tool @('greet', $profiles, $endpoint) | Out-Null; Say 'twenty characters provisioned and greeted (opening letter, quest flag, receipts)'
  $stateSrc = Join-Path $Work 'monitor-source.json'; $stopMon = Join-Path $Work 'monitor-source.stop'
  $monitor = Start-Java 'monitor-source' @('-Djava.awt.headless=true', '-cp', $probeCp, 'com.perso.T4C.server.NativeRestoreCheck', 'monitor', $profiles, $stateSrc, $stopMon)
  Start-Sleep -Seconds 4
  $reportSrc = Join-Path $Work 'soak-source.json'
  $soak = Start-Java 'soak-source' @('-Djava.awt.headless=true', '-cp', $cp, (Join-Path $PSScriptRoot 'NativeAdapterSoak.java'), $endpoint, $profiles, $SoakSeconds, $map, $reportSrc, $server.Id) 'src'
  $soakStart = Get-Date; Say "soak on the source started ($SoakSeconds s)"
  Start-Sleep -Seconds ($SoakSeconds - 15)
  Copy-Item $stateSrc (Join-Path $Work 'monitor-source-before-kill.json')
  Stop-Process -Id $server.Id -Force; $server.WaitForExit(10000) | Out-Null
  Say "source server pid hard-killed 15 s before the soak ends (live copied leases will be in the backup); HasExited=$($server.HasExited)"
  if (Test-Health) { throw 'Health still answers after the kill' }
  $server = $null
  if (-not $soak.WaitForExit(120000)) { throw 'Soak did not finish' }
  Say "soak exited with code $($soak.ExitCode)"; $soak = $null
  New-Item $stopMon -ItemType File | Out-Null; $monitor.WaitForExit(20000) | Out-Null; $monitor = $null
  Soak-Allowed $reportSrc 'source' | Out-Null

  # ---- 3. source summary and verification (the database is static now: nothing writes)
  $sumSrc = Join-Path $Work 'summary-source.json'
  Check-Tool @('summary', $sumSrc) | Out-Null
  $s = Get-Content $sumSrc -Raw | ConvertFrom-Json
  Say ("phase 3: source: {0} characters, {1} inventory items, {2} characters with quest flags, {3} receipts, {4} combat events, {5} loot claims, {6} corpses ({7} recovered), live leases {8}" -f `
      $s.'rows.characters', $s.'characters.inventoryItems', $s.'characters.withQuestFlags', $s.'rows.rewards', $s.'rows.combat_events', $s.'rows.loot_claims', $s.'rows.corpses', $s.'corpses.recovered', $s.'info.liveLeasesNow')
  if ($s.'rows.loot_claims' -lt 1 -or $s.'rows.corpses' -lt 1 -or $s.'characters.withQuestFlags' -lt 20) { throw 'Source database is not populated with loot claims, corpses and quest flags' }
  Check-Tool @('verify', $profiles, $stateSrc, (Join-Path $Work 'verify-source.txt'), 'expect-letters') | ForEach-Object { Say "  [source] $_" }

  # ---- 4. backup and restore into a separate database of the same cluster
  $verifier = (& wsl.exe -d Ubuntu -e wslpath -u (Join-Path $PSScriptRoot 'verify_backup.py') | Select-Object -First 1).Trim()
  $helper = (& wsl.exe -d Ubuntu -e wslpath -u (Join-Path $PSScriptRoot 'restore-check-backup.sh') | Select-Object -First 1).Trim()
  $reportWin = Join-Path $Work 'backup-verification.json'
  $reportWsl = (& wsl.exe -d Ubuntu -e wslpath -u $reportWin | Select-Object -First 1).Trim()
  $srcName = ($sourceUrl -replace '\?.*$', '' -replace '^.*/', '')
  $out = & wsl.exe -d Ubuntu -e bash $helper $srcName $restoreName $verifier $reportWsl
  if ($LASTEXITCODE -ne 0) { throw "restore-check-backup.sh failed ($LASTEXITCODE)" }
  $vb = Get-Content $reportWin -Raw | ConvertFrom-Json
  Say ("phase 4: verify_backup.py: restore database $restoreName created and every online table compared; report keys: " + (($vb.PSObject.Properties.Name) -join ','))
  Add-Content -LiteralPath $log -Value ($vb | ConvertTo-Json -Depth 6)

  # ---- 5. the restore must equal the source
  $env:T4C_DB_URL = $restoreUrl
  $sumRes = Join-Path $Work 'summary-restore.json'
  Check-Tool @('summary', $sumRes) | Out-Null
  Check-Tool @('compare', $sumSrc, $sumRes) | ForEach-Object { Say "phase 5: $_" }
  Check-Tool @('verify', $profiles, $stateSrc, (Join-Path $Work 'verify-restore-static.txt'), 'expect-letters') | ForEach-Object { Say "  [restore, before any server] $_" }

  # ---- 6. a server on the restore only; leases copied from the dump fence the adapters until they expire
  $stateRes = Join-Path $Work 'monitor-restore.json'; $stopMon2 = Join-Path $Work 'monitor-restore.stop'
  $server = Start-Server 'res1'; Wait-Health $server; $tUp = Get-Date; Say "phase 6: server on the restore healthy (pid $($server.Id)); copied leases are still in force"
  $monitor = Start-Java 'monitor-restore' @('-Djava.awt.headless=true', '-cp', $probeCp, 'com.perso.T4C.server.NativeRestoreCheck', 'monitor', $profiles, $stateRes, $stopMon2)
  Start-Sleep -Seconds 3
  $reportRes = Join-Path $Work 'soak-restore.json'
  $soak = Start-Java 'soak-restore' @('-Djava.awt.headless=true', '-cp', $cp, (Join-Path $PSScriptRoot 'NativeAdapterSoak.java'), $endpoint, $profiles, $RestoredSoakSeconds, $map, $reportRes, $server.Id) 'res1'
  Say "soak on the restore started ($RestoredSoakSeconds s)"
  if (-not $soak.WaitForExit(($RestoredSoakSeconds + 120) * 1000)) { throw 'Restored soak did not finish' }
  Say "restored soak exited with code $($soak.ExitCode)"; $soak = $null
  $srRes = Soak-Allowed $reportRes 'restore'
  Say "restored soak report: $reportRes ($(@($srRes.players).Count) adapters)"
  Stop-Graceful $server 'res1'; $server = $null
  # intentional reconnect: a new server process on the same restored database; leases were released by the graceful stop.
  # (A repeated greeting is not possible here: the soak has walked the characters away from the opening NPC. The final
  # verification instead requires exactly one opening letter per character after all of this play.)
  $server = Start-Server 'res2'; Wait-Health $server; Say 'server restarted on the restore (reconnect round)'
  Start-Sleep -Seconds 3
  $reportRes2 = Join-Path $Work 'soak-restore-2.json'
  $soak = Start-Java 'soak-restore-2' @('-Djava.awt.headless=true', '-cp', $cp, (Join-Path $PSScriptRoot 'NativeAdapterSoak.java'), $endpoint, $profiles, $ReconnectSoakSeconds, $map, $reportRes2, $server.Id) 'res2'
  if (-not $soak.WaitForExit(($ReconnectSoakSeconds + 120) * 1000)) { throw 'Second restored soak did not finish' }
  Say "second restored soak exited with code $($soak.ExitCode)"; $soak = $null
  Soak-Allowed $reportRes2 'restore, after restart' | Out-Null
  Stop-Graceful $server 'res2'; $server = $null
  New-Item $stopMon2 -ItemType File | Out-Null; $monitor.WaitForExit(20000) | Out-Null; $monitor = $null

  # lease timeline on the restore: how long copied leases kept the adapters out
  $mon = Get-Content $stateRes -Raw | ConvertFrom-Json
  $rows = $mon.timeline | ForEach-Object { $f = $_ -split ','; [pscustomobject]@{ At = [long]$mon.startedMs + [long]$f[0]; Ok = $f[1] -eq '1'; Active = [int]$f[2] } }
  $first = $rows | Select-Object -First 1
  Say ("restore lease timeline: {0} samples; live leases at the first sample {1}; max live {2}" -f @($rows).Count, $first.Active, ($rows | Measure-Object -Property Active -Maximum).Maximum)

  # ---- final judgement of the restore against what BOTH samplers saw committed, and a final summary
  Check-Tool @('verify', $profiles, $stateRes, (Join-Path $Work 'verify-restore-final.txt'), 'expect-letters') | ForEach-Object { Say "  [restore, final, restore sampler] $_" }
  Check-Tool @('verify', $profiles, $stateSrc, (Join-Path $Work 'verify-restore-final-vs-source.txt'), 'expect-letters') | ForEach-Object { Say "  [restore, final, source sampler] $_" }
  $sumFin = Join-Path $Work 'summary-restore-final.json'
  Check-Tool @('summary', $sumFin) | Out-Null
  $f = Get-Content $sumFin -Raw | ConvertFrom-Json
  Say ("final restore: {0} receipts (source {1}), {2} combat events (source {3}), {4} loot claims (source {5}), {6} corpses (source {7}), {8} transfer events (source {9})" -f `
      $f.'rows.rewards', $s.'rows.rewards', $f.'rows.combat_events', $s.'rows.combat_events', $f.'rows.loot_claims', $s.'rows.loot_claims', $f.'rows.corpses', $s.'rows.corpses', $f.'rows.transfer_events', $s.'rows.transfer_events')
  if ($f.'rows.combat_events' -le $s.'rows.combat_events') { throw 'No new gameplay was committed on the restore' }
  $ok = $true
} catch { Say "FAILURE: $($_.Exception.Message)"; throw } finally {
  $env:T4C_DB_URL = $sourceUrl
  foreach ($proc in @($soak, $monitor, $server)) { if ($proc -and -not $proc.HasExited) { Stop-Process -Id $proc.Id -Force } }
  Remove-Item (Join-Path $Work 'control-*.token') -ErrorAction SilentlyContinue
  if ($ok) { Remove-Item (Join-Path $profiles '*.json') -ErrorAction SilentlyContinue }
  Say ("END " + $(if ($ok) { 'restore check passed' } else { 'restore check failed or run aborted' }))
}
