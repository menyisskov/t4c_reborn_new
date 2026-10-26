$ErrorActionPreference = 'Stop'
$previewSession = Join-Path $PSScriptRoot 'server/target/preview-session.json'
if (!(Test-Path -LiteralPath $previewSession)) { Write-Host 'No preview session recorded.'; return }
$record = Get-Content -LiteralPath $previewSession -Raw | ConvertFrom-Json
$process = Get-Process -Id $record.processId -ErrorAction SilentlyContinue
if (!$process) { Write-Host 'Preview is already stopped.'; return }
if ($process.StartTime.ToUniversalTime().Ticks -ne ([datetime]$record.started).ToUniversalTime().Ticks) {
    throw 'The recorded process ID has been reused. No process was stopped.'
}
$command = (Get-CimInstance Win32_Process -Filter "ProcessId = $($record.processId)").CommandLine
$expectedJar = Join-Path $PSScriptRoot 'server/target/t4c-server.jar'
if (!$command -or !$command.Contains($expectedJar) -or !$command.Contains('--preview')) {
    throw 'The recorded process is not this preview server. No process was stopped.'
}
# Preview state is deliberately ephemeral. This cannot affect the separately launched offline game.
Stop-Process -Id $record.processId
Wait-Process -Id $record.processId -Timeout 5 -ErrorAction SilentlyContinue
Write-Host 'Preview stopped. Temporary characters have been discarded.'
