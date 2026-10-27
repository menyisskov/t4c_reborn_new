param(
    [Parameter(Mandatory)][string]$RuntimeSource,
    [Parameter(Mandatory)][ValidatePattern('^[0-9a-f]{40}$')][string]$SourceRevision,
    [Parameter(Mandatory)][ValidatePattern('^[0-9]{1,2}$')][string]$SchemaVersion,
    [Parameter(Mandatory)][ValidatePattern('^[0-9a-fA-F]{64}$')][string]$ExpectedServerSha256,
    [Parameter(Mandatory)][ValidatePattern('^[0-9a-fA-F]{64}$')][string]$ExpectedClientSha256
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$root = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '../..')).Path
$source = (Resolve-Path -LiteralPath $RuntimeSource -ErrorAction Stop).Path
if ($source.StartsWith($root + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) { throw 'Use an immutable runtime outside the checkout.' }
$server = Join-Path $source 't4c-server.jar'
$client = Join-Path $source 'client/T4C.jar'
$required = @('t4c-server.jar','client/T4C.jar','lib','client-lib','assets/maps/worldmap/worldmap.colbin','assets/maps/dungeon/DungeonMap.colbin')
foreach ($relative in $required) { if (!(Test-Path -LiteralPath (Join-Path $source $relative))) { throw "Runtime source missing $relative" } }
if ((Get-FileHash -LiteralPath $server -Algorithm SHA256).Hash -ne $ExpectedServerSha256 -or
    (Get-FileHash -LiteralPath $client -Algorithm SHA256).Hash -ne $ExpectedClientSha256) { throw 'Runtime JAR hashes differ from the explicitly selected candidate.' }
foreach ($port in @(55433,18768)) {
    if (@(Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue).Count -ne 0) { throw "Reserved loopback port $port is occupied." }
}
$java = (Get-Command java -ErrorAction Stop).Source
$null = Get-Command javac -ErrorAction Stop
$null = Get-Command wsl.exe -ErrorAction Stop
$id = 't4c_db_0208_' + [guid]::NewGuid().ToString('N').Substring(0,12)
$runtime = Join-Path $env:TEMP ('t4c-0208-runtime-' + $id)
$evidence = Join-Path $env:TEMP ('t4c-0208-evidence-' + $id)
if ((Test-Path -LiteralPath $runtime) -or (Test-Path -LiteralPath $evidence)) { throw 'Create-only output already exists.' }
$helperWindows = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot 'db-recovery-cluster.sh')).Path.Replace('\','/')
$helperLinux = (& wsl.exe -d Ubuntu -- /usr/bin/wslpath -a $helperWindows).Trim()
if ($LASTEXITCODE -ne 0 -or $helperLinux -notmatch '^/mnt/c/[A-Za-z0-9_./ -]+/db-recovery-cluster\.sh$') { throw 'Could not resolve the Ubuntu cluster helper.' }
New-Item -ItemType Directory -Path $runtime -ErrorAction Stop | Out-Null
foreach ($relative in @('lib','client','client-lib','probe','assets/maps/worldmap','assets/maps/dungeon')) {
    New-Item -ItemType Directory -Path (Join-Path $runtime $relative) -ErrorAction Stop | Out-Null
}
Copy-Item -LiteralPath $server -Destination (Join-Path $runtime 't4c-server.jar')
Copy-Item -LiteralPath $client -Destination (Join-Path $runtime 'client/T4C.jar')
foreach ($relative in @('lib','client-lib')) {
    Get-ChildItem -LiteralPath (Join-Path $source $relative) -File | ForEach-Object {
        if ($_.Extension -ne '.jar' -or $_.LinkType) { throw 'Runtime dependency directory contains a non-JAR or linked file.' }
        Copy-Item -LiteralPath $_.FullName -Destination (Join-Path $runtime $relative)
    }
}
foreach ($relative in @('assets/maps/worldmap/worldmap.colbin','assets/maps/dungeon/DungeonMap.colbin')) {
    Copy-Item -LiteralPath (Join-Path $source $relative) -Destination (Join-Path $runtime $relative)
}
if ((Get-FileHash -LiteralPath (Join-Path $runtime 't4c-server.jar') -Algorithm SHA256).Hash -ne $ExpectedServerSha256 -or
    (Get-FileHash -LiteralPath (Join-Path $runtime 'client/T4C.jar') -Algorithm SHA256).Hash -ne $ExpectedClientSha256) { throw 'Copied runtime hash changed.' }
$cp = (Join-Path $runtime 'client/T4C.jar') + ';' + (Join-Path $runtime 't4c-server.jar') + ';' +
    (Join-Path $runtime 'lib/*') + ';' + (Join-Path $runtime 'client-lib/*')
& javac --release 21 -proc:none -cp $cp -d (Join-Path $runtime 'probe') (Join-Path $PSScriptRoot 'NativeDatabaseRecoveryProbe.java')
if ($LASTEXITCODE -ne 0) { throw 'Isolated probe compilation failed.' }
$probeCp = (Join-Path $runtime 'probe') + ';' + $cp
$oldUrl = [Environment]::GetEnvironmentVariable('T4C_DB_URL','Process')
$oldUser = [Environment]::GetEnvironmentVariable('T4C_DB_USER','Process')
$oldPassword = [Environment]::GetEnvironmentVariable('T4C_DB_PASSWORD','Process')
$created = $false
try {
    $creation = (& wsl.exe -d Ubuntu -- /bin/bash $helperLinux create $id)
    if ($LASTEXITCODE -ne 0 -or $creation -ne 'CREATE_OK') { throw 'Isolated PostgreSQL cluster initialization failed; preserve its private directory.' }
    $created = $true
    $privatePassword = (& wsl.exe -d Ubuntu -- /bin/bash $helperLinux password $id)
    if ($LASTEXITCODE -ne 0 -or $privatePassword -notmatch '^[0-9a-f]{64}$') { throw 'Private isolated database password unavailable.' }
    $env:T4C_DB_URL = "jdbc:postgresql://127.0.0.1:55433/$id"
    $env:T4C_DB_USER = 't4c_recovery'
    $env:T4C_DB_PASSWORD = $privatePassword
    & $java '-Djava.awt.headless=true' -cp $probeCp com.perso.T4C.server.NativeDatabaseRecoveryProbe $runtime $java 18768 $evidence $helperLinux $id $SourceRevision $SchemaVersion
    if ($LASTEXITCODE -ne 0) { throw 'Isolated database recovery probe failed; preserve the evidence and cluster data.' }
    Write-Host "PASS T4C-0208; private evidence: $evidence"
} finally {
    [Environment]::SetEnvironmentVariable('T4C_DB_URL',$oldUrl,'Process')
    [Environment]::SetEnvironmentVariable('T4C_DB_USER',$oldUser,'Process')
    [Environment]::SetEnvironmentVariable('T4C_DB_PASSWORD',$oldPassword,'Process')
    $privatePassword = $null
    if ($created) {
        $stopped = (& wsl.exe -d Ubuntu -- /bin/bash $helperLinux stop $id)
        if ($LASTEXITCODE -ne 0 -or $stopped -ne 'STOP_OK') { Write-Warning 'Owned isolated PostgreSQL process may remain; inspect the private cluster marker before stopping it.' }
    }
}
