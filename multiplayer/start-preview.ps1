param([ValidateRange(1024,65535)][int]$Port = 18765, [int]$GameSocketPort = 0, [switch]$Build, [switch]$Persistent)
$ErrorActionPreference = 'Stop'
if ($GameSocketPort -eq 0) {
    if ($Port -ge 65535) { throw 'Use -GameSocketPort when the HTTP port is 65535.' }
    $GameSocketPort = $Port + 1
}
if ($GameSocketPort -lt 1024 -or $GameSocketPort -gt 65535 -or $GameSocketPort -eq $Port) {
    throw 'Game socket port must be a distinct port from 1024 through 65535.'
}
if ($env:T4C_TLS_KEYSTORE -or $env:T4C_TLS_PASSWORD_FILE -or $env:T4C_PUBLIC_ORIGIN -or
    ($env:T4C_BIND_ADDRESS -and $env:T4C_BIND_ADDRESS -ne '127.0.0.1')) {
    throw 'start-preview.ps1 manages local HTTP only. For TLS, run the foreground server command in multiplayer/TLS.md.'
}
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
$socketListener = Get-NetTCPConnection -State Listen -LocalPort $GameSocketPort -ErrorAction SilentlyContinue
if ($socketListener) { throw "Game socket port $GameSocketPort is in use. Select another port with -GameSocketPort." }
if ($Build -or !(Test-Path -LiteralPath $previewJar)) {
    & mvn -q -f (Join-Path $PSScriptRoot 'pom.xml') verify
    if ($LASTEXITCODE -ne 0) { throw 'Multiplayer build failed.' }
}
$previewJava = (Get-Command java -ErrorAction Stop).Source
$previewMap = Join-Path $previewRoot 'assets/maps/worldmap/worldmap.colbin'
$previewMode = if ($Persistent) { '--online' } else { '--preview' }
$previewHealthMode = if ($Persistent) { 'local-persistent' } else { 'local-preview' }
$previewControlName = if ($Persistent) { 'online-control-' + [guid]::NewGuid().ToString('N') + '.token' } else { '' }
$previewEnvironment = @{ T4C_CONTROL_FILE = ''; T4C_GAME_SOCKET_PORT = [string]$GameSocketPort }
if ($Persistent) { $previewEnvironment.T4C_CONTROL_FILE = Join-Path $previewTarget $previewControlName }
$stdoutPath = Join-Path $previewTarget 'preview.log'
$stderrPath = Join-Path $previewTarget 'preview-error.log'
$previewProcess = Start-Process -FilePath $previewJava -ArgumentList @(
    '-Djava.awt.headless=true', '-jar', ('"' + $previewJar + '"'), $previewMode, ('"' + $previewMap + '"'), $Port
) -WindowStyle Hidden -Environment $previewEnvironment -PassThru -RedirectStandardOutput $stdoutPath -RedirectStandardError $stderrPath
@{ processId = $previewProcess.Id; started = $previewProcess.StartTime.ToUniversalTime().ToString('o'); port = $Port; gameSocketPort = $GameSocketPort; persistent = [bool]$Persistent; controlFile = $previewControlName } |
    ConvertTo-Json | Set-Content -LiteralPath $previewSession
for ($attempt = 0; $attempt -lt 30; $attempt++) {
    if ($previewProcess.HasExited) { throw "Preview startup failed. See $stderrPath" }
    try {
        $health = Invoke-RestMethod "http://127.0.0.1:$Port/health" -TimeoutSec 1
        if ($health.mode -eq $previewHealthMode) {
            $map = Invoke-RestMethod "http://127.0.0.1:$Port/map" -TimeoutSec 1
            if ($map.gameSocketPort -ne $GameSocketPort) { throw 'Server advertised an unexpected game socket port.' }
            $socket = [System.Net.Sockets.TcpClient]::new()
            try { $connect = $socket.ConnectAsync('127.0.0.1',$GameSocketPort); if (!$connect.Wait(1000)) { continue }; $connect.GetAwaiter().GetResult() }
            finally { $socket.Dispose() }
            Write-Host "Preview ready: http://127.0.0.1:$Port"
            Write-Host "Native game socket ready: 127.0.0.1:$GameSocketPort"
            Write-Host "Start native clients with multiplayer/start-client.ps1. Persistent mode requires -Profile. Stop with multiplayer/stop-preview.ps1. Logs: $stdoutPath"
            return
        }
    } catch { }
    Start-Sleep -Milliseconds 200
}
# This is the process created above, not a process found by name or port.
if (!$previewProcess.HasExited) { $previewProcess.Kill(); $previewProcess.WaitForExit() }
throw "Preview did not become ready. See $stderrPath"
