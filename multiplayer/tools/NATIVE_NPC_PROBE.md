# Native NPC interaction probe

This runs two real `OnlineConnection` adapters without opening windows. It provisions fresh
online-only accounts in the configured QA database, positions one beside authored Kilhiam,
checks public NPC replication and the first letter/repeat greeting, verifies observer isolation
and unchanged unrelated progress, then reconnects and verifies the same single letter identity.
QA rows remain for diagnosis. No credentials or personal saves are read or printed.

Build matching desktop/server packages and start an isolated persistent QA server first.
Set private `T4C_DB_URL`, `T4C_DB_USER`, `T4C_DB_PASSWORD` for that same QA database.
From the repository root:

```powershell
$probeClasspath='target/classes;multiplayer/server/target/classes;multiplayer/server/target/lib/*;'+(Get-Content target/classpath.txt -Raw).Trim()
javac -proc:none -cp $probeClasspath -d target/native-npc-probe multiplayer/tools/NativeNpcProbe.java
java -cp ('target/native-npc-probe;'+$probeClasspath) com.perso.T4C.server.NativeNpcProbe
```

The optional argument is the endpoint (default loopback port 18765); it must match the database.
Package compilation is required because the server-side provisioner is package-private and Java
source-file launch uses a different class loader. This is native networking evidence, not rendered
NPC art/dialogue, full quest-chain completion or a Beta acceptance result.
