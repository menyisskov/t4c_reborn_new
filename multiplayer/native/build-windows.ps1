param([string]$CMake)
$ErrorActionPreference = 'Stop'
$nativeRoot = $PSScriptRoot
$repositoryRoot = Split-Path (Split-Path $nativeRoot -Parent) -Parent
$sourceRoot = Join-Path $repositoryRoot 'target/glfw-online-source'
$buildRoot = Join-Path $repositoryRoot 'target/glfw-online-build'
$resourceRoot = Join-Path $repositoryRoot 'src/main/resources/online-native/windows-x86_64'
$revision = '7b6aead9fb88b3623e3b3725ebb42670cbe4c579'
$patchPath = Join-Path $nativeRoot 'glfw-3.4-owned-window.patch'

function Check-Command([string]$Description) {
    if ($LASTEXITCODE -ne 0) { throw "$Description failed (exit $LASTEXITCODE)" }
}

if (!$CMake) {
    $found = Get-Command cmake -ErrorAction SilentlyContinue
    if ($found) { $CMake = $found.Source }
    else {
        $locator = Join-Path ${env:ProgramFiles(x86)} 'Microsoft Visual Studio/Installer/vswhere.exe'
        if (!(Test-Path -LiteralPath $locator)) { throw 'Install Visual Studio 2022 C++ tools and CMake, or pass -CMake.' }
        $CMake = & $locator -version '[17.0,18.0)' -products '*' -find '**\cmake.exe' | Select-Object -First 1
    }
}
if (!$CMake -or !(Test-Path -LiteralPath $CMake)) { throw 'CMake was not found.' }
if (!(Test-Path -LiteralPath $sourceRoot)) {
    & git clone --depth 1 --branch 3.4 https://github.com/glfw/glfw.git $sourceRoot
    Check-Command 'GLFW source download'
}
$actualRevision = (& git -C $sourceRoot rev-parse HEAD).Trim()
Check-Command 'GLFW revision check'
if ($actualRevision -ne $revision) { throw 'Unexpected GLFW revision; refusing to build or reset the source directory.' }
# Repeated builds accept exactly our patch, never silently discard other source edits.
& git -C $sourceRoot apply --reverse --check $patchPath 2>$null
if ($LASTEXITCODE -ne 0) {
    if ((& git -C $sourceRoot status --porcelain)) { throw 'GLFW source has unexpected edits.' }
    & git -C $sourceRoot apply $patchPath
    Check-Command 'GLFW patch application'
}
$expectedDiff = (Get-Content -LiteralPath $patchPath -Raw).Replace("`r`n", "`n").Trim()
$actualDiff = ((& git -C $sourceRoot diff -- src/win32_window.c) -join "`n").Trim()
if ($actualDiff -ne $expectedDiff -or ((& git -C $sourceRoot status --porcelain) -join "`n") -ne ' M src/win32_window.c') {
    throw 'GLFW source differs from the reviewed patch.'
}
& $CMake -S $sourceRoot -B $buildRoot -G 'Visual Studio 17 2022' -A x64 `
    '-DBUILD_SHARED_LIBS=ON' '-DGLFW_BUILD_EXAMPLES=OFF' '-DGLFW_BUILD_TESTS=OFF' `
    '-DGLFW_BUILD_DOCS=OFF' '-DGLFW_INSTALL=OFF' '-DUSE_MSVC_RUNTIME_LIBRARY_DLL=OFF' `
    '-DCMAKE_SHARED_LINKER_FLAGS=/Brepro'
Check-Command 'GLFW configure'
& $CMake --build $buildRoot --config Release --parallel 4
Check-Command 'GLFW build'
New-Item -ItemType Directory -Force -Path $resourceRoot | Out-Null
Copy-Item -LiteralPath (Join-Path $buildRoot 'src/Release/glfw3.dll') -Destination (Join-Path $resourceRoot 'glfw3.dll')
Copy-Item -LiteralPath (Join-Path $sourceRoot 'LICENSE.md') -Destination (Join-Path $resourceRoot 'GLFW-LICENSE.txt')
$digest = (Get-FileHash -LiteralPath (Join-Path $resourceRoot 'glfw3.dll') -Algorithm SHA256).Hash.ToLowerInvariant()
[IO.File]::WriteAllText((Join-Path $resourceRoot 'glfw3.dll.sha256'), "$digest`n", [Text.Encoding]::ASCII)
Write-Host "Built online-only GLFW from $revision. SHA-256: $digest"
Write-Host 'Review the DLL/checksum diff and run native two-client focus QA before committing a rebuilt library.'

