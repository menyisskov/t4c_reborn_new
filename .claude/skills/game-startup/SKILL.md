---
name: game-startup
description: Build and launch the T4C Reborn LibGDX game client on a fresh checkout or after pulling new commits. Use whenever the user asks to run, start, launch, or compile the game, or hits a "release version not supported" Maven error.
---

# Game startup (build + launch T4C Reborn)

The client is a desktop LibGDX app (`com.perso.T4C.MyGame`), built with Maven. It opens a
native OS window — you cannot screenshot it with browser tools; confirm success from the
startup log and process liveness instead.

## 1. Check the required JDK matches what's installed

`pom.xml` pins `maven.compiler.release` (currently `21`, see lines ~13-15 and ~126). This has
been bumped before across commits, so re-check it after every `git pull` rather than assuming.

```bash
grep -n "maven.compiler.release\|<release>" pom.xml
java -version
```

If the installed JDK is older than the pinned release, Maven fails fast with
`error: release version <N> not supported`. Fix by installing the matching JDK — do not lower
`pom.xml`'s release version to dodge this, since newer commits may already use language
features from the pinned version.

```bash
export PATH="$PATH:/c/Users/menyi/AppData/Local/Microsoft/WindowsApps"
winget install --id EclipseAdoptium.Temurin.<N>.JDK -e --accept-package-agreements --accept-source-agreements
```

Installed JDKs land under `C:\Program Files\Eclipse Adoptium\`. Point `JAVA_HOME` at the one
matching the pinned release for the rest of this session:

```bash
export JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-<installed-version>-hotspot"
export PATH="$JAVA_HOME/bin:$PATH"
```

If `winget` reports "Another installation is already in progress", another MSI install/update
holds the installer mutex — wait ~15s and retry; it clears on its own.

## 2. Compile and build the runtime classpath

```bash
mvn -q -o compile
mvn -q -o dependency:build-classpath -Dmdep.outputFile=target/classpath.txt
```

`-o` (offline) works once dependencies are already cached locally; drop it on the first-ever
build on a machine. `target/classpath.txt` is reused by `run.sh`/`launch.bat` and only needs
rebuilding when dependencies change — but rebuilding it is cheap and safe to always do.

## 3. Launch

```bash
java -cp "target/classes;target/natives;$(cat target/classpath.txt)" com.perso.T4C.MyGame
```

(`run.sh` / `launch.bat` at the repo root do steps 2-3 together, but assume the classpath file
already exists and don't handle the JDK-version mismatch — run step 1 manually first if unsure.)

To verify without blocking the shell, launch in the background and tail the log:

```bash
java -cp "target/classes;target/natives;$(cat target/classpath.txt)" com.perso.T4C.MyGame > /tmp/t4c_run.log 2>&1 &
sleep 8
tail -40 /tmp/t4c_run.log
```

A healthy startup logs `MonsterJsonLoader`/`ItemJsonLoader` lines (JSON content loading) and
`[PlayerStateStore] Loaded player_state from: ...` (existing character saves), with the process
still alive afterward — no window screenshot needed to confirm this much.
