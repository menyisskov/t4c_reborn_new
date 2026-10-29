# Windows x64 online client test distribution

This is a create-only test package for the native **online** client. It contains the
desktop application JAR, runtime dependency JARs, tracked game assets, the licensed
online GLFW correction, and a headless integrity checker. It contains no JRE. The
tester needs Windows x64, an installed x64 Java 21 or newer runtime on `PATH`, and
network access to the intended server. The package is about 640 MB extracted
(about 290 MB of it is the tracked `assets/origin/` folder of original-game files, which
the online client was observed not to need; see "Clean-machine rehearsal" below).
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
`launch-online.bat` starts the default local loopback profile or accepts
`-CheckOnly`; use PowerShell for endpoint or private-profile arguments.

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
  the GLFW extraction directory is not removed on exit (one small directory per launch).
- With `assets/origin/` moved away, the client (started with `java -cp`, bypassing the
  manifest check) connected and rendered its window the same way, so that folder is dead weight
  in this package. Whether to stop shipping it is an owner decision (it also holds original
  game executables and DLLs that antivirus products may flag); the builder still copies it.
- Two builder/launcher defects found by this rehearsal were fixed: the builder used a .NET Core
  only API and failed under Windows PowerShell 5.1, and the launcher died with a "NativeCommandError"
  on the client's first stderr line under 5.1 (so `launch-online.bat` could never start the
  client). The launcher now starts the client with `Start-Process` and file redirection.

