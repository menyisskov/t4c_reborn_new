param([ValidateRange(1024,65535)][int]$Port = 18765, [string]$Profile)
$ErrorActionPreference = 'Stop'
$clientRoot = Split-Path $PSScriptRoot -Parent
$clientClasses = Join-Path $clientRoot 'target/classes'
$clientClasspathFile = Join-Path $clientRoot 'target/classpath.txt'
if (!(Test-Path (Join-Path $clientClasses 'com/perso/T4C/online/OnlineClient.class'))) {
    throw 'Build the desktop game with mvn -DskipTests package first.'
}
if (!(Test-Path $clientClasspathFile)) {
    & mvn -q -f (Join-Path $clientRoot 'pom.xml') dependency:build-classpath "-Dmdep.outputFile=$clientClasspathFile"
    if ($LASTEXITCODE -ne 0) { throw 'Could not build the client classpath.' }
}
$clientClasspath = $clientClasses + ';' + (Get-Content -LiteralPath $clientClasspathFile -Raw).Trim()
$clientJava = (Get-Command java).Source
$clientLog = Join-Path $clientRoot ('target/online-client-' + [guid]::NewGuid().ToString('N'))
$clientEnvironment = @{ T4C_ONLINE_PROFILE = '' }
if ($Profile) { $clientEnvironment.T4C_ONLINE_PROFILE = (Resolve-Path -LiteralPath $Profile -ErrorAction Stop).Path }
$clientProcess = Start-Process -FilePath $clientJava -WorkingDirectory $clientRoot -WindowStyle Hidden -Environment $clientEnvironment -PassThru -ArgumentList @(
    '-cp', ('"' + $clientClasspath + '"'), 'com.perso.T4C.online.OnlineClient', "http://127.0.0.1:$Port"
) -RedirectStandardOutput ($clientLog + '.log') -RedirectStandardError ($clientLog + '-error.log')
Write-Host "Started native online client $($clientProcess.Id). Run this script again for a second player. Logs: $clientLog.log"
