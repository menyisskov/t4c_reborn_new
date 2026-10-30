# Shared snippet: the package builders paste this function into every generated launcher so a tester without a suitable
# Java sees one plain message and a non-zero exit code instead of a stack trace. It is also dot-sourced by
# test-package-java-check.ps1. It needs only Windows PowerShell 5.1, downloads nothing and changes nothing.
function Assert-T4CJava {
    param([string]$Role = 'T4C')
    $fix = @(
        '',
        "T4C needs a 64-bit (x64) Java 21 or newer on this PC, and $Role could not find one.",
        'To fix it:',
        '  1. Install an x64 Java 21 (or newer) runtime, for example Eclipse Temurin 21 from https://adoptium.net/ (Windows x64, JRE or JDK).',
        '  2. Open a NEW command prompt and run:  java -version   -- it should say version "21" or higher and mention 64-Bit.',
        '  3. Start this program again.',
        'Nothing was started and nothing was changed.'
    )
    $found = Get-Command java -ErrorAction SilentlyContinue | Select-Object -First 1
    if (!$found) {
        Write-Host 'Java was not found on your PATH.'
        $fix | ForEach-Object { Write-Host $_ }
        exit 1
    }
    $lines = @()
    try {
        # `java -version` writes to stderr; under Windows PowerShell 5.1 a plain 2>&1 would raise a terminating error.
        $lines = @(& { $ErrorActionPreference = 'Continue'; & $found.Source -version 2>&1 | ForEach-Object { "$_" } })
    } catch { $lines = @() }
    $text = $lines -join "`n"
    if ($text -notmatch 'version "(\d+)(?:\.(\d+))?') {
        Write-Host "The program named 'java' ($($found.Source)) did not report a Java version."
        $fix | ForEach-Object { Write-Host $_ }
        exit 1
    }
    $major = [int]$Matches[1]
    if ($major -eq 1 -and $Matches[2]) { $major = [int]$Matches[2] }   # old style "1.8.0" means Java 8
    if ($major -lt 21) {
        Write-Host "Java $major was found at $($found.Source), but Java 21 or newer is required."
        $fix | ForEach-Object { Write-Host $_ }
        exit 1
    }
    if ($text -notmatch '64-Bit') {
        Write-Host "The Java at $($found.Source) is not a 64-bit (x64) runtime."
        $fix | ForEach-Object { Write-Host $_ }
        exit 1
    }
    return $found.Source
}
