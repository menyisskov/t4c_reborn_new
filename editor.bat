@echo off
setlocal
cd /d "%~dp0"

where java >nul 2>&1
if errorlevel 1 goto missing_java
where mvn >nul 2>&1
if errorlevel 1 goto missing_maven

echo Building T4C Map Editor...
call mvn -q -DskipTests compile dependency:build-classpath "-Dmdep.outputFile=target/classpath.txt"
if errorlevel 1 goto build_failed

set "DEP_CP="
for /f "usebackq delims=" %%A in ("target\classpath.txt") do set "DEP_CP=%%A"
if not defined DEP_CP goto build_failed

echo Starting T4C Map Editor...
java -cp "target\classes;target\natives;%DEP_CP%" com.perso.T4C.MapEditor
if errorlevel 1 goto editor_failed
exit /b 0

:missing_java
echo Java 21 is required. Install it and add java to PATH.
pause
exit /b 1

:missing_maven
echo Maven is required. Install it and add mvn to PATH.
pause
exit /b 1

:build_failed
echo Could not build the map editor. See the Maven error above.
pause
exit /b 1

:editor_failed
echo The map editor exited with an error. See the message above.
pause
exit /b 1
