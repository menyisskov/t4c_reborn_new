# Template for the verify-package.ps1 that both package builders write into their package (the builders fill in the
# two #TOKENS#). One command re-checks a copied or downloaded package against its own SHA256SUMS.tsv.
param([string]$ManifestSha256)
$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
#JAVA_CHECK#
$java = Assert-T4CJava 'the package verifier'
$manifest = Join-Path $root 'SHA256SUMS.tsv'
if (!(Test-Path -LiteralPath $manifest -PathType Leaf)) {
    Write-Host 'PACKAGE VERIFICATION FAILED: SHA256SUMS.tsv is missing. Copy the complete package directory again.'
    exit 1
}
$manifestHash = (Get-FileHash -LiteralPath $manifest -Algorithm SHA256).Hash.ToLowerInvariant()
if ($ManifestSha256 -and $ManifestSha256.Trim().ToLowerInvariant() -ne $manifestHash) {
    Write-Host 'PACKAGE VERIFICATION FAILED: SHA256SUMS.tsv is not the manifest you were told to expect.'
    Write-Host "  expected $($ManifestSha256.Trim().ToLowerInvariant())"
    Write-Host "  found    $manifestHash"
    exit 1
}
$files = 0
foreach ($line in (Get-Content -LiteralPath $manifest)) { if ($line -like "file`t*") { $files++ } }
#CHECK_COMMAND#
if ($LASTEXITCODE -ne 0) {
    Write-Host 'PACKAGE VERIFICATION FAILED: a file is missing, changed, or not listed in the manifest. Copy the package again from its source.'
    exit 1
}
Write-Host "PACKAGE OK: $files files match SHA256SUMS.tsv."
Write-Host "Manifest SHA-256: $manifestHash"
Write-Host 'Compare that value with the one the package publisher gave you (or pass it as -ManifestSha256 to check it automatically).'
