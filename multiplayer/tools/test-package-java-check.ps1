# Tests the friendly Java check that the package builders paste into every launcher (package-java-check.ps1), and, when
# built packages are given, that their real launchers exit non-zero with the plain message instead of a stack trace.
# Uses fake java.cmd files in a temp folder and a PATH without any Java; downloads and installs nothing.
param([string]$ClientPackage, [string]$ServerPackage)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$snippet = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot 'package-java-check.ps1')).Path
$shell = Join-Path $env:SystemRoot 'System32\WindowsPowerShell\v1.0\powershell.exe'
$bareDir = Join-Path $env:SystemRoot 'System32'
$work = Join-Path ([IO.Path]::GetTempPath()) ('t4c-java-check-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $work -ErrorAction Stop | Out-Null

function New-FakeJava([string]$name, [string[]]$stderrLines) {
    $dir = Join-Path $work $name
    New-Item -ItemType Directory -Path $dir | Out-Null
    $body = '@echo off' + "`r`n"
    foreach ($line in $stderrLines) { $body += 'echo ' + $line + ' 1>&2' + "`r`n" }
    [IO.File]::WriteAllText((Join-Path $dir 'java.cmd'), $body, [Text.Encoding]::ASCII)
    return $dir
}
function Invoke-WithPath([string]$path, [string]$commandText) {
    $old = $env:PATH
    try {
        $env:PATH = $path
        $out = & { $ErrorActionPreference = 'Continue'; & $shell -NoProfile -ExecutionPolicy Bypass -Command $commandText 2>&1 | ForEach-Object { "$_" } }
        return [pscustomobject]@{ Code = $LASTEXITCODE; Text = ($out -join "`n") }
    } finally { $env:PATH = $old }
}
function Assert-Case([string]$label, $result, [int]$code, [string]$mustContain) {
    if ($result.Code -ne $code -or $result.Text -notlike "*$mustContain*") {
        throw "$label failed: exit $($result.Code), output: $($result.Text)"
    }
    if ($result.Text -match 'at <ScriptBlock>|Exception|CategoryInfo|\bat line:') { throw "$label printed a stack trace: $($result.Text)" }
    Write-Host "ok  $label"
}
$call = ". '$snippet'; `$j = Assert-T4CJava 'test'; Write-Output ('JAVA=' + `$j)"
try {
    Assert-Case 'no java on PATH' (Invoke-WithPath $bareDir $call) 1 'Java was not found'
    $old = New-FakeJava 'java8' @('java version "1.8.0_301"', 'Java HotSpot(TM) 64-Bit Server VM (build 25.301-b09, mixed mode)')
    Assert-Case 'Java 8' (Invoke-WithPath ($old + ';' + $bareDir) $call) 1 'Java 8 was found'
    $mid = New-FakeJava 'java17' @('openjdk version "17.0.9" 2023-10-17', 'OpenJDK 64-Bit Server VM (build 17.0.9+9, mixed mode)')
    Assert-Case 'Java 17' (Invoke-WithPath ($mid + ';' + $bareDir) $call) 1 'Java 17 was found'
    $x86 = New-FakeJava 'java21x86' @('openjdk version "21.0.4" 2024-07-16 LTS', 'OpenJDK Client VM Temurin-21.0.4+7 (build 21.0.4+7-LTS, mixed mode)')
    Assert-Case '32-bit Java 21' (Invoke-WithPath ($x86 + ';' + $bareDir) $call) 1 'not a 64-bit'
    $junk = New-FakeJava 'javajunk' @('this is not a java')
    Assert-Case 'program that is not Java' (Invoke-WithPath ($junk + ';' + $bareDir) $call) 1 'did not report a Java version'
    $good = New-FakeJava 'java21' @('openjdk version "21.0.12.1" 2026-08-18 LTS', 'OpenJDK 64-Bit Server VM Temurin-21.0.12.1+1 (build 21.0.12.1+1-LTS, mixed mode, sharing)')
    Assert-Case 'Java 21 x64' (Invoke-WithPath ($good + ';' + $bareDir) $call) 0 'JAVA='
    $new = New-FakeJava 'java26' @('openjdk version "26-ea" 2027-03-17', 'OpenJDK 64-Bit Server VM (build 26-ea+3, mixed mode, sharing)')
    Assert-Case 'newer Java 26-ea' (Invoke-WithPath ($new + ';' + $bareDir) $call) 0 'JAVA='

    # Real generated launchers, run through their .bat wrappers with no Java on PATH. T4C_NO_PAUSE keeps a failing .bat from waiting for a key.
    $env:T4C_NO_PAUSE = '1'
    foreach ($entry in @(@($ClientPackage, 'launch-online.bat', '-CheckOnly'), @($ServerPackage, 'check-server.bat', ''), @($ServerPackage, 'start-server.bat', '-Preview'),
                         @($ServerPackage, 'verify-package.bat', ''), @($ClientPackage, 'verify-package.bat', ''))) {
        if (!$entry[0]) { continue }
        $package = (Resolve-Path -LiteralPath $entry[0]).Path
        $bat = Join-Path $package $entry[1]
        $result = Invoke-WithPath $bareDir ('& cmd.exe /c ""' + $bat + '" ' + $entry[2] + '"')
        Assert-Case ("$($entry[1]) in $(Split-Path -Leaf $package) without Java") $result 1 'Java 21 or newer'
    }
    Write-Host 'All Java-check cases passed.'
} finally {
    Remove-Item Env:\T4C_NO_PAUSE -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath $work -Recurse -Force -ErrorAction SilentlyContinue
}
