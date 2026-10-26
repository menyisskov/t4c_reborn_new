param([switch]$Force)
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
if (!$command -or !$command.Contains($expectedJar) -or !($command.Contains('--preview') -or $command.Contains('--online'))) {
    throw 'The recorded process is not this preview server. No process was stopped.'
}
if ($command.Contains('--online') -and !$Force) {
    if ($record.controlFile -notmatch '^online-control-[0-9a-f]{32}\.token$') { throw 'No valid private shutdown control file recorded. Use -Force only for intentional crash testing.' }
    $controlFile = Join-Path $PSScriptRoot ('server/target/' + $record.controlFile)
    $controlToken = (Get-Content -LiteralPath $controlFile -Raw).Trim()
    if ($controlToken -notmatch '^[0-9a-f]{64}$') { throw 'Invalid shutdown credentials.' }
    $controlUrl = "http://127.0.0.1:$($record.port)"
    try {
        $null = Invoke-RestMethod "$controlUrl/admin/shutdown" -Method Post -Body '' -ContentType 'text/plain' -Headers @{ Origin = $controlUrl; Authorization = 'Bearer ' + $controlToken } -TimeoutSec 3
    } catch { if (!$process.HasExited) { throw 'Graceful shutdown request failed. Use -Force only for intentional crash testing.' } }
    if (!$process.WaitForExit(20000)) { throw 'Graceful shutdown has not completed. Inspect server logs; -Force discards uncommitted movement.' }
    Write-Host 'Persistent server stopped after its bounded checkpoint flush. Check server logs for persistence failures.'
    return
}
# Forced persistent stops deliberately simulate a crash; recovery uses the last committed checkpoint.
Stop-Process -Id $record.processId
Wait-Process -Id $record.processId -Timeout 5 -ErrorAction SilentlyContinue
Write-Host 'Server stopped. Temporary state was discarded; persistent sessions recover their last committed checkpoint after lease expiry.'
