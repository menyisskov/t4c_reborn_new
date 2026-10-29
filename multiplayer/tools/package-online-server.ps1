param(
    [Parameter(Mandatory)][ValidatePattern('^[A-Za-z0-9][A-Za-z0-9._-]{0,63}$')][string]$Name,
    [Parameter(Mandatory)][ValidatePattern('^[0-9a-fA-F]{40}$')][string]$SourceRevision
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$repo = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '../..')).Path
$revision = $SourceRevision.ToLowerInvariant()
$package = Join-Path $repo ('target/online-server-packages/' + $Name)
if (Test-Path -LiteralPath $package) { throw 'Package already exists; choose a new name.' }
$null = Get-Command git -ErrorAction Stop
$null = Get-Command mvn -ErrorAction Stop
$null = Get-Command javac -ErrorAction Stop
$null = Get-Command jar -ErrorAction Stop
$java = (Get-Command java -ErrorAction Stop).Source
if ((& git -C $repo cat-file -t "$revision^{commit}").Trim() -ne 'commit' -or $LASTEXITCODE -ne 0) {
    throw 'SourceRevision must name an available committed source revision.'
}
if ((& git -C $repo rev-parse "$revision^{commit}").Trim() -ne $revision -or $LASTEXITCODE -ne 0) {
    throw 'SourceRevision must be the full SHA-1 of a commit.'
}
if ((& $java -version 2>&1 | Select-Object -First 1) -notmatch 'version "(21|2[2-9]|[3-9][0-9])') {
    throw 'Builder requires an installed Java 21 or newer JDK.'
}
$build = Join-Path $env:TEMP ('t4c-online-server-package-build-' + $Name + '-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $build -ErrorAction Stop | Out-Null
$archive = Join-Path $build 'committed-source.zip'
$source = Join-Path $build 'source'
& git -C $repo archive --format=zip "--output=$archive" $revision multiplayer src/main/java assets/i18n/lang.json assets/maps/worldmap/worldmap.colbin assets/maps/dungeon/DungeonMap.colbin
if ($LASTEXITCODE -ne 0) { throw 'Could not archive the selected committed source.' }
Expand-Archive -LiteralPath $archive -DestinationPath $source -ErrorAction Stop
& mvn -B -f (Join-Path $source 'multiplayer/pom.xml') '-DskipTests' '-Dmaven.compiler.useIncrementalCompilation=true' package 'org.apache.maven.plugins:maven-dependency-plugin:3.1.2:build-classpath' '-DincludeScope=runtime' '-Dmdep.outputFile=target/runtime-classpath.txt'
if ($LASTEXITCODE -ne 0) { throw 'Isolated multiplayer Maven build failed.' }
$artifact = Join-Path $source 'multiplayer/server/target/t4c-server.jar'
$libraryRoot = Join-Path $source 'multiplayer/server/target/lib'
$classpathFile = Join-Path $source 'multiplayer/server/target/runtime-classpath.txt'
if (!(Test-Path -LiteralPath $artifact -PathType Leaf) -or !(Test-Path -LiteralPath $libraryRoot -PathType Container)) {
    throw 'Server JAR or runtime dependencies are missing.'
}
if (!(Test-Path -LiteralPath $classpathFile -PathType Leaf)) { throw 'Resolved runtime classpath is missing.' }
$libraries = @(Get-ChildItem -LiteralPath $libraryRoot -File -Filter '*.jar')
if ($libraries.Count -eq 0) { throw 'Runtime dependencies are empty.' }
$names = @{}
foreach ($library in $libraries) {
    $key = $library.Name.ToLowerInvariant()
    if ($names.ContainsKey($key)) { throw "Dependency filename collision: $($library.Name)" }
    $names[$key] = (Get-FileHash -LiteralPath $library.FullName -Algorithm SHA256).Hash
}
$resolved = @((Get-Content -LiteralPath $classpathFile -Raw).Trim() -split [regex]::Escape([string][IO.Path]::PathSeparator) | Where-Object { $_ })
if ($resolved.Count -ne $libraries.Count) { throw 'Resolved runtime dependency count differs from copied libraries.' }
$resolvedNames = @{}
foreach ($dependency in $resolved) {
    if (!(Test-Path -LiteralPath $dependency -PathType Leaf) -or [IO.Path]::GetExtension($dependency) -ne '.jar') {
        throw 'Resolved runtime classpath has a missing or non-JAR entry.'
    }
    $key = [IO.Path]::GetFileName($dependency).ToLowerInvariant()
    if ($resolvedNames.ContainsKey($key) -or !$names.ContainsKey($key)) { throw "Resolved dependency filename collision or missing copy: $key" }
    $resolvedNames[$key] = $true
    if ((Get-FileHash -LiteralPath $dependency -Algorithm SHA256).Hash -ne $names[$key]) {
        throw "Copied dependency does not match the resolved runtime artifact: $key"
    }
}
foreach ($relative in @('assets/maps/worldmap/worldmap.colbin','assets/maps/dungeon/DungeonMap.colbin')) {
    $path = Join-Path $source $relative
    if (!(Test-Path -LiteralPath $path -PathType Leaf) -or (Get-Item -LiteralPath $path).LinkType) {
        throw "Missing or linked committed map: $relative"
    }
}
New-Item -ItemType Directory -Path $package -ErrorAction Stop | Out-Null
function Copy-File([string]$from, [string]$relative) {
    $destination = Join-Path $package $relative
    New-Item -ItemType Directory -Path (Split-Path -Parent $destination) -Force | Out-Null
    Copy-Item -LiteralPath $from -Destination $destination -ErrorAction Stop
}
Copy-File $artifact 't4c-server.jar'
foreach ($library in $libraries) { Copy-File $library.FullName ('lib/' + $library.Name) }
foreach ($relative in @('assets/maps/worldmap/worldmap.colbin','assets/maps/dungeon/DungeonMap.colbin')) {
    Copy-File (Join-Path $source $relative) $relative
}

$checkScript = @'
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$root = $PSScriptRoot
$java = (Get-Command java -ErrorAction Stop).Source
& $java -cp (Join-Path $root 'checker/server-package-check.jar') ServerPackageCheck $root
if ($LASTEXITCODE -ne 0) { throw 'Server package integrity check failed.' }
Push-Location $root
try {
    & $java '-Djava.awt.headless=true' -jar (Join-Path $root 't4c-server.jar') --check-content
    if ($LASTEXITCODE -ne 0) { throw 'Bundled server content check failed.' }
} finally { Pop-Location }
'@
$startScript = @'
param(
    [ValidateRange(1024,65535)][int]$Port = 18773,
    [int]$GameSocketPort = 0,
    [switch]$Preview,
    [string]$StateDirectory
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$root = [IO.Path]::GetFullPath($PSScriptRoot)
if ($GameSocketPort -eq 0) {
    if ($Port -ge 65535) { throw 'Set -GameSocketPort when the HTTP port is 65535.' }
    $GameSocketPort = $Port + 1
}
if ($GameSocketPort -lt 1024 -or $GameSocketPort -gt 65535 -or $GameSocketPort -eq $Port) {
    throw 'Game socket port must be a distinct port from 1024 through 65535.'
}
foreach ($name in @('T4C_TLS_KEYSTORE','T4C_TLS_PASSWORD_FILE','T4C_PUBLIC_ORIGIN')) {
    if (Test-Path -LiteralPath ('Env:' + $name)) { throw 'Local QA launcher refuses inherited TLS/origin settings. See multiplayer/TLS.md for a separate foreground TLS deployment.' }
}
if ((Test-Path Env:T4C_BIND_ADDRESS) -and $env:T4C_BIND_ADDRESS -ne '127.0.0.1') {
    throw 'Local QA launcher requires loopback bind 127.0.0.1.'
}
if (!$Preview) {
    foreach ($name in @('T4C_DB_URL','T4C_DB_USER','T4C_DB_PASSWORD')) {
        $value = [Environment]::GetEnvironmentVariable($name)
        if ([string]::IsNullOrWhiteSpace($value)) { throw "Persistent mode requires private $name environment setting." }
    }
}
$listener = Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue
if ($listener) { throw "Port $Port is occupied; no process was started." }
$socketListener = Get-NetTCPConnection -State Listen -LocalPort $GameSocketPort -ErrorAction SilentlyContinue
if ($socketListener) { throw "Game socket port $GameSocketPort is occupied; no process was started." }
& (Join-Path $root 'check-server.ps1')
if ($StateDirectory) {
    $state = [IO.Path]::GetFullPath($StateDirectory)
} else {
    if (!$env:LOCALAPPDATA) { throw 'LOCALAPPDATA is required for default private state.' }
    $state = Join-Path $env:LOCALAPPDATA ('T4C Online Server QA/' + [guid]::NewGuid().ToString('N'))
    $state = [IO.Path]::GetFullPath($state)
}
$comparison = [StringComparison]::OrdinalIgnoreCase
if ($state.Equals($root,$comparison) -or $state.StartsWith($root + [IO.Path]::DirectorySeparatorChar,$comparison) -or
    $root.StartsWith($state + [IO.Path]::DirectorySeparatorChar,$comparison)) {
    throw 'Private state must be outside the package.'
}
if (Test-Path -LiteralPath $state) { throw 'State directory already exists; use a new path.' }
New-Item -ItemType Directory -Path $state -ErrorAction Stop | Out-Null
$jar = Join-Path $root 't4c-server.jar'
$map = Join-Path $root 'assets/maps/worldmap/worldmap.colbin'
$mode = if ($Preview) { '--preview' } else { '--online' }
$expectedMode = if ($Preview) { 'local-preview' } else { 'local-persistent' }
$control = if ($Preview) { '' } else { Join-Path $state 'control.token' }
$stdout = Join-Path $state 'stdout.log'
$stderr = Join-Path $state 'stderr.log'
$java = (Get-Command java -ErrorAction Stop).Source
$oldControl = [Environment]::GetEnvironmentVariable('T4C_CONTROL_FILE','Process')
$oldBind = [Environment]::GetEnvironmentVariable('T4C_BIND_ADDRESS','Process')
$oldGameSocketPort = [Environment]::GetEnvironmentVariable('T4C_GAME_SOCKET_PORT','Process')
try {
    [Environment]::SetEnvironmentVariable('T4C_CONTROL_FILE',$control,'Process')
    [Environment]::SetEnvironmentVariable('T4C_BIND_ADDRESS','127.0.0.1','Process')
    [Environment]::SetEnvironmentVariable('T4C_GAME_SOCKET_PORT',[string]$GameSocketPort,'Process')
    $process = Start-Process -FilePath $java -ArgumentList @('-Djava.awt.headless=true','-jar',('"' + $jar + '"'),$mode,('"' + $map + '"'),$Port) -WorkingDirectory $root -WindowStyle Hidden -PassThru -RedirectStandardOutput $stdout -RedirectStandardError $stderr
} finally {
    [Environment]::SetEnvironmentVariable('T4C_CONTROL_FILE',$oldControl,'Process')
    [Environment]::SetEnvironmentVariable('T4C_BIND_ADDRESS',$oldBind,'Process')
    [Environment]::SetEnvironmentVariable('T4C_GAME_SOCKET_PORT',$oldGameSocketPort,'Process')
}
$manifest = Get-Content -LiteralPath (Join-Path $root 'SHA256SUMS.tsv') | Where-Object { $_ -like "fact`tsourceRevision`t*" }
$revision = ($manifest -split "`t")[2]
$session = @{
    processId = $process.Id
    startedUtc = $process.StartTime.ToUniversalTime().ToString('o')
    port = $Port
    gameSocketPort = $GameSocketPort
    mode = $mode
    jarPath = $jar
    jarSha256 = (Get-FileHash -LiteralPath $jar -Algorithm SHA256).Hash.ToLowerInvariant()
    sourceRevision = $revision
    controlFile = $control
}
[IO.File]::WriteAllText((Join-Path $state 'session.json'),($session | ConvertTo-Json) + "`n",[Text.UTF8Encoding]::new($false))
for ($attempt=0; $attempt -lt 40; $attempt++) {
    if ($process.HasExited) { throw "Server exited before readiness. State and logs retained at $state" }
    try {
        $health = Invoke-RestMethod "http://127.0.0.1:$Port/health" -TimeoutSec 1
        if ($health.mode -eq $expectedMode) {
            $map = Invoke-RestMethod "http://127.0.0.1:$Port/map" -TimeoutSec 1
            if ($map.gameSocketPort -ne $GameSocketPort) { throw 'Server advertised an unexpected game socket port.' }
            $socket = [System.Net.Sockets.TcpClient]::new()
            try { $connect = $socket.ConnectAsync('127.0.0.1',$GameSocketPort); if (!$connect.Wait(1000)) { continue }; $connect.GetAwaiter().GetResult() }
            finally { $socket.Dispose() }
            Write-Host "Server ready: http://127.0.0.1:$Port"
            Write-Host "Native game socket ready: 127.0.0.1:$GameSocketPort"
            Write-Host "Private state: $state"
            Write-Host "Stop: & '$root/stop-server.ps1' -StateDirectory '$state'"
            return
        }
    } catch { }
    Start-Sleep -Milliseconds 250
}
throw "Server readiness failed. Owned process/state may remain; inspect logs and stop with -StateDirectory $state. No persistence success is claimed."
'@
$stopScript = @'
param([Parameter(Mandatory)][string]$StateDirectory)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$root = [IO.Path]::GetFullPath($PSScriptRoot)
$state = (Resolve-Path -LiteralPath $StateDirectory -ErrorAction Stop).Path
$record = Get-Content -LiteralPath (Join-Path $state 'session.json') -Raw | ConvertFrom-Json
$jar = Join-Path $root 't4c-server.jar'
if ($record.jarPath -ne $jar -or $record.jarSha256 -ne (Get-FileHash -LiteralPath $jar -Algorithm SHA256).Hash.ToLowerInvariant() -or
    $record.mode -notin @('--preview','--online') -or $record.port -lt 1024 -or $record.port -gt 65535) {
    throw 'Session record does not identify this exact package and server mode.'
}
$process = Get-Process -Id $record.processId -ErrorAction SilentlyContinue
if (!$process) { throw 'Recorded server is no longer running. Logs/state retained; no checkpoint success is claimed.' }
if ($process.StartTime.ToUniversalTime().Ticks -ne ([datetime]$record.startedUtc).ToUniversalTime().Ticks) {
    throw 'Recorded process ID was reused. Nothing was stopped.'
}
$command = (Get-CimInstance Win32_Process -Filter "ProcessId = $($record.processId)").CommandLine
$map = Join-Path $root 'assets/maps/worldmap/worldmap.colbin'
if (!$command -or !$command.Contains($jar) -or !$command.Contains($map) -or !$command.Contains($record.mode) -or
    !$command.Contains([string]$record.port)) { throw 'Recorded process command does not match this server. Nothing was stopped.' }
if ($record.mode -eq '--preview') {
    Stop-Process -Id $record.processId -ErrorAction Stop
    $process.WaitForExit(5000) | Out-Null
    Write-Host 'Owned temporary preview stopped. No persistent state was involved.'
    return
}
$control = Join-Path $state 'control.token'
if ($record.controlFile -ne $control) { throw 'Private control path does not match this session.' }
$token = (Get-Content -LiteralPath $control -Raw -ErrorAction Stop).Trim()
if ($token -notmatch '^[0-9a-f]{64}$') { throw 'Invalid private control token.' }
try { $null = $process.Handle } catch { throw 'Cannot retain the process handle; no shutdown request was sent.' }
$origin = "http://127.0.0.1:$($record.port)"
try {
    $null = Invoke-RestMethod "$origin/admin/shutdown" -Method Post -Body '' -ContentType 'text/plain' -Headers @{ Origin = $origin; Authorization = 'Bearer ' + $token } -TimeoutSec 3
} catch { if (!$process.HasExited) { throw 'Graceful shutdown request failed. Inspect state/logs; no checkpoint success is claimed.' } }
if (!$process.WaitForExit(90000)) { throw 'Graceful shutdown timed out. Inspect state/logs; no checkpoint success is claimed.' }
try { $exitCode = $process.ExitCode } catch { throw 'Server exit status unreadable; no checkpoint success is claimed.' }
if ($null -eq $exitCode -or $exitCode -ne 0) { throw "Server exited with code $exitCode; no checkpoint success is claimed." }
Write-Host 'Persistent server stopped with exit code 0 after its bounded checkpoint flush. Private state and logs retained.'
'@
foreach ($pair in @(@('check-server.ps1',$checkScript),@('start-server.ps1',$startScript),@('stop-server.ps1',$stopScript))) {
    [IO.File]::WriteAllText((Join-Path $package $pair[0]),$pair[1] + "`n",[Text.UTF8Encoding]::new($false))
}
$classes = Join-Path $build 'checker-classes'
New-Item -ItemType Directory -Path $classes -ErrorAction Stop | Out-Null
& javac --release 21 -d $classes (Join-Path $PSScriptRoot 'ServerPackageCheck.java')
if ($LASTEXITCODE -ne 0) { throw 'Server checker compilation failed.' }
New-Item -ItemType Directory -Path (Join-Path $package 'checker') -ErrorAction Stop | Out-Null
& jar --create --file (Join-Path $package 'checker/server-package-check.jar') -C $classes .
if ($LASTEXITCODE -ne 0) { throw 'Server checker archive failed.' }
$lines = [Collections.Generic.List[string]]::new()
$lines.Add("T4C-ONLINE-SERVER-PACKAGE`t1")
$lines.Add("fact`tsourceRevision`t$revision")
$lines.Add("fact`tartifact`tt4c-server.jar")
$lines.Add("fact`tjavaMinimum`t21")
foreach ($file in @(Get-ChildItem -LiteralPath $package -Recurse -File | Sort-Object FullName)) {
    $relative = $file.FullName.Substring($package.Length + 1).Replace('\','/')
    $hash = (Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
    $lines.Add("file`t$relative`t$hash")
}
[IO.File]::WriteAllLines((Join-Path $package 'SHA256SUMS.tsv'),$lines,[Text.UTF8Encoding]::new($false))
& $java -cp (Join-Path $package 'checker/server-package-check.jar') ServerPackageCheck $package
if ($LASTEXITCODE -ne 0) { throw 'Created package failed integrity validation.' }
& (Join-Path $package 'check-server.ps1')
Write-Host "Created local QA server package: $package"
Write-Host "Committed source: $revision; isolated build retained at $build"
