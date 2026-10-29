# Two native-adapter portal traversal probe

This reproducible integration check uses two actual OnlineConnection instances against a running two-region PostgreSQL QA realm. It creates two new private QA accounts/standard characters in that same database, keeps credentials only in memory, and drives movement through the existing network adapter. It does not launch renderers or touch offline saves.

The player walks authored entrance 16 using collision-based direction inputs. The probe checks stable identity, matching map/private epochs and world isolation while the observer stays in the starting area. It closes/reconnects inside the dungeon, walks return 17, then verifies saved world/epoch and unchanged inventory/stats/gold. Both adapters close in finally blocks. QA rows are retained; this tool does not delete data.

## Run

Build the desktop and multiplayer server, start the persistent two-region QA realm, and privately set T4C_DB_URL/T4C_DB_USER/T4C_DB_PASSWORD to the same QA database as that endpoint. Java 21, javac and the normal Maven-generated classpath are required. From the repository root in PowerShell:

```powershell
$probeClasspath = 'target/classes;multiplayer/server/target/classes;multiplayer/server/target/lib/*;' + (Get-Content target/classpath.txt -Raw).Trim()
javac -proc:none -cp $probeClasspath -d target/native-transfer-probe multiplayer/tools/NativeTransferProbe.java
java -cp ('target/native-transfer-probe;' + $probeClasspath) com.perso.T4C.server.NativeTransferProbe
```

Optional arguments are endpoint origin and world collision file, in that order. Defaults are http://127.0.0.1:18765 and assets/maps/worldmap/worldmap.colbin. Endpoint validation still refuses remote plaintext. The server's sibling dungeon collision file must be present, as documented by HostedRegionPlan. Do not print environment credentials or add account keys to command arguments.

Recorded check (2026-10-27): entrance 16, observer isolation, dungeon reconnect and return 17 passed with actual native networking and PostgreSQL; saved epoch 2/world 0 and character progress were preserved. Root's run completed in about ten seconds on the local QA realm.

This evidence covers the native networking path, not rendered map resources, animation, focus or remote TLS. Rendered two-client world switching remains an acceptance gate. The probe's 90-second per-leg timeout is a failure bound, not an expected duration or performance target.

Movement rule change (T4C-0287): the walker plans from the tile the player is arriving on (the last direction sent),
because the server now finishes a claimed tile (single-player's grid-reservation rule, PR #234). Compile-checked
against the current runtime; not re-run live for this change.
