# Multiplayer foundation

This is the first Alpha increment, not a playable multiplayer server. It has no network
listener, accounts, world entities or persistence. The executable only exercises server
startup, fixed-step scheduling and shutdown.

## Single-player compatibility

The root Maven project and `launch.bat` retain the existing offline game. This reactor is
opt-in: it does not modify or read local saves, account storage, preferences or GM seeds.
Build outputs stay below `multiplayer/*/target`, separate from the desktop build.

`game-domain` compiles shared combat and world collision source directly from the desktop
source tree, along with their regression tests. Gameplay formulas are not duplicated.
It has no runtime dependencies: an accidental graphics dependency fails compilation.
This explicit source allowlist is a transitional boundary; expand it only after extracting
and checking another headless rule. Do not add the desktop JAR or LibGDX as a server dependency.

## Build and smoke run

From the repository root with Java 21 and Maven:

```text
mvn -f multiplayer/pom.xml verify
java -jar multiplayer/server/target/t4c-server.jar --ticks 5
```

Keep the adjacent `lib` directory with the server JAR when copying the distribution.
Omit `--ticks` to run until interrupted. `--help` prints usage. The process can run from an
empty working directory, without game assets, a display or a database.

The scheduler starts at 34 Hz independently of desktop preferences. Each executed tick gets
a fixed delta. After a large stall it limits catch-up rather than passing a large time jump
to gameplay. Exceptions propagate and stop the loop; a shutdown request wakes it promptly.
This scheduling policy needs load validation before a production world uses it.

## Next Alpha increments

Static collision and line-of-sight rules now live in `WorldCollision`, used by both the
offline collision adapter and the headless domain. Each instance binds its own stable
terrain sources. The desktop's wall-foot exceptions are preserved. This is not yet a
server world loader or an authoritative movement validator: map bounds must be checked
separately, and occupancy and portal permissions are still handled by the offline adapter.

1. Load server-owned world data and add authoritative movement, keeping offline behavior covered.
2. Separate entity state from animation/audio; run one representative encounter headlessly.
3. Add authenticated commands, two-client replication and server-side action validation.
4. Add separate online persistence with transactional rewards and unique item ownership.
5. Complete the Alpha loop: quest, combat, loot, map transfer, reconnect and restart.

Beta completes launch gameplay for 20 concurrent players. GA requires secure, durable
end-to-end gameplay and tested operations/recovery. Optional tools and expansions follow GA.
