# Two native sessions during hostile HTTP bodies (T4C-0226)

`NativeHttpResilienceProbe.java` exercises two actual `OnlineConnection`
adapters against one privately owned persistent loopback server. It extends
the service-level admission test by checking selected-character readiness,
mutual views, movement, committed records and leases while hostile HTTP
requests are open.

The probe creates two new Warrior accounts and characters at the hosted
Temple starting spawn. Both native adapters must join, acknowledge region
readiness and see each other before the attack phase. It then holds eight
valid Host, Origin and protocol-5 `POST /join` requests with declared
`Content-Length: 101` and no body. Eight more `POST /input` requests
declare `Content-Length: 64` with no body and forged 64-hex bearer tokens. Those
must return 401 before their bodies arrive. While the slow joins remain
open, `/health` must return 200; both native adapters must remain connected,
advance at least three snapshot ticks, and move at least 0.15 world units
using legal cardinal direction intents. Both visible player sets must
contain exactly two actors, and the two real leases must remain active.
The meaningful admission phase is bounded to four seconds, before the
JDK's five-second incomplete-body timeout.

After the attacker sockets close, the probe requires healthy service and
additional movement from both adapters. It checks that their locations
commit and that private identity, gold, experience and progression do not
change. Normal adapter close must release both leases. The private admin
shutdown must exit the exact owned child with code zero, and the dedicated
port must be free. Failure output retains the stage and private server logs;
a forced cleanup fallback is reported separately.

## Isolation

Obtain a new explicit root grant for a create-only
`t4c_http_native_0226_[a-z0-9_]+` database before each run. The URL guard
accepts only that namespace on `127.0.0.1:55432`; an accepted name is not
permission to reuse a retained database. Use the existing local PostgreSQL
instance and the dedicated, unused
`127.0.0.1:18777` port. The probe does not create/drop databases, control
PostgreSQL, change OS trust, or use an owner QA or GUI resource. Its DB
password is supplied through a private environment variable, never a
command argument or report. No account key or admin token is printed.

Build the client and server from the **same immutable commit** in a
create-only private archive outside the checkout. Copy their JARs, server
`lib/` and both collision maps into a create-only private runtime:

```text
RUNTIME/t4c-server.jar
RUNTIME/lib/*
RUNTIME/client/T4C.jar
RUNTIME/probe/com/perso/T4C/server/NativeHttpResilienceProbe.class
RUNTIME/assets/maps/worldmap/worldmap.colbin
RUNTIME/assets/maps/dungeon/DungeonMap.colbin
```

Compile only the new probe against those copied artifacts with Java 21:

```powershell
$dependencies = "$runtime/client/T4C.jar;$runtime/t4c-server.jar;$runtime/lib/*"
javac -proc:none -cp $dependencies -d "$runtime/probe" multiplayer/tools/NativeHttpResilienceProbe.java
$classpath = "$runtime/probe;$dependencies"
java -cp $classpath com.perso.T4C.server.NativeHttpResilienceProbe $runtime $javaExe 18777 $newOutput
```

The output directory must not already exist. It retains a sanitized
`report.txt` plus private control and raw child logs. Keep failed runs
and databases for diagnosis. This is bounded local adapter acceptance;
it does not prove full denial-of-service resistance, rendered gameplay,
remote deployment or the two-hour mixed-stability Beta gate.

## Recorded evidence

The private source archive used immutable
`f6175cc78a6738f73700cfdaf014a6f44ab3219d` (protocol 5, schema 11).
Both client and server packages built successfully in the archive, and the
earlier probe compiled with `javac -proc:none` against the copied JARs and
server libraries. SHA-256 of that earlier tested Java source:
`649590518808A76EAF6BD8E82B6D857F7642BB925BA9ECABC4D23FB762433206`,
server JAR
`FBDE9D36752D5E184A9D3D8D4CD5C56691EE5BF3179CFFC5BA324388D97E0CA0`,
client JAR
`4E6BDF6307894B6DE0B3D7227D9AD300F12F16EBB428A00F8EA47FA34AC37C2E`.

The successful earlier run used probe Java SHA-256
`649590518808A76EAF6BD8E82B6D857F7642BB925BA9ECABC4D23FB762433206`,
whose URL guard named the first two reserved databases explicitly. The
current source generalizes only that guard for future fresh grants; the
behavioral assertions are unchanged. The earlier run used retained fresh database
`t4c_http_native_0226_7c4422d7f1f2` and dedicated port 18777. All
eight forged requests returned 401 before sending any body; `/health`
returned 200 during the held joins. Both actual native adapters stayed
connected, advanced at least three ticks and moved more than 0.15 in legal
cardinal directions within a measured 222 ms phase. Both then moved again
after attacker close; their committed locations advanced while private
progress, gold and experience stayed unchanged. The probe exited 0 after
normal leaves, owned admin shutdown with child exit 0, and port release.
Read-only DB inspection found schema 11, two accounts, two characters and
zero live leases. Sanitized report:
`%TEMP%/t4c-0226-f617-5ced3338f7354c85b73b1253f2f34aac/evidence-second-a8a80c1a59b24ee3a86b6e4ac06e1bcb/report.txt`,
SHA-256
`A55545BDA20A4F68B744EE6964DD312A20A36E087BEAB103DEF842CF7DD2DFE1`.

The **final source** SHA-256 is
`3AEC0090D6C458DAE43AD26C2064DEDCF23033AD67D4B3A3F0801B4FA9E1170F`.
It recompiled against the same copied immutable f617 runtime. Separate
private no-connection invocations rejected an owner database name, a remote
host and the wrong port before creating any output. Its complete probe run
then passed on a third newly granted create-only database
`t4c_http_native_0226_4e5b08fc0817`: eight incomplete joins, eight
forged early 401 responses, health 200, two connected mutual native views,
at least three advancing ticks and over 0.15 cardinal movement for each
within 200 ms, healthy movement after attacker close, committed locations,
unchanged private progress/gold/experience, two then zero live leases,
owned server exit 0 and free port. Read-only inspection found schema 11,
two accounts and two characters. Its sanitized report is
`%TEMP%/t4c-0226-f617-5ced3338f7354c85b73b1253f2f34aac/evidence-final-e3c5ff7513ed497ca8ab39652fc4f409/report.txt`,
SHA-256
`FFD24BFC49993971225E7501CA684D00ADB43BADC54B63649DD82260E54188DF`.
All three databases, reports and private raw logs are retained.

Preserve the first failed run and its separate database:
`t4c_http_native_0226_dc6a1ec76d69` and
`%TEMP%/t4c-0226-f617-5ced3338f7354c85b73b1253f2f34aac/evidence-first-b5f71a687f1241329ca7a71a7cf3ab5b/`.
The first probe incorrectly declared 101 bytes for forged `/input`,
above that route's existing 64-byte declared-body limit, then asserted
401. It failed at that probe assertion before the native liveness phase.
The corrected probe declares 64 bytes for `/input`; the joined
`/join` bodies retain the valid 101-byte declaration. The first failed
run's retained database has schema 11, two accounts, two characters and
zero live leases, and the port was free before the corrected run. Its
failure is a harness error, not evidence of a server admission defect.
Private logs and both databases are retained.
