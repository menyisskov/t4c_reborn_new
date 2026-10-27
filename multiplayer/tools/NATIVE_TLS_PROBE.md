# Native persistent TLS acceptance probe (T4C-0216)

`NativeTlsProbe.java` covers the native path that `ServerTransportTest` does not:
two real `OnlineConnection` adapters against one owned persistent HTTPS server. It
creates a one-day `localhost` certificate in a new private TEMP directory, exports
only its public certificate into a JVM truststore and leaves the operating-system
trust configuration unchanged. Standard JSSE validates the chain and hostname.
The tool never installs a trust-all manager or disables hostname checking.

It first runs two isolated native adapter JVM attempts: one with an empty truststore
against `https://localhost`, one with the certificate-only truststore against
`https://127.0.0.1`. Each refusal JVM first observes a concrete JSSE
`SSLHandshakeException` on an unauthenticated `/map` request, then its native
adapter must disconnect with no private view; both account leases must remain
free. Standard TLS behavior implies an unsuccessful handshake cannot carry an
HTTP `/join` body. This is an inference plus lease observation, not a packet
capture of native credential bytes. The trusted phase then
joins two adapters concurrently, checks mutual authoritative snapshots, moves one
character, observes its position through the other adapter, checks committed global
position and reconnects both identities. The probe stops only its own server child
through the private HTTPS admin endpoint and requires exit 0.

## Isolation and prerequisites

Obtain an explicit peer register grant for a fresh `t4c_tls_0216_*` PostgreSQL
database and confirmed-unused dedicated port before any server or database run.
This tool does not create or delete a database, control PostgreSQL or use the owner
QA port 18765. Use Java 21, `keytool`, the server JAR and client JAR from one
immutable committed-source build. Do not compile against live shared `target`
classes. Copy the server JAR, `lib/`, desktop client JAR and its dependencies, both
collision maps, then compile the probe into the same copied runtime. The runtime
and report directory must be outside the checkout; report directory must not exist
before launch. The probe checks those conditions and a local dedicated DB name.

The copied runtime layout is:

```text
RUNTIME/t4c-server.jar
RUNTIME/lib/*
RUNTIME/client/T4C.jar
RUNTIME/client-lib/*
RUNTIME/probe/com/perso/T4C/server/NativeTlsProbe.class
RUNTIME/assets/maps/worldmap/worldmap.colbin
RUNTIME/assets/maps/dungeon/DungeonMap.colbin
```

With a separately approved database and port, run from the repository root:

```powershell
$runtime = '<fresh immutable TEMP runtime>'
$output = Join-Path $env:TEMP ('t4c-0216-evidence-' + [guid]::NewGuid().ToString('N'))
$dependencies = (Join-Path $runtime 'client/T4C.jar') + ';' +
  (Join-Path $runtime 't4c-server.jar') + ';' +
  (Join-Path $runtime 'lib/*') + ';' + (Join-Path $runtime 'client-lib/*')
javac -proc:none -cp $dependencies -d (Join-Path $runtime 'probe') multiplayer/tools/NativeTlsProbe.java
if ($LASTEXITCODE -ne 0) { throw 'Probe compilation failed' }
$probeClasspath = (Join-Path $runtime 'probe') + ';' + $dependencies
# Set T4C_DB_URL, T4C_DB_USER and T4C_DB_PASSWORD privately for a newly created
# t4c_tls_0216_* database. Never print or commit them.
$javaExe = (Get-Command java -ErrorAction Stop).Source
java -cp $probeClasspath com.perso.T4C.server.NativeTlsProbe $runtime $javaExe 18774 $output
if ($LASTEXITCODE -ne 0) { throw 'TLS probe failed; preserve private output and DB' }
Get-Content -LiteralPath (Join-Path $output 'report.txt')
```

The output retains the private fixture keystore, fixture password, public-only
truststore, empty refusal truststore, admin token and child logs. Treat the entire
directory as private. The sanitized `report.txt` omits account secrets, passwords
and tokens. Account secrets travel only through child standard input in the two
negative probes, not command-line arguments or log output. Preserve failed runs
for diagnosis. This is a local loopback adapter check, not remote deployment or
rendered two-client acceptance.

## Recorded evidence

The final local run passed on 2026-10-27 from exact committed source
`130bc3c0389aa08f801c8c69f8f11abdc32c724d` (protocol 5/schema 10).
An isolated archive built desktop and multiplayer server JARs; the copied
`NativeTlsProbe.java` compiled with `javac -proc:none` against those JARs and
server libraries. SHA-256: probe Java
`1344B1DDFBAEB1182A89546CCBD06997F30DBB5DEABF72E72453F9F83DA40ABE`,
server JAR `D5BBD75EAAC7E19AF976B316BA6FCFBD5321FC0803E604BB29760CB18A9F950C`,
client JAR `EEF5FDD51199487D6F14F4B16686154A82C9168A22E81049AD1E87CE4E8D808B`.

The probe exited 0 on port 18774 with new retained database
`t4c_tls_0216_fe2b78abb682` on existing PG55432. Each negative JVM observed
`SSLHandshakeException`; the native adapter then disconnected without a private
view or account lease. The trusted pair both joined over HTTPS, observed each
other, moved one authoritative actor, observed that move in the second adapter,
read its committed global position and reconnected both identities. The owned
server gracefully exited 0; server stderr was empty and port 18774 was free
afterward. The database schema query returned 10. The sanitized private report
is `%TEMP%/t4c-0216-evidence-19aceb89c0b1400f9fbfb63c8fd5079f/report.txt`,
SHA-256 `CFAC272C3085DFB5542A8FCD9E804E51546418BB3891545BEF03FC965527AACC`.
The entire fixture directory and database are retained for inspection; do not
publish the private control/password files or raw child logs. The isolated build
and run log are under
`%TEMP%/t4c-0216-final-130bc3c0-ef9e7e43828444d9833675f04e654ba9/`.

This is local loopback adapter evidence. It does not verify remote certificate
deployment, a network packet capture, rendered clients or the complete
multiplayer milestone.
