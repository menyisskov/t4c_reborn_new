# Multiplayer Alpha work

For optional saved native characters, follow [persistent-mode setup](PERSISTENCE.md#running-persistent-native-clients).

Release evidence and remaining gates are tracked in [ACCEPTANCE.md](ACCEPTANCE.md).
The PostgreSQL groundwork and its current integration limits are in [PERSISTENCE.md](PERSISTENCE.md).

## Native game client QA

The next test uses the existing desktop game's map renderer, scenery and layered character
animations. Start the local server as below, then run this twice from the repository root:

```powershell
.\multiplayer\start-client.ps1
```

This opens a separate **T4C Reborn - Online Alpha** window for each player. Use WASD or
arrows to move; the other window should show that character moving. Switching focus stops
input from the previous window. Close one window to disconnect. If the server is restarted,
press R to reconnect. Escape closes only that online client. The normal `launch.bat` still
starts single-player.

This is a native movement slice, not complete Alpha. In the default preview, characters are temporary and
use default appearances (male/female alternating by temporary ID). No saved character is
imported. The native entry point shares the actual game renderer and animations, but does
not initialize the offline gameplay screen, combat, inventory, quests or saving. It inherits
the server's conservative movement and cropped test area. There is no prediction yet.
The animation renderer may read the existing timing preference; it does not write preferences.
Rebuild the desktop with `mvn -DskipTests package` after source changes before launching.
Client logs are separate `target/online-client-*.log` files, in addition to normal renderer logs.

Native QA: join with two windows; move in each; verify scenery occlusion, facing and walking;
test walls; switch focus during movement; close/reopen one client; stop/restart the server
and reconnect with R. Confirm single-player still starts with its own saved characters.

## Browser diagnostic client

The browser diagnostic surface is a **local movement preview**, not the complete Alpha.
It serves a browser client, temporary sessions, shared player positions and server-owned
movement on the real game's starting-area collision map. It has no combat, quests,
inventory, portals, account login or persistence yet.

## Run the preview on Windows

From the repository root with Java 21 and Maven:

```powershell
.\multiplayer\start-preview.ps1 -Build
```

Open <http://127.0.0.1:18765/> in two browser windows and click **Join preview** in each.
Move with WASD or the arrow keys. **Open second client** opens another client.
If the port is occupied, pass `-Port 18766` and use the printed URL. The script does not
replace other listeners. Omit `-Build` to reuse the already-built server.

```powershell
.\multiplayer\stop-preview.ps1
.\multiplayer\start-preview.ps1
```

The server runs hidden until stopped. Its process identity, startup log and error log
are under `multiplayer/server/target/`: `preview-session.json`, `preview.log` and
`preview-error.log`. Stop checks creation time and command line so a reused PID cannot
stop an unrelated process. Stopping discards all preview state. After restart, join again;
a previous token cannot restore a discarded character.

For a foreground run on any platform, from the repository root:

```text
mvn -f multiplayer/pom.xml verify
java -Djava.awt.headless=true -jar multiplayer/server/target/t4c-server.jar --preview assets/maps/worldmap/worldmap.colbin 18765
```

Stop with Ctrl+C. Keep the adjacent `lib/` directory with the JAR.
`--ticks 5` still runs the asset-free lifecycle check without a network listener.

## Product QA checklist

1. Join in two windows. Each shows two connected players. Move one away from the spawn
   point so both markers are visible; your player is gold, other players are blue.
2. Move with WASD and arrow keys. Both windows should show the same movement. A small
   visible delay is expected: this first client displays server snapshots without prediction.
3. Walk into grey cells and the map edge. You should stop rather than cross them.
4. Switch windows while holding a direction. The first player should stop. Releasing a
   key stops movement; if input delivery stops, the server stops it independently.
5. Disconnect one client. It disappears from the other. Join again for a new temporary
   character. Closing a client removes it when possible, otherwise after session expiry.
6. Stop and restart the server. Open clients report connection loss and can join again.
7. Launch single-player as usual; its launcher and existing saves are unchanged.

Report the action, expected behavior, actual behavior, and whether it happened in one or
both windows. The screen's server tick and player ID help correlate reports.

The grey grid is a diagnostic collision view, **not replacement game art or approved UI**.
Its cropped area is centered on the configured new-character location. If that tile
requires an offline portal exception, the server picks a nearby clear tile. Player speed,
grid scale and starting-area coordinates use existing game constants. Movement is
conservative: no sliding, portal overrides, dynamic actor collision, or client prediction
yet. Players may overlap. Online movement parity remains future work.

## Authority, isolation and limits

- One simulation thread owns every player. HTTP workers submit bounded commands and
  receive immutable snapshots; clients never supply authoritative positions or time deltas.
- Fixed server time and the existing speed constant control movement. Extra input packets
  replace intent, never add movement budget. Swept substeps, corner and bounds checks
  prevent skipping blocked cells. The scheduler currently runs at 34 Hz.
- Random 256-bit bearer tokens identify temporary sessions. Snapshots expose public player
  IDs, never tokens. Input sequence numbers reject replay/reordering. These are local
  sessions, **not user accounts or production authentication**.
- At most 20 sessions, 128 queued simulation commands, eight HTTP workers, bounded HTTP
  queues/connections/headers/body sizes and request timeouts. Input expires after silence;
  idle sessions expire after about 30 seconds at the target tick rate. All preview positions
  are intentionally replicated to all local clients; production interest/privacy filtering
  has not been implemented.
- The listener binds only `127.0.0.1`; Host and mutation Origin checks reject foreign
  websites and DNS rebinding. No CORS is enabled. Do not proxy or expose this preview
  publicly. Remote play requires authenticated TLS transport and abuse controls.
- HTTP polling is a replaceable diagnostic transport. Java's built-in
  [HTTP server API](https://docs.oracle.com/en/java/javase/21/docs/api/jdk.httpserver/com/sun/net/httpserver/HttpServer.html)
  supplies the listener; its [documented limits](https://docs.oracle.com/en/java/javase/21/docs/api/jdk.httpserver/module-summary.html)
  are configured at startup. This is not a production MMORPG transport commitment.
- The root build and `launch.bat` remain the offline game. No preview code reads or writes
  saves, storage, preferences or GM seeds. Outputs stay in this reactor. Collision codes
  and wall-foot rules compile from the same pure sources in both builds; editor colors
  and LibGDX stay outside the domain dependency boundary.

## Validation and remaining Alpha work

`mvn -f multiplayer/pom.xml verify` covers shared combat/collision, loading the real map,
invalid map rejection, movement authority, stale input, session expiry/capacity, hostile
HTTP requests, 20 simultaneous clients receiving state, disconnect/rejoin and port reuse.
The 20-client check is a short integration test, **not a production capacity/soak benchmark**.
The desktop suite covers unchanged collision behavior through its adapter.

The native desktop client now renders server-owned player movement, and the owner has
verified facing and two connected players in the actual game windows.

Remaining Alpha work: extract full player movement and entity state from rendering;
implement combat/quest/loot and map transfer; introduce real
authentication and remote encrypted transport; persist online characters and transactional
rewards separately from offline files; verify reconnect, restart and recovery.
Beta completes launch gameplay and validates sustained play with 20 users. GA requires
essential end-to-end gameplay, security and operations. Optional tooling and content follow GA.
