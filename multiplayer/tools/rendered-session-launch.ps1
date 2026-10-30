<#
.SYNOPSIS
  Starts one program from a SCRUBBED process environment (T4C-0350, the T4C-0311 rehearsal rules).

.DESCRIPTION
  -SpecFile names a JSON file with: filePath, argumentLine, workingDirectory, stdoutFile, stderrFile, scratchRoot, javaExe (optional),
  passDb (bool), wait (bool), extraEnv (array of NAME=value). The child gets only SystemRoot/windir/ComSpec, a PATH of the JDK bin,
  System32 and Windows PowerShell, a TEMP/profile directory created fresh under scratchRoot, and (with passDb) the three T4C_DB_*
  values already present in THIS process (use it under acceptance-cluster.ps1 -Action RunEnv). Nothing else is inherited: no T4C_*
  variable, no repository on PATH. Standard output and error go to files. The process id of the cmd.exe wrapper (or, with wait, the
  exit code) is written to "<SpecFile>.result". Secrets are never printed or written.
#>
param([Parameter(Mandatory = $true)][string]$SpecFile)
$ErrorActionPreference = 'Stop'
$spec = Get-Content -LiteralPath $SpecFile -Raw | ConvertFrom-Json
$javaExe = $spec.javaExe
if (-not $javaExe) { $javaExe = (Get-Command java -ErrorAction Stop).Source }
$jdkBin = Split-Path -Parent $javaExe
$profileDir = Join-Path $spec.scratchRoot ([guid]::NewGuid().ToString('N'))
foreach ($d in 'profile', 'appdata', 'local', 'temp') { New-Item -ItemType Directory -Force -Path (Join-Path $profileDir $d) | Out-Null }
$sys = Join-Path $env:SystemRoot 'System32'
$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = Join-Path $sys 'cmd.exe'
$psi.Arguments = '/c ""' + $spec.filePath + '" ' + $spec.argumentLine + ' > "' + $spec.stdoutFile + '" 2> "' + $spec.stderrFile + '""'
$psi.WorkingDirectory = $spec.workingDirectory
$psi.UseShellExecute = $false
$psi.CreateNoWindow = $true
$psi.EnvironmentVariables.Clear()
$psi.EnvironmentVariables['SystemRoot'] = $env:SystemRoot
$psi.EnvironmentVariables['windir'] = $env:SystemRoot
$psi.EnvironmentVariables['ComSpec'] = Join-Path $sys 'cmd.exe'
$psi.EnvironmentVariables['PATH'] = $jdkBin + ';' + $sys + ';' + (Join-Path $sys 'WindowsPowerShell\v1.0')
$psi.EnvironmentVariables['TEMP'] = Join-Path $profileDir 'temp'
$psi.EnvironmentVariables['TMP'] = Join-Path $profileDir 'temp'
$psi.EnvironmentVariables['USERPROFILE'] = Join-Path $profileDir 'profile'
$psi.EnvironmentVariables['APPDATA'] = Join-Path $profileDir 'appdata'
$psi.EnvironmentVariables['LOCALAPPDATA'] = Join-Path $profileDir 'local'
$psi.EnvironmentVariables['HOMEDRIVE'] = $env:SystemDrive
$psi.EnvironmentVariables['HOMEPATH'] = '\'
if ($spec.passDb) {
  foreach ($n in 'T4C_DB_URL', 'T4C_DB_USER', 'T4C_DB_PASSWORD') {
    $v = [Environment]::GetEnvironmentVariable($n)
    if (-not $v) { throw "$n is not set in this process" }
    $psi.EnvironmentVariables[$n] = $v
  }
}
foreach ($pair in @($spec.extraEnv)) {
  if (-not $pair) { continue }
  $i = $pair.IndexOf('=')
  if ($i -lt 1) { throw 'extraEnv entries are NAME=value' }
  $psi.EnvironmentVariables[$pair.Substring(0, $i)] = $pair.Substring($i + 1)
}
$process = [System.Diagnostics.Process]::Start($psi)
if ($spec.wait) {
  $process.WaitForExit()
  [IO.File]::WriteAllText($SpecFile + '.result', [string]$process.ExitCode)
} else {
  [IO.File]::WriteAllText($SpecFile + '.result', [string]$process.Id)
}
