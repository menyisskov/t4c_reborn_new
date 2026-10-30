# Windows x64 online client test distribution

This is a create-only test package for the native **online** client. It contains the
desktop application JAR, runtime dependency JARs, tracked game assets, the licensed
online GLFW correction, and a headless integrity checker. It contains no JRE. The
tester needs Windows x64, an installed x64 Java 21 or newer runtime on `PATH`, and
network access to the intended server. The package is about 759 MB extracted
(287 MB of it is the tracked `assets/origin/` folder of original-game files, which
the online client does not need; see "Size breakdown and the origin folder" below).
The builder needs Git, Maven, and a Java 21
JDK. Both the builder and the launcher run under stock Windows PowerShell 5.1 or PowerShell 7. Neither Git, Maven, this checkout, nor a Maven cache is needed by the tester.

## Build and distribute

From a PowerShell prompt in a source checkout after the desired source revision is
committed and integrated:

```powershell
./multiplayer/tools/package-online-client.ps1 -Name qa-2026-10-27-a
./target/online-client-packages/qa-2026-10-27-a/launch-online.ps1 -CheckOnly
./multiplayer/tools/test-online-package-check.ps1 -Package ./target/online-client-packages/qa-2026-10-27-a
```

The name is restricted to plain filename characters. The builder refuses an existing
output and places each new package under `target/online-client-packages/`. It requires
clean tracked desktop source/assets and builds the desktop JAR and runtime classpath
from an isolated archive of committed `pom.xml`, `src/main`, and `assets` under
`target/online-client-package-build/`. This avoids reusing stale compiled classes
from the developer checkout. Never overwrite an old
package to update it. Copy the complete package directory to a fresh directory or
archive it for transfer, retaining its relative layout. `SHA256SUMS.tsv` lists every
distributed file with SHA-256 and records the Git source revision and artifact name.
The source revision identifies the checkout used for the build; the file hashes
identify the actual delivered bytes. The source revision alone is not a binary
reproducibility or provenance proof.
The focused checker fixture exercises a valid manifest, an unlisted private
profile, a tampered asset, and a duplicate manifest path. It writes only under
`target/online-client-package-tests/` and never alters the built package.

The package copies only `git ls-files assets` entries. It does not include checkout
save files, characters, ordinary preferences, GM seed files, private profiles, log
files, or crash reports. Review the manifest before publishing a bundle. The copied
GLFW license is `GLFW-LICENSE.txt`; the app JAR contains the corrected DLL and its
verified checksum resource.

## Install, check, and connect

Extract or copy the complete directory somewhere writable, including a path with
spaces if desired. A stock Windows client defaults to a script execution policy that
refuses unsigned `.ps1` files (and a zip downloaded through a browser marks every file
as from the internet), so start the launcher through an explicit policy for that one
process, or use `launch-online.bat`, which already does so:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File 'C:\Games\T4C Online QA\launch-online.ps1' -CheckOnly
```

If you prefer to call the script directly, first run
`Get-ChildItem -Recurse 'C:\Games\T4C Online QA' | Unblock-File` and make sure your
execution policy is `RemoteSigned` or looser. Use PowerShell from any working directory:

```powershell
& 'C:\Games\T4C Online QA\launch-online.ps1' -CheckOnly
& 'C:\Games\T4C Online QA\launch-online.ps1' -Endpoint 'https://game.example.org' -Profile 'C:\Private\player-one.json'
```

For local loopback development only, omit `-Endpoint` to use
`http://127.0.0.1:18765`, or set `-Port 18766`. HTTP to a non-loopback host is
rejected. HTTPS must use a trusted certificate and matching host name. Do not put
keys in the endpoint. The profile path is supplied at launch and passed through the
`T4C_ONLINE_PROFILE` environment variable to the existing `OnlineClient` entry point.
Its contents are never copied into the bundle or printed by the launcher. Store each
player's private profile outside the package and restrict its filesystem access.
`launch-online.bat` passes every argument through to the script, returns its exit code and, when
started by double-click, keeps the window open after a failure so the message can be read:
`launch-online.bat -Endpoint https://game.example.org -Profile C:\Private\player-one.json`.
A profile that only holds an account key (no character, made by `--provision-online-account`)
opens the online lobby and needs the `-Lobby` switch; a character profile joins directly.

## Java requirement (21 or newer, x64)

The package bundles no Java. Every launcher (`launch-online.ps1`, `verify-package.ps1`, and the `.bat` files that call
them) first looks for `java` on `PATH` and, when it is missing, older than 21, not 64-bit, or not a Java at all, prints a
short message with the fix and exits with code 1; no stack trace and nothing is started. To satisfy it, install any x64
Java 21 or newer runtime (for example Eclipse Temurin 21 from adoptium.net, JRE or JDK), open a new command prompt and
check that `java -version` reports 21 or higher and "64-Bit". The wording is covered by `test-package-java-check.ps1`
(fake `java.cmd` files and a PATH without Java; no install, no download).

**Option, not done: bundle a runtime with jlink.** `jdeps --print-module-deps` on this build lists
`java.base, java.desktop, java.sql, jdk.httpserver, jdk.jfr, jdk.unsupported` for the client and
`java.base, java.desktop, java.naming, java.security.jgss, java.sql, jdk.httpserver, jdk.management` for the server. On a JDK 21,
`jlink --add-modules <those, plus java.logging,java.management> --strip-debug --no-header-files --no-man-pages --compress=zip-6 --output runtime`
produced a measured 49 MB runtime for the union of both module sets (the full JDK is 328 MB). Shipping it would also need: the runtime's licence text and an update
path for security fixes, the builder copying `runtime/` into the manifest (the checker rejects unlisted files), the launcher
preferring `runtime\bin\java.exe` when present, and a re-test of the Java message. This is an owner decision; no runtime is
bundled and nothing was downloaded to measure it.

Each launch verifies the public manifest and the corrected embedded GLFW bytes
before starting the renderer. The client runs with the package directory as its
working directory and writes diagnostic output under its `logs/` directory. Keep
those logs private; inspect and sanitize them before sharing. The online launcher
never invokes the offline game entry point. An existing offline installation, saves,
and preferences do not need to be moved or changed.

If a protocol or content version mismatch prevents connection, obtain a matching
server/client pair. For an update, install a new complete package beside the old
one, check it, then use it with the same external private profile. To roll back,
start the previous complete client package against a compatible server. Server
database rollback is a separate operation requiring compatible code or a matching
restored database; do not delete receipts or player progress to force compatibility.

## Scope and limits

The checker reads package files and the embedded native DLL resource. It does not
start LibGDX, contact a server, or read saves or ordinary preferences. A passing
check establishes package file integrity, not rendering, remote connectivity,
full gameplay, or clean-machine acceptance. The required rendered two-client,
remote-machine, offline-regression, and owner QA gates remain separate in
`multiplayer/MILESTONES.md` and `multiplayer/ACCEPTANCE.md`.

## Clean-machine rehearsal (T4C-0311, 2026-10-29) and what it does not prove

A package built from `f3d4ed62` was zipped, extracted into a new empty directory with spaces
in its name outside the repository, and run from a scrubbed process environment: no `T4C_*`
variables, a `PATH` of only the JDK `bin`, `System32` and Windows PowerShell, a fresh
profile/`APPDATA`/`LOCALAPPDATA`/`TEMP` directory and an unrelated working directory. It joined
the packaged server (see [SERVER_DISTRIBUTION.md](SERVER_DISTRIBUTION.md)) on loopback. This is
a **same-machine rehearsal with a scrubbed environment**; it is not a second machine. The host
still has a JDK, a GPU driver, a Defender configuration, a firewall policy and a warm file cache
that a tester's machine may lack. Observed:

- `-CheckOnly` passed through `launch-online.bat` and through `-ExecutionPolicy Bypass`, and
  failed (as expected) under `-ExecutionPolicy Restricted` with "running scripts is disabled".
- The packaged launcher started the real client, a window titled "T4C Reborn - Online Alpha"
  connected to the server's game socket, stayed responsive, and closed cleanly with exit code 0.
- The only install-directory writes were the `logs/` files. The game wrote nothing under the
  profile directory except extracted native DLLs (LWJGL, libGDX, the pinned GLFW) in `TEMP`;
  at that time the GLFW extraction directory was not removed on exit (one small directory per launch);
  T4C-0343 fixed this, see "Per-launch residue" below.
- With `assets/origin/` moved away, the client (started with `java -cp`, bypassing the
  manifest check) connected and rendered its window the same way, so that folder is dead weight
  in this package. Whether to stop shipping it is an owner decision (it also holds original
  game executables and DLLs that antivirus products may flag); the builder still copies it.
- Two builder/launcher defects found by this rehearsal were fixed: the builder used a .NET Core
  only API and failed under Windows PowerShell 5.1, and the launcher died with a "NativeCommandError"
  on the client's first stderr line under 5.1 (so `launch-online.bat` could never start the
  client). The launcher now starts the client with `Start-Process` and file redirection.

## Per-launch residue (T4C-0343)

The online GLFW library cannot be deleted while the client is running on Windows, so the old per-launch temp folder was never removed. The
client now extracts it to `%TEMP%\t4c-online-glfw-<first 16 hex of its SHA-256>\glfw3.dll`, reuses that folder on every launch of any package
carrying the same library, and re-writes it only if the file differs (a damaged or planted file is never loaded; if the shared folder is unusable a
private fresh folder is used as before). Verified with five packaged launches (three package builds, lobby and in-game) from a scrubbed
environment with a fresh `TEMP`: one `t4c-online-glfw-*` folder after the first launch and after every later one. LWJGL and libGDX already
reuse stable per-user folders (`lwjgl_<user>`, `libgdx<user>`).

## Verify a copied package

`verify-package.bat` (or `powershell.exe -NoProfile -ExecutionPolicy Bypass -File verify-package.ps1`) inside the package re-hashes every
file against its own `SHA256SUMS.tsv`, fails on a missing, changed or unlisted file, and prints the manifest's SHA-256. The builders print
the same value; publish it separately from the download and pass it as `-ManifestSha256 <value>` to prove that the manifest itself is the
one that was built (a tamperer who edits both files and the manifest would otherwise pass the self-check). It needs the Java 21 requirement above.

## Size breakdown and the origin folder

Measured on the default package built from `34863bf2` (extracted, 934 files, 759 MB): `assets/` 731 MB, `lib/` 17.9 MB (64 JARs), `app/` 10 MB, the rest
under 1 MB. Inside `assets/`: `sprites` 232 MB, `origin` 287 MB, `maps` 119 MB, `sounds` 85 MB, `ui` 4.3 MB, `i18n` 2 MB, `editor` 1.3 MB, everything
else under 1 MB. `assets/origin/` is 82 files: `.vsf`/`.VSB` 178 MB, `.Dat` 44 MB, `.WDA` 19 MB, `.dll` 12 files 7.9 MB, `.exe` 4 files 4.9 MB,
`.bin` 8.9 MB, plus maps and fonts (the executables and DLLs are the part antivirus products may flag).

`package-online-client.ps1 -Name <new> -ExcludeOrigin` (opt-in; the default is unchanged) leaves that folder out and records `excludeOrigin true` in the
manifest. That package is 852 files and 472 MB. From a scrubbed environment against a packaged server on a disposable database it passed its
integrity check, opened the online lobby (account profile with `-Lobby`) with the logo art and character panel rendered, and joined the game with a
character profile and rendered the map, NPCs and HUD, then closed with exit 0. So the folder is not needed for the lobby or the starting area, and no Java source under `src/main` names `assets/origin`; other
features that might read it indirectly were not exercised, and nothing was removed from the default builder. The zipped size and download time were not
measured. Whether to ship it by default is an owner decision.

