param([ValidateRange(1024,65535)][int]$Port = 18765, [switch]$Build)
$ErrorActionPreference = 'Stop'
$previewRoot = Split-Path $PSScriptRoot -Parent
$previewTarget = Join-Path $PSScriptRoot 'server/target'
$previewJar = Join-Path $previewTarget 't4c-server.jar'
$previewSession = Join-Path $previewTarget 'preview-session.json'
if (Test-Path -LiteralPath $previewSession) {
    $record = Get-Content -LiteralPath $previewSession -Raw | ConvertFrom-Json
    $existing = Get-Process -Id $record.processId -ErrorAction SilentlyContinue
    if ($existing -and $existing.StartTime.ToUniversalTime().Ticks -eq ([datetime]$record.started).ToUniversalTime().Ticks) {
        throw "Preview is already running on port $($record.port). Run stop-preview.ps1 before restarting or rebuilding."
    }
}
# Never take over another application's listener, even on systems allowing address reuse.
$listener = Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue
if ($listener) { throw "Port $Port is in use. Select another port with -Port." }
if ($Build -or !(Test-Path -LiteralPath $previewJar)) {
    & mvn -q -f (Join-Path $PSScriptRoot 'pom.xml') verify
    if ($LASTEXITCODE -ne 0) { throw 'Multiplayer build failed.' }
}
$previewJava = (Get-Command java -ErrorAction Stop).Source
$previewMap = Join-Path $previewRoot 'assets/maps/worldmap/worldmap.colbin'
$stdoutPath = Join-Path $previewTarget 'preview.log'
$stderrPath = Join-Path $previewTarget 'preview-error.log'
$previewProcess = Start-Process -FilePath $previewJava -ArgumentList @(
    '-Djava.awt.headless=true', '-jar', ('"' + $previewJar + '"'), '--preview', ('"' + $previewMap + '"'), $Port
) -WindowStyle Hidden -PassThru -RedirectStandardOutput $stdoutPath -RedirectStandardError $stderrPath
@{ processId = $previewProcess.Id; started = $previewProcess.StartTime.ToUniversalTime().ToString('o'); port = $Port } |
    ConvertTo-Json | Set-Content -LiteralPath $previewSession
for ($attempt = 0; $attempt -lt 30; $attempt++) {
    if ($previewProcess.HasExited) { throw "Preview startup failed. See $stderrPath" }
    try {
        $health = Invoke-RestMethod "http://127.0.0.1:$Port/health" -TimeoutSec 1
        if ($health.mode -eq 'local-preview') {
            Write-Host "Preview ready: http://127.0.0.1:$Port"
            Write-Host "Open in two browser windows. Stop with multiplayer/stop-preview.ps1. Logs: $stdoutPath"
            return
        }
    } catch { }
    Start-Sleep -Milliseconds 200
}
# This is the process created above, not a process found by name or port.
if (!$previewProcess.HasExited) { $previewProcess.Kill(); $previewProcess.WaitForExit() }
throw "Preview did not become ready. See $stderrPath"
