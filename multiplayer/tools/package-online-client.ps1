param([Parameter(Mandatory)][ValidatePattern('^[A-Za-z0-9][A-Za-z0-9._-]{0,63}$')][string]$Name)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$root = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '../..')).Path
$targetRoot = Join-Path $root 'target/online-client-packages'
$bundle = Join-Path $targetRoot $Name
$buildRoot = Join-Path $root ('target/online-client-package-build/' + $Name)
if (Test-Path -LiteralPath $bundle) { throw 'Package output already exists; choose a new name.' }
if (Test-Path -LiteralPath $buildRoot) { throw 'Package build output already exists; choose a new name.' }
$null = Get-Command git -ErrorAction Stop
$null = Get-Command mvn -ErrorAction Stop
$null = Get-Command javac -ErrorAction Stop
$null = Get-Command jar -ErrorAction Stop
$clientJava = (Get-Command java -ErrorAction Stop).Source

function Assert-SourceReady {
    $changed = @(& git -C $root diff --name-only HEAD -- pom.xml src/main assets)
    if ($LASTEXITCODE -ne 0 -or $changed.Count -ne 0) { throw 'Tracked desktop source/assets are modified; commit and integrate them before packaging.' }
    $untracked = @(& git -C $root ls-files --others --exclude-standard -- src/main assets)
    if ($LASTEXITCODE -ne 0 -or $untracked.Count -ne 0) { throw 'Untracked desktop source/assets are present; review them before packaging.' }
}
Assert-SourceReady
$revision = (& git -C $root rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0 -or $revision -notmatch '^[0-9a-f]{40}$') { throw 'Cannot read source revision.' }
New-Item -ItemType Directory -Path $buildRoot -ErrorAction Stop | Out-Null
$archive = Join-Path $buildRoot 'committed-source.zip'
$sourceRoot = Join-Path $buildRoot 'source'
& git -C $root archive --format=zip "--output=$archive" $revision pom.xml src/main assets
if ($LASTEXITCODE -ne 0) { throw 'Could not archive committed desktop source.' }
Expand-Archive -LiteralPath $archive -DestinationPath $sourceRoot -ErrorAction Stop
Push-Location $sourceRoot
try {
    & mvn -B '-DskipTests' '-Dmaven.compiler.useIncrementalCompilation=true' package dependency:build-classpath '-DincludeScope=runtime' '-Dmdep.outputFile=target/classpath.txt'
    if ($LASTEXITCODE -ne 0) { throw 'Desktop Maven build failed.' }
} finally { Pop-Location }
Assert-SourceReady
if ((& git -C $root rev-parse HEAD).Trim() -ne $revision) { throw 'Source revision changed during build.' }
$app = Join-Path $sourceRoot 'target/T4C-1.0-SNAPSHOT.jar'
$classPathFile = Join-Path $sourceRoot 'target/classpath.txt'
if (!(Test-Path -LiteralPath $app -PathType Leaf) -or !(Test-Path -LiteralPath $classPathFile -PathType Leaf)) { throw 'Desktop artifact or dependency classpath missing.' }
$dependencies = @((Get-Content -LiteralPath $classPathFile -Raw).Trim() -split ';' | Where-Object { $_ })
if ($dependencies.Count -eq 0) { throw 'Runtime classpath is empty.' }
$names = @{}
foreach ($dependency in $dependencies) {
    if (!(Test-Path -LiteralPath $dependency -PathType Leaf) -or [IO.Path]::GetExtension($dependency) -ne '.jar') { throw 'Runtime classpath contains a missing or non-JAR dependency.' }
    $base = [IO.Path]::GetFileName($dependency)
    $hash = (Get-FileHash -LiteralPath $dependency -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($names.ContainsKey($base) -and $names[$base] -ne $hash) { throw "Dependency filename collision: $base" }
    $names[$base] = $hash
}
$assetEntries = @(& git -C $root -c core.quotepath=false ls-files -s -- assets)
if ($LASTEXITCODE -ne 0 -or $assetEntries.Count -eq 0) { throw 'Cannot list tracked assets.' }
$assets = [Collections.Generic.List[string]]::new()
foreach ($entry in $assetEntries) {
    if ($entry -notmatch '^([0-7]{6}) [0-9a-f]{40} [0-3]\t(.+)$' -or $Matches[1] -notin @('100644','100755')) { throw 'Tracked asset has unsupported Git mode or path.' }
    $assets.Add($Matches[2])
}
foreach ($asset in $assets) {
    if ($asset -notmatch '^assets/[A-Za-z0-9_./ -]+$' -or $asset.Contains('..') -or $asset.Contains([char]9)) { throw 'Unsafe tracked asset path.' }
    $source = Join-Path $sourceRoot ($asset -replace '/', [IO.Path]::DirectorySeparatorChar)
    if (!(Test-Path -LiteralPath $source -PathType Leaf) -or (Get-Item -LiteralPath $source).LinkType) { throw "Missing or linked committed asset: $asset" }
    $stream = [IO.File]::OpenRead($source)
    try {
        $header = [byte[]]::new(64)
        $count = $stream.Read($header, 0, $header.Length)
        if ([Text.Encoding]::ASCII.GetString($header, 0, $count).StartsWith('version https://git-lfs.github.com/spec/v1')) { throw "Git LFS pointer is not a materialized game asset: $asset" }
    } finally { $stream.Dispose() }
}
$license = Join-Path $root 'src/main/resources/online-native/windows-x86_64/GLFW-LICENSE.txt'
if (!(Test-Path -LiteralPath $license -PathType Leaf)) { throw 'Online GLFW license missing.' }

New-Item -ItemType Directory -Path $bundle -ErrorAction Stop | Out-Null
function Copy-PackageFile([string]$from, [string]$relative) {
    $to = Join-Path $bundle ($relative -replace '/', [IO.Path]::DirectorySeparatorChar)
    $parent = Split-Path -Parent $to
    New-Item -ItemType Directory -Path $parent -Force | Out-Null
    Copy-Item -LiteralPath $from -Destination $to -ErrorAction Stop
}
Copy-PackageFile $app 'app/T4C.jar'
foreach ($dependency in $dependencies) { Copy-PackageFile $dependency ('lib/' + [IO.Path]::GetFileName($dependency)) }
foreach ($asset in $assets) { Copy-PackageFile (Join-Path $sourceRoot ($asset -replace '/', [IO.Path]::DirectorySeparatorChar)) $asset }
Copy-PackageFile $license 'GLFW-LICENSE.txt'

$launch = @'
param([ValidateRange(1024,65535)][int]$Port = 18765, [string]$Profile, [string]$Endpoint, [switch]$CheckOnly)
$ErrorActionPreference = 'Stop'
if (!$Endpoint) { $Endpoint = "http://127.0.0.1:$Port" }
$parsed = $null
if (![uri]::TryCreate($Endpoint, [UriKind]::Absolute, [ref]$parsed) -or
    $parsed.UserInfo -or $parsed.Query -or $parsed.Fragment -or
    $parsed.AbsolutePath -ne '/' -or $parsed.Port -lt 1 -or
    ($parsed.Scheme -ne 'https' -and !($parsed.Scheme -eq 'http' -and $parsed.Host -eq '127.0.0.1'))) {
    throw 'Endpoint must be an HTTPS origin, or http://127.0.0.1:PORT for local development.'
}
$endpointValue = if ($parsed.Scheme -eq 'http') { "http://127.0.0.1:$($parsed.Port)" } else { $parsed.GetLeftPart([UriPartial]::Authority) }
$java = (Get-Command java -ErrorAction Stop).Source
$root = $PSScriptRoot
$cp = (Join-Path $root 'checker/online-package-check.jar') + ';' + (Join-Path $root 'app/T4C.jar') + ';' + (Join-Path $root 'lib/*')
& $java -cp $cp com.perso.T4C.online.OnlinePackageCheck $root
if ($LASTEXITCODE -ne 0) { throw 'Package integrity check failed.' }
if ($CheckOnly) { return }
if ($Profile) {
    $profilePath = (Resolve-Path -LiteralPath $Profile -ErrorAction Stop).Path
    if (!(Test-Path -LiteralPath $profilePath -PathType Leaf) -or (Get-Item -LiteralPath $profilePath).Length -gt 4096) { throw 'Online profile is missing or too large.' }
    $env:T4C_ONLINE_PROFILE = $profilePath
} else { $env:T4C_ONLINE_PROFILE = '' }
Set-Location -LiteralPath $root
$logs = Join-Path $root 'logs'
New-Item -ItemType Directory -Path $logs -Force | Out-Null
$log = Join-Path $logs ('online-' + [guid]::NewGuid().ToString('N'))
& $java -cp $cp com.perso.T4C.online.OnlineClient $endpointValue 1>> ($log + '.log') 2>> ($log + '-error.log')
if ($LASTEXITCODE -ne 0) { throw "Online client exited with code $LASTEXITCODE. Logs: $logs" }
'@
[IO.File]::WriteAllText((Join-Path $bundle 'launch-online.ps1'), $launch + "`n", [Text.UTF8Encoding]::new($false))
$batch = '@echo off' + "`r`n" + 'if /I "%~1"=="-CheckOnly" (' + "`r`n" + '  powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0launch-online.ps1" -CheckOnly' + "`r`n" + ') else (' + "`r`n" + '  powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0launch-online.ps1"' + "`r`n" + ')' + "`r`n"
[IO.File]::WriteAllText((Join-Path $bundle 'launch-online.bat'), $batch, [Text.Encoding]::ASCII)

$checkerClasses = Join-Path $buildRoot 'checker-classes'
New-Item -ItemType Directory -Path $checkerClasses -ErrorAction Stop | Out-Null
& javac --release 21 -cp $app -d $checkerClasses (Join-Path $PSScriptRoot 'OnlinePackageCheck.java')
if ($LASTEXITCODE -ne 0) { throw 'Package checker compilation failed.' }
New-Item -ItemType Directory -Path (Join-Path $bundle 'checker') | Out-Null
& jar --create --file (Join-Path $bundle 'checker/online-package-check.jar') -C $checkerClasses .
if ($LASTEXITCODE -ne 0) { throw 'Package checker archive failed.' }

$lines = [Collections.Generic.List[string]]::new()
$lines.Add("T4C-ONLINE-PACKAGE`t1")
$lines.Add("fact`tsourceRevision`t$revision")
$lines.Add("fact`tartifact`tT4C-1.0-SNAPSHOT.jar")
$lines.Add("fact`tplatform`tWindows-x86_64")
$lines.Add("fact`tjavaMinimum`t21")
$files = @(Get-ChildItem -LiteralPath $bundle -File -Recurse | Sort-Object FullName)
foreach ($file in $files) {
    $relative = [IO.Path]::GetRelativePath($bundle, $file.FullName).Replace('\','/')
    if ($relative -notmatch '^[A-Za-z0-9_./ -]+$' -or $relative.Contains('..') -or $relative.Contains([char]9)) { throw 'Unsafe package path.' }
    $lines.Add("file`t$relative`t$((Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash.ToLowerInvariant())")
}
[IO.File]::WriteAllLines((Join-Path $bundle 'SHA256SUMS.tsv'), $lines, [Text.UTF8Encoding]::new($false))
$packageClasspath = (Join-Path $bundle 'checker/online-package-check.jar') + ';' + (Join-Path $bundle 'app/T4C.jar') + ';' + (Join-Path $bundle 'lib/*')
& $clientJava -cp $packageClasspath com.perso.T4C.online.OnlinePackageCheck $bundle
if ($LASTEXITCODE -ne 0) { throw 'New package did not pass its headless integrity check.' }
Write-Host "Created online client package: $bundle"
Write-Host "Source revision: $revision; inspect SHA256SUMS.tsv for exact packaged file hashes."
