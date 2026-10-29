# Local Windows online server QA package

This create-only bundle packages a committed multiplayer server revision for **local
loopback QA**. It includes the server JAR, runtime dependency JARs, the two collision
maps used by the current starting slice, a headless integrity checker, and local
start/stop/check scripts. Content catalogs are inside the server JAR. The bundle
contains no JRE, PostgreSQL binary, database, credentials, private profiles,
desktop saves, preferences or logs. The operator needs installed Java 21 or newer,
PowerShell and a separately configured PostgreSQL service. The builder additionally
needs Git, Maven and a Java 21 JDK.

## Build from an exact committed revision

```powershell
./multiplayer/tools/package-online-server.ps1 -Name qa-0215-a -SourceRevision 42b32feaa9f052da8a64d435132c0d7703c98c4d
& './target/online-server-packages/qa-0215-a/check-server.ps1'
./multiplayer/tools/test-online-server-package.ps1 -Package './target/online-server-packages/qa-0215-a'
```

`SourceRevision` is mandatory and must be a full 40-hex commit SHA available locally.
The builder archives **that commit** into a new isolated `%TEMP%` source/build directory,
then runs `mvn -B -f <archive>/multiplayer/pom.xml -DskipTests package`
plus Maven's pinned runtime-classpath goal there. The builder compares every
copied dependency JAR with that resolved classpath. Dirty or
uncommitted checkout changes are excluded. Both the package path under
`target/online-server-packages/<Name>` and the isolated build directory are
create-only. Choose a new name for every build; retain old bundles for rollback.
Do not treat the source revision as proof of byte-for-byte reproducible JARs.
`SHA256SUMS.tsv` identifies the source commit and hashes the actual packaged
JARs, maps, checker and scripts. Review it before copying the whole package.

The checker rejects missing/tampered or unlisted files, duplicate or unsafe paths,
linked files/directories, and missing required server resources. `check-server.ps1`
also runs the packaged `ServerMain --check-content` to load item and monster
catalogs without a database. This is a content/integrity check, not a gameplay test.
The fixture script tests a valid manifest, unlisted private file, map tamper,
duplicate path and traversal path. It writes only a new `%TEMP%` fixture.

## Start and stop local QA

Copy the complete package to a path that can contain spaces. Configure a **new
private QA PostgreSQL database** outside the package and set `T4C_DB_URL`,
`T4C_DB_USER` and `T4C_DB_PASSWORD` in the launching process environment. Keep
those values and database backups private. The server's `--online` startup applies
its schema migration; run it only against that designated QA database. The sample
port must be free and separately reserved when other local QA runs are active.

```powershell
& 'C:\Games\T4C Server QA\start-server.ps1' -Port 18773 -StateDirectory 'C:\Private\t4c-qa-run-1'
& 'C:\Games\T4C Server QA\stop-server.ps1' -StateDirectory 'C:\Private\t4c-qa-run-1'
```

On a stock Windows client the default execution policy refuses unsigned scripts (and a browser-downloaded
zip marks each file as from the internet). Either run `Get-ChildItem -Recurse 'C:\Games\T4C Server QA' | Unblock-File`
with a `RemoteSigned` or looser policy, or start each script through an explicit per-process policy, for example
`powershell.exe -NoProfile -ExecutionPolicy Bypass -File 'C:\Games\T4C Server QA\start-server.ps1' -Port 18773`.
The package has no `.bat` wrapper. The scripts work under Windows PowerShell 5.1 and PowerShell 7.

The package has no provisioning wrapper script, but the packaged JAR carries the operator commands. With the
same `T4C_DB_*` variables set, from any directory (the profile is written to a path you choose, outside the
package, and holds an account key, so keep it private):

```powershell
java -jar 'C:\Games\T4C Server QA\t4c-server.jar' --provision-online 'C:\Games\T4C Server QA\assets\maps\worldmap\worldmap.colbin' 'C:\Private\player-one.json' PlayerOne
java -jar 'C:\Games\T4C Server QA\t4c-server.jar' --provision-online-account 'C:\Private\account-only.json'
```

If `-StateDirectory` is omitted, start creates a unique directory under
`%LOCALAPPDATA%\T4C Online Server QA` and prints its path. The directory must not
already exist and must be outside the bundle. Its `session.json`, private shutdown
token and logs are retained after stop; each run needs a fresh directory. The
launcher refuses occupied ports, non-loopback bind settings, and inherited TLS or
public-origin settings. It starts a hidden server on `127.0.0.1` and waits for a
matching `/health` response. For a database-free temporary smoke, use `-Preview`;
this mode creates no persistent player state. The scripts work without a source
checkout, Git, Maven or a Maven cache on the operator machine.

Stop verifies the exact recorded PID, process start time, command JAR/map/mode and
JAR hash before acting. Persistent stop sends the private token to the local
`/admin/shutdown` endpoint with the expected Origin, waits up to 90 seconds, and
reports a completed checkpoint only with a readable exit code 0. On timeout,
unreadable/nonzero exit or an ambiguous process, it fails visibly and retains the
state/logs. Preview stop affects only its verified temporary process. Neither
script deletes private state, another process or an old package.

For protocol mismatch, use a client and server built for the same protocol/content
version. Update by installing a new package alongside the old one and using a new
state directory. Rolling back server code requires a compatible database schema or
a separately restored matching backup; never delete committed receipts or progress
to make an old binary start. Provision/reissue private credentials and backup/restore
procedures remain in the existing multiplayer operations docs.

This launcher intentionally offers local HTTP only. Remote operation needs the
separate foreground HTTPS deployment in [TLS.md](TLS.md), with a trusted hostname,
certificate, firewall configuration and end-to-end validation. This package does
not establish clean-machine, remote, rendered two-client, full M6 or Beta evidence.

## Clean-machine rehearsal (T4C-0311, 2026-10-29)

A package built from `f3d4ed62` was zipped and extracted into a new empty directory with spaces in its
name outside the repository, then run from a scrubbed process environment (no repo on the `PATH` or working
directory, no inherited `T4C_*` variables except the three database values passed to that one process, a fresh
profile and `TEMP`). Against a newly created disposable PostgreSQL 16 cluster: `start-server.ps1` reached
`local-persistent` health with the game socket accepting; `--provision-online` created two private profiles from the
packaged JAR and map; the packaged client joined and stayed connected; `stop-server.ps1` reported a graceful
exit code 0. The package directory was byte-identical before and after (the server writes only to the external state
directory). The package holds no credentials, keys or developer paths (text entries of the package and both JARs
were scanned). This is a same-machine rehearsal, not a second machine. The Windows PowerShell 5.1 builder defects found
here (the `java -version` check and the negative-test helpers failing on stderr) were fixed.

