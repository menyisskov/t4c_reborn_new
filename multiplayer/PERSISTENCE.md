# Online persistence foundation

The server now has a PostgreSQL storage adapter, schema installation command and real-database
CI coverage. **The movement preview does not use it yet.** Its characters are still temporary.
No desktop save, preference, account-storage or character file is read or written by this adapter.

The adapter stores account ownership, a character's name and world position, gold, experience,
and a monotonically increasing revision. Authentication must supply the account ID; a client
claiming an account ID is not authentication. Inventory, quests, equipment, abilities and full
player-state serialization must be added before this is a complete online save format.

Position checkpoints compare the expected revision before writing. A stale save fails instead
of overwriting newer state. Reward grants lock the character row and record an event receipt
in the same transaction as the balance update. Concurrent retries of the same event apply
once; changing its amounts is rejected. Numeric overflow rolls back both balance and receipt.
These guarantees use PostgreSQL's [row locks and transactions](https://www.postgresql.org/docs/current/explicit-locking.html),
with [JDBC prepared statements](https://jdbc.postgresql.org/documentation/query/) for data values.

The simulation must own reward amounts, checkpoint coordinates and stable event IDs. Never
expose these storage calls directly as client commands. A successful database write must precede
acknowledging a durable reward to a player. Retry uncertain outcomes with the same event ID.
Do not call the blocking adapter from the simulation tick thread. A bounded persistence worker,
single-session fencing, checkpoint scheduling and recovery reconciliation are still required.

## Local development database

Use the installed Docker runtime and `docker compose -f multiplayer/compose.yaml up -d`.
Set `T4C_DB_PASSWORD` privately in the process environment or ignored `multiplayer/.env` first.
The database binds only to loopback port 55432 and keeps data in the named `online-postgres`
volume, independently of game saves. Stopping the container preserves the volume. Do not use
`down -v` for routine restarts: that deletes the database volume.

For the server process, set:

- `T4C_DB_URL=jdbc:postgresql://127.0.0.1:55432/t4c_online?connectTimeout=5&socketTimeout=10`
- `T4C_DB_USER=t4c_online`
- `T4C_DB_PASSWORD` to the same private password.

Build with `mvn -f multiplayer/pom.xml verify`, then install/check the schema with
`java -jar multiplayer/server/target/t4c-server.jar --migrate-online`.
Migrations are transactional and serialize concurrent installers. Unsupported schema versions
fail closed. This development container uses its bootstrap database role; production deployment
must separate migration and application roles, keep the database private, and enforce encrypted
remote connections. It is not a production deployment recipe.

## Verification

`OnlineStoreTest` uses a real PostgreSQL database when `T4C_DB_URL` is configured. CI provisions
an isolated PostgreSQL service and requires all three tests. Without the environment variables,
the local headless suite skips this integration class explicitly; a skipped run is not database
verification. The tests create randomly identified records and never truncate existing tables.
Use a dedicated test database because the records remain after the run.

Coverage: store restart/reload and owner isolation; stale checkpoint rejection; concurrent
duplicate rewards and changed-payload rejection; rollback of an overflowing balance update.
Database-process restart, backup/restore and game-session recovery remain acceptance checks
for the integrated persistent server.
