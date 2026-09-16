@echo off
REM Builds the runtime classpath (if needed) and launches the T4C Reborn game client.
setlocal enabledelayedexpansion

cd /d "%~dp0"

set CP_FILE=target\classpath.txt

if not exist "%CP_FILE%" (
    echo Building dependency classpath...
    call mvn -q dependency:build-classpath -Dmdep.outputFile="%CP_FILE%"
    if errorlevel 1 exit /b 1
)

for /f "usebackq delims=" %%A in ("%CP_FILE%") do set "DEP_CP=%%A"

java -cp "target\classes;target\natives;!DEP_CP!" com.perso.T4C.MyGame
