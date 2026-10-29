param([Parameter(Mandatory)][string]$Package)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$root = (Resolve-Path -LiteralPath $Package -ErrorAction Stop).Path
$checker = Join-Path $root 'checker/server-package-check.jar'
$app = Join-Path $root 't4c-server.jar'
if (!(Test-Path -LiteralPath $checker -PathType Leaf) -or !(Test-Path -LiteralPath $app -PathType Leaf)) {
    throw 'Build a server package first.'
}
$fixture = Join-Path $env:TEMP ('t4c-server-package-fixture-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $fixture -ErrorAction Stop | Out-Null
$files = @(
    't4c-server.jar', 'checker/server-package-check.jar',
    'check-server.ps1', 'start-server.ps1', 'stop-server.ps1',
    'assets/maps/worldmap/worldmap.colbin', 'assets/maps/dungeon/DungeonMap.colbin',
    'lib/fixture.jar'
)
foreach ($relative in $files) {
    $path = Join-Path $fixture $relative
    New-Item -ItemType Directory -Path (Split-Path -Parent $path) -Force | Out-Null
    if ($relative -eq 't4c-server.jar') { Copy-Item -LiteralPath $app -Destination $path }
    elseif ($relative -eq 'checker/server-package-check.jar') { Copy-Item -LiteralPath $checker -Destination $path }
    else { [IO.File]::WriteAllText($path,'fixture',[Text.Encoding]::ASCII) }
}
function Write-Manifest {
    $lines = [Collections.Generic.List[string]]::new()
    $lines.Add("T4C-ONLINE-SERVER-PACKAGE`t1")
    $lines.Add("fact`tsourceRevision`t$('a' * 40)")
    $lines.Add("fact`tartifact`tt4c-server.jar")
    $lines.Add("fact`tjavaMinimum`t21")
    foreach ($relative in $files) {
        $hash = (Get-FileHash -LiteralPath (Join-Path $fixture $relative) -Algorithm SHA256).Hash.ToLowerInvariant()
        $lines.Add("file`t$relative`t$hash")
    }
    [IO.File]::WriteAllLines((Join-Path $fixture 'SHA256SUMS.tsv'),$lines,[Text.UTF8Encoding]::new($false))
}
function Invoke-Check([bool]$expectedSuccess) {
    # The checker reports failures on stderr; Windows PowerShell 5.1 turns 2>&1 into a terminating error under -Stop.
    & { $ErrorActionPreference = 'Continue'; & java -cp $checker ServerPackageCheck $fixture 2>&1 | Out-Null }
    if (($LASTEXITCODE -eq 0) -ne $expectedSuccess) {
        throw "Unexpected checker result; expected success: $expectedSuccess"
    }
}
Write-Manifest
Invoke-Check $true
$private = Join-Path $fixture 'private-profile.json'
[IO.File]::WriteAllText($private,'{"private":"never ship"}')
Invoke-Check $false
Remove-Item -LiteralPath $private
$map = Join-Path $fixture 'assets/maps/worldmap/worldmap.colbin'
[IO.File]::AppendAllText($map,'tamper')
Invoke-Check $false
[IO.File]::WriteAllText($map,'fixture',[Text.Encoding]::ASCII)
Write-Manifest
$manifest = Join-Path $fixture 'SHA256SUMS.tsv'
$duplicate = @(Get-Content -LiteralPath $manifest | Where-Object { $_ -like "file`tlib/fixture.jar`t*" })[0]
[IO.File]::AppendAllText($manifest,$duplicate + "`n")
Invoke-Check $false
Write-Manifest
$escape = "file`t../outside.jar`t$('0' * 64)"
[IO.File]::AppendAllText($manifest,$escape + "`n")
Invoke-Check $false
Write-Host "Server package fixture passed valid, private-file, tamper, duplicate-path and traversal checks: $fixture"
