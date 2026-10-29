param([Parameter(Mandatory)][string]$Package)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$packageRoot = (Resolve-Path -LiteralPath $Package -ErrorAction Stop).Path
$app = Join-Path $packageRoot 'app/T4C.jar'
$checker = Join-Path $packageRoot 'checker/online-package-check.jar'
if (!(Test-Path -LiteralPath $app -PathType Leaf) -or !(Test-Path -LiteralPath $checker -PathType Leaf)) { throw 'Build a package first.' }
$repo = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '../..')).Path
$fixture = Join-Path $repo ('target/online-client-package-tests/' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $fixture -ErrorAction Stop | Out-Null
$files = @(
    'app/T4C.jar', 'checker/online-package-check.jar', 'launch-online.ps1',
    'launch-online.bat', 'GLFW-LICENSE.txt', 'lib/dummy.jar',
    'assets/maps/worldmap/worldmap.mapbin', 'assets/maps/dungeon/DungeonMap.mapbin',
    'assets/sprites/sprites_0.bin',
    'assets/sprites/sprites_1.bin', 'assets/sprites/sprites_2.bin'
)
foreach ($relative in $files) {
    $path = Join-Path $fixture $relative
    New-Item -ItemType Directory -Path (Split-Path -Parent $path) -Force | Out-Null
    if ($relative -eq 'app/T4C.jar') { Copy-Item -LiteralPath $app -Destination $path }
    elseif ($relative -eq 'checker/online-package-check.jar') { Copy-Item -LiteralPath $checker -Destination $path }
    else { [IO.File]::WriteAllText($path, 'fixture', [Text.Encoding]::ASCII) }
}
function Write-Manifest {
    $lines = [Collections.Generic.List[string]]::new()
    $lines.Add("T4C-ONLINE-PACKAGE`t1")
    $lines.Add("fact`tsourceRevision`tfixture")
    foreach ($relative in $files) {
        $hash = (Get-FileHash -LiteralPath (Join-Path $fixture $relative) -Algorithm SHA256).Hash.ToLowerInvariant()
        $lines.Add("file`t$relative`t$hash")
    }
    [IO.File]::WriteAllLines((Join-Path $fixture 'SHA256SUMS.tsv'), $lines, [Text.UTF8Encoding]::new($false))
}
function Invoke-Check([bool]$expectSuccess) {
    $cp = $checker + ';' + $app
    # The checker reports failures on stderr; Windows PowerShell 5.1 turns 2>&1 into a terminating error under -Stop.
    & { $ErrorActionPreference = 'Continue'; & java -cp $cp com.perso.T4C.online.OnlinePackageCheck $fixture 2>&1 | Out-Null }
    if (($LASTEXITCODE -eq 0) -ne $expectSuccess) { throw "Unexpected checker result (expected success: $expectSuccess)." }
}
Write-Manifest
Invoke-Check $true
[IO.File]::WriteAllText((Join-Path $fixture 'private-profile.json'), '{"private":"must not ship"}')
Invoke-Check $false
Remove-Item -LiteralPath (Join-Path $fixture 'private-profile.json')
[IO.File]::AppendAllText((Join-Path $fixture 'assets/sprites/sprites_0.bin'), 'tampered')
Invoke-Check $false
[IO.File]::WriteAllText((Join-Path $fixture 'assets/sprites/sprites_0.bin'), 'fixture', [Text.Encoding]::ASCII)
Write-Manifest
$manifest = Join-Path $fixture 'SHA256SUMS.tsv'
$duplicate = (Get-Content -LiteralPath $manifest | Where-Object { $_ -like "file`tlib/dummy.jar`t*" })
[IO.File]::AppendAllText($manifest, $duplicate + "`n")
Invoke-Check $false
Write-Host "Positive, private-file, tamper, and duplicate-path checks passed. Fixture: $fixture"
