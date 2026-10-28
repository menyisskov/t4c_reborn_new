<#
.SYNOPSIS
  Windows wrapper for acceptance-cluster.sh: a private, disposable PostgreSQL 16 cluster in WSL Ubuntu.

.DESCRIPTION
  Actions: Create, Status, Start, Stop, Crash, Destroy, RunEnv.
  RunEnv reads the generated password from the cluster's mode-0600 file through wsl.exe into
  PROCESS-SCOPED environment variables (T4C_DB_URL, T4C_DB_USER, T4C_DB_PASSWORD) for one child
  command only, then removes them. The password is never printed, logged or put in an argument.
  -DryRun (or -WhatIf) prints the planned commands and runs nothing, touches no port or database.

.EXAMPLE
  .\acceptance-cluster.ps1 -Action Create -Port 55440 -DryRun
.EXAMPLE
  .\acceptance-cluster.ps1 -Action RunEnv -RunId t4c_acc_0123456789ab -Command java -CommandArgs @('-cp', $cp, 'com.perso.T4C.server.NativeLootProbe', $rt, $java, '18790', $out)
#>
[CmdletBinding(SupportsShouldProcess = $true)]
param(
  [Parameter(Mandatory = $true)]
  [ValidateSet('Create', 'Status', 'Start', 'Stop', 'Crash', 'Destroy', 'RunEnv')]
  [string]$Action,

  [ValidatePattern('^t4c_acc_[0-9a-f]{12}$')]
  [string]$RunId,

  [int]$Port,

  [string]$Command,

  [string[]]$CommandArgs = @(),

  [switch]$DryRun,

  [string]$Distro = 'Ubuntu'
)

$ErrorActionPreference = 'Stop'
$protectedPorts = 55432, 55433, 18765, 18766, 18770, 18771
$scriptWin = Join-Path $PSScriptRoot 'acceptance-cluster.sh'
$dry = $DryRun -or $WhatIfPreference

function Format-Plan([string]$text) { Write-Host "[dry-run] $text" }

function Get-ScriptPath {
  $p = & wsl.exe -d $Distro -e wslpath -u $scriptWin
  if ($LASTEXITCODE -ne 0 -or -not $p) { throw 'wslpath failed for acceptance-cluster.sh' }
  return ($p | Select-Object -First 1).Trim()
}

function Invoke-Cluster([string[]]$ClusterArgs) {
  $script = Get-ScriptPath
  $out = & wsl.exe -d $Distro -e bash $script @ClusterArgs
  if ($LASTEXITCODE -ne 0) { throw "acceptance-cluster.sh $($ClusterArgs[0]) failed (exit $LASTEXITCODE)" }
  return $out
}

function Assert-Port([int]$p) {
  if ($p -lt 1024 -or $p -gt 65535) { throw 'Port must be 1024-65535' }
  if ($protectedPorts -contains $p) { throw "Port $p is protected (55432, 55433, 18765, 18766, 18770, 18771) and can never be used" }
}

switch ($Action) {
  'Create' {
    if (-not $Port) { throw '-Port is required for Create' }
    Assert-Port $Port
    $id = if ($RunId) { $RunId } else { 'new' }
    if ($dry) {
      Format-Plan "wsl.exe -d $Distro -e wslpath -u $scriptWin   (path translation only)"
      Format-Plan "Windows listener check on 127.0.0.1:$Port via Get-NetTCPConnection (read-only)"
      Format-Plan "wsl.exe -d $Distro -e bash <script> create $id $Port   (initdb, pg_ctl start, createdb on loopback port $Port)"
      return
    }
    if (Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue) {
      throw "Port $Port is already listening on Windows"
    }
    Invoke-Cluster @('create', $id, "$Port")
  }
  { $_ -in 'Status', 'Start', 'Stop', 'Crash', 'Destroy' } {
    if (-not $RunId) { throw '-RunId is required' }
    $verb = $Action.ToLowerInvariant()
    if ($dry) {
      Format-Plan "wsl.exe -d $Distro -e bash <script> $verb $RunId   (verifies recorded id/port/pid first)"
      return
    }
    Invoke-Cluster @($verb, $RunId)
  }
  'RunEnv' {
    if (-not $RunId) { throw '-RunId is required' }
    if (-not $Command) { throw '-Command is required' }
    if ($dry) {
      Format-Plan "wsl.exe -d $Distro -e bash <script> status $RunId   (learn the recorded port)"
      Format-Plan "wsl.exe -d $Distro -e bash <script> password $RunId   (captured silently, never printed)"
      Format-Plan "set process env T4C_DB_URL=jdbc:postgresql://127.0.0.1:<port>/${RunId}?connectTimeout=5&socketTimeout=10"
      Format-Plan "set process env T4C_DB_USER=t4c_acc; T4C_DB_PASSWORD=<hidden>"
      Format-Plan "& $Command $($CommandArgs -join ' ')"
      Format-Plan 'then remove the three variables from this process'
      return
    }
    $statusText = (Invoke-Cluster @('status', $RunId) | Out-String)
    if ($statusText -notmatch '^UP .*port=(\d+)') { throw 'Cluster is not up; start it first' }
    $dbPort = [int]$Matches[1]
    Assert-Port $dbPort
    $secret = (Invoke-Cluster @('password', $RunId) | Select-Object -First 1).Trim()
    try {
      $env:T4C_DB_URL = "jdbc:postgresql://127.0.0.1:${dbPort}/${RunId}?connectTimeout=5&socketTimeout=10"
      $env:T4C_DB_USER = 't4c_acc'
      $env:T4C_DB_PASSWORD = $secret
      & $Command @CommandArgs
      if ($LASTEXITCODE -ne 0) { throw "Child command failed (exit $LASTEXITCODE); cluster and output preserved" }
    } finally {
      Remove-Item Env:\T4C_DB_URL, Env:\T4C_DB_USER, Env:\T4C_DB_PASSWORD -ErrorAction SilentlyContinue
      $secret = $null
    }
  }
}
