@echo off
setlocal enabledelayedexpansion
set "PATH=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot\bin;C:\Program Files\apache-maven-3.9.16\bin;%PATH%"
cd /d "%~dp0"
for /f "usebackq delims=" %%A in ("target\classpath.txt") do set "DEP_CP=%%A"
java -cp "target\classes;target\natives;!DEP_CP!" com.perso.T4C.MyGame
