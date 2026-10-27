# Native account roster probe

This opens no windows. It provisions two fresh online accounts and three standard characters,
uses real `OnlineAccountClient` instances to verify owner-only discovery, joins selected characters
through `OnlineConnection`, checks the active-lease summary and switches to the other owned
character after ordinary logout. Saved progress must remain unchanged. QA records are retained;
credentials remain in memory and are never printed. No single-player files are touched.

Build matching desktop/server packages, start an isolated persistent QA server, and privately set
`T4C_DB_URL`, `T4C_DB_USER`, `T4C_DB_PASSWORD` for that same database. From the repository root:

```powershell
$probeClasspath='target/classes;multiplayer/server/target/classes;multiplayer/server/target/lib/*;'+(Get-Content target/classpath.txt -Raw).Trim()
javac -proc:none -cp $probeClasspath -d target/native-roster-probe multiplayer/tools/NativeRosterProbe.java
java -cp ('target/native-roster-probe;'+$probeClasspath) com.perso.T4C.server.NativeRosterProbe
```

The optional argument is the endpoint (default loopback18765); it must match the database.
This checks the actual native networking integration. It does not verify a rendered selector,
character creation, public registration, remote key delivery/recovery or full account lifecycle.
