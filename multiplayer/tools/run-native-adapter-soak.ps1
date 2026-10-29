param(
    [ValidateRange(1024,65535)][int]$Port = 18765,
    [ValidateRange(10,86400)][int]$Seconds = 7200,
    [string]$Profiles,
    [long]$ServerProcessId = 0,
    [string]$Report
)
$ErrorActionPreference = 'Stop'
$soakRoot = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$soakJar = Join-Path $soakRoot 'multiplayer/server/target/t4c-server.jar'
$soakClasspathFile = Join-Path $soakRoot 'target/classpath.txt'
$soakCollision = Join-Path $soakRoot 'assets/maps/worldmap/worldmap.colbin'
$soakDungeonCollision = Join-Path $soakRoot 'assets/maps/dungeon/DungeonMap.colbin'
if (!(Test-Path -LiteralPath $soakJar) -or !(Test-Path -LiteralPath $soakClasspathFile)) {
    throw 'Build desktop and multiplayer packages plus target/classpath.txt before running this harness.'
}
if (!(Test-Path -LiteralPath $soakCollision) -or !(Test-Path -LiteralPath $soakDungeonCollision)) {
    throw 'Protocol 4 QA requires the starting-world and dungeon collision assets in their normal sibling layout.'
}
$soakRun = [guid]::NewGuid().ToString('N')
if (!$Profiles) {
    $Profiles = Join-Path $soakRoot "multiplayer/local/soak-$soakRun"
    New-Item -ItemType Directory -Path $Profiles | Out-Null
    if (!$env:T4C_DB_URL -or !$env:T4C_DB_USER -or !$env:T4C_DB_PASSWORD) {
        throw 'Fresh QA provisioning requires T4C_DB_URL, T4C_DB_USER and T4C_DB_PASSWORD in the environment.'
    }
    for ($soakIndex = 1; $soakIndex -le 20; $soakIndex++) {
        $soakProfile = Join-Path $Profiles ('player-{0:D2}.json' -f $soakIndex)
        & java -jar $soakJar --provision-online $soakCollision $soakProfile ('Soak {0}-{1:D2}' -f $soakRun.Substring(0,8), $soakIndex) | Out-Null
        if ($LASTEXITCODE -ne 0) { throw "QA provisioning failed at player $soakIndex; existing profiles were preserved." }
    }
}
$Profiles = (Resolve-Path -LiteralPath $Profiles).Path
if (!$Report) { $Report = Join-Path $soakRoot "target/native-adapter-soak-$soakRun.json" }
$soakClasspath = (Join-Path $soakRoot 'target/classes') + ';' +
    (Join-Path $soakRoot 'multiplayer/server/target/classes') + ';' +
    (Join-Path $soakRoot 'multiplayer/server/target/lib/*') + ';' +
    (Get-Content -LiteralPath $soakClasspathFile -Raw).Trim()
Write-Host "Starting twenty native network adapters, without renderers. Report: $Report"
# An adapter that stops prints one line to stderr; under 'Stop' Windows PowerShell 5.1 would turn that into a
# terminating error and abort the whole soak. Keep stderr visible, judge the run by the exit code.
$ErrorActionPreference = 'Continue'
& java '-Djava.awt.headless=true' -cp $soakClasspath (Join-Path $PSScriptRoot 'NativeAdapterSoak.java') "http://127.0.0.1:$Port" $Profiles $Seconds $soakCollision $Report $ServerProcessId
if ($LASTEXITCODE -ne 0) { throw 'Adapter soak failed; inspect its sanitized report if present.' }
