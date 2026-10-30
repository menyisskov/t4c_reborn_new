# Live native account-key rotation (T4C-0219)

The task was renumbered from its original T4C-0218 reservation after a
separate native UI delivery used that ID. The immutable probe source and
private run artifacts retain their original `0218` labels and database names;
those labels identify the recorded runs, not a second delivered task.

`NativeKeyRotationProbe.java` checks a live case not covered by the existing
`OnlineStoreTest`, `AccountRosterTest` or `NativeRosterProbe`: two native player
sessions are active while an operator rotates only account A's key. The probe
uses the existing `OnlineStore.rotateAccountKey` API. It does not alter the
server or manually rewrite a lease, character or save.

On an owned persistent loopback server, it provisions fresh A and B accounts
at the hosted starting spawn. Both actual `OnlineConnection` adapters join and
observe each other. After rotation, the old A key must receive 401 from roster
and 409 from join, and a fresh old-key native adapter must disconnect without a
private view. The `/join` route maps a rejected Store acquire to 409, while the
roster route returns 401 for an invalid key. The old active A adapter sends a
new direction intent, then must fail
closed. The committed A record after disconnection must exactly match the
record immediately after rotation: identity, revision, progress, balances and
position. This bounds the post-rotation check; it does not assert that a
pre-rotation pending checkpoint was rolled back. B remains connected, its key
can still read the roster, it moves and retains its private progress. The new
A key then rejoins the same character with the committed progress, and both
native adapters again observe each other. No ambiguous gameplay mutation is
replayed. Private admin shutdown must exit the owned server with code 0.

## Isolation and run

Obtain explicit ownership of a fresh `t4c_rotation_0218_*` database on local
PostgreSQL and an unused dedicated loopback port before running. The probe
does not create/drop databases, control the PostgreSQL service or use QA18765.
Build both client and server from the same explicit committed source in a
create-only TEMP archive. Copy their JARs, server `lib/` and both collision
maps into a create-only TEMP runtime. Compile the probe there against those
copied artifacts with Java 21. Do not use live shared `target` classes.

The copied runtime layout is:

```text
RUNTIME/t4c-server.jar
RUNTIME/lib/*
RUNTIME/client/T4C.jar
RUNTIME/probe/com/perso/T4C/server/NativeKeyRotationProbe.class
RUNTIME/assets/maps/worldmap/worldmap.colbin
RUNTIME/assets/maps/dungeon/DungeonMap.colbin
```

From the repository root after an approved database and port grant:

```powershell
$runtime = '<fresh immutable TEMP runtime>'
$output = Join-Path $env:TEMP ('t4c-0218-evidence-' + [guid]::NewGuid().ToString('N'))
$dependencies = (Join-Path $runtime 'client/T4C.jar') + ';' +
  (Join-Path $runtime 't4c-server.jar') + ';' + (Join-Path $runtime 'lib/*')
javac -proc:none -cp $dependencies -d (Join-Path $runtime 'probe') multiplayer/tools/NativeKeyRotationProbe.java
if ($LASTEXITCODE -ne 0) { throw 'Probe compilation failed' }
$probeClasspath = (Join-Path $runtime 'probe') + ';' + $dependencies
# Set T4C_DB_URL, T4C_DB_USER and T4C_DB_PASSWORD privately for a newly
# created t4c_rotation_0218_* database; never print or commit them.
$javaExe = (Get-Command java -ErrorAction Stop).Source
java -cp $probeClasspath com.perso.T4C.server.NativeKeyRotationProbe $runtime $javaExe 18775 $output
if ($LASTEXITCODE -ne 0) { throw 'Key-rotation probe failed; preserve private output and DB' }
Get-Content -LiteralPath (Join-Path $output 'report.txt')
```

The output directory must not exist before launch. It retains private admin
control and raw child logs. Treat it as private; the sanitized report contains
no account key, database password or admin token. The server is stopped only
through its owned child handle and private control token, with a reported
forced fallback only if graceful failure cleanup fails. Preserve failed runs.
This is local adapter and persistence evidence, not rendered or remote Beta
acceptance.

## Recorded evidence

The successful local run used immutable committed source
`130bc3c0389aa08f801c8c69f8f11abdc32c724d` (protocol 5/schema 10).
An isolated archive built the client and server, and the final copied probe
compiled with `javac -proc:none`. SHA-256: probe Java
`3FB42EA1628B924B6039318E6B54DF1D4E56C27FEDC914458214BB2C9DA37448`,
server JAR `D5BBD75EAAC7E19AF976B316BA6FCFBD5321FC0803E604BB29760CB18A9F950C`,
client JAR `EEF5FDD51199487D6F14F4B16686154A82C9168A22E81049AD1E87CE4E8D808B`.

The final probe exited 0 on dedicated port 18775 against new retained database
`t4c_rotation_0218_312c3ec2435a` on existing PG55432. The old A key failed
roster (401), direct join (409) and native rejoin; its active adapter failed
closed without a post-rotation committed change. The new key rejoined the same
character and committed progress. B retained its own connected session,
account access and progress while moving. The server recorded one expected
fenced-session `Conflict` diagnostic, then exited 0 after the revoked binding
drained. Port 18775 was free afterward. Read-only database checks found schema
10, two accounts, two characters and zero held leases. Sanitized report:
`%TEMP%/t4c-0218-evidence-07fecbb11bff4844a72c1711703fc745/report.txt`,
SHA-256 `2F83E9DE19FB6473339FFC11627568FB14CB6F5FC7BA11E2AA7AC3D630A68569`.
The isolated source/build/runtime and private launcher log are under
`%TEMP%/t4c-0216-final-130bc3c0-ef9e7e43828444d9833675f04e654ba9/`.

Preserve the first failed run: `%TEMP%/t4c-0218-evidence-71076f38fd124617862a1ed4e4c8ab37/`
and database `t4c_rotation_0218_91def691bf71`. Its probe expected HTTP 401
from direct `/join`, although the server maps Store conflict to 409. The
sanitized failure report SHA-256 is
`B8625B3C08B61E4D5D17F7FFB5BC5DCBC67EA50BA947ABDC7ADD3515F6299D12`.
Failure cleanup stopped only its owned server, but it exited nonzero because
shutdown began while the revoked A binding was still draining. The corrected
run waited for that disconnect and exited 0, so the first nonzero exit is not
evidence of a deterministic shutdown failure after the rotation drain.

This is a local native adapter security check. It does not establish a
rendered-client run, remote deployment or the complete Beta gate.

Re-run on the current build (T4C-0353, 2026-10-30, `origin/main` 3e147d8b plus the T4C-0353 server metrics change, protocol 8/schema 13, unchanged probe source,
disposable PG16 acceptance cluster `t4c_acc_9f2311e03b41` on 57704 holding the dedicated database `t4c_rotation_0218_a1b6134dcd77`, server on 18851, cluster destroyed afterwards):
**PASS**. Roster 401 and join 409 for the old key, the old active adapter failed closed without a post-rotation committed change, the new key rejoined the same
committed character, the observer stayed connected and kept playing, and the owned persistent server exited 0 after the private stop.
