# Online persistence foundation

The server has PostgreSQL storage, account/session ownership, background checkpoints and real-database
CI coverage. The default movement preview remains temporary. **Opt-in persistent mode now saves
native-client movement across reconnects and server restarts.** It is still movement-only Alpha.
No desktop save, preference, account-storage or character file is read or written by this adapter.

The adapter stores account ownership, a character's name and world position, gold, experience,
and a monotonically increasing revision. Operator-issued random access keys authenticate through `acquire`; claiming an account ID
is not authentication. Administrative creation and read APIs must stay inside trusted server code.
Schema version three adds an optional versioned gameplay payload: attributes, vitals, advancement,
inventory/equipment, skills/spells, quest flags, storage, respawn, buffs, cooldowns and companion state.
`checkpointCharacter` saves that immutable payload and location atomically under the writer lease
and revision. Ordinary movement and reward writes preserve it. Unknown versions or invalid references
fail closed; equipment must reference an owned item instance and containers cannot duplicate an ID.
Existing movement-only rows have a null payload. Migration does not invent a class, stats or items.
Authoritative creation, content-registry validation and gameplay consumers must be connected before
this becomes playable character persistence. Cross-character trade and currency/storage transfers
need their own atomic transactions; the payload alone does not implement those operations.

Position checkpoints compare the expected revision before writing. A stale save fails instead
of overwriting newer state. Reward grants lock the character row and record an event receipt
in the same transaction as the balance update. Concurrent retries of the same event apply
once; changing its amounts is rejected. Numeric overflow rolls back both balance and receipt.
These guarantees use PostgreSQL's [row locks and transactions](https://www.postgresql.org/docs/current/explicit-locking.html),
with [JDBC prepared statements](https://jdbc.postgresql.org/documentation/query/) for data values.

The simulation must own reward amounts, checkpoint coordinates and stable event IDs. Never
expose these storage calls directly as client commands. A successful database write must precede
acknowledging a durable reward to a player. Retry uncertain outcomes with the same event ID.
The simulation coordinator sends immutable position checkpoints to a bounded worker pool; the
tick thread never performs database I/O. Each character has at most one checkpoint in flight.
Every gameplay save and reward requires a valid internal session lease; there is no unfenced write
overload. Authoritative combat/quest reward delivery is not connected yet.

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
an isolated PostgreSQL service and requires the integration tests to run without skips. Without the environment variables,
the local headless suite skips this integration class explicitly; a skipped run is not database
verification. The tests create randomly identified records and never truncate existing tables.
Use a dedicated test database because the records remain after the run.

Coverage: store restart/reload and owner isolation; stale checkpoint rejection; concurrent
duplicate rewards and changed-payload rejection; rollback of an overflowing balance update.
Additional coverage checks concurrent login exclusion, expired-writer fencing, credential
rotation and version-one schema upgrade with existing reward receipts intact.
Database-process restart, backup/restore and game-session recovery remain acceptance checks
for the integrated persistent server.

## Account ownership and writer leases

Closed-Alpha provisioning creates a 256-bit random access key and returns it once to trusted
operator code. PostgreSQL stores its SHA-256 verifier. This follows the distinction between
random bearer credentials and user passwords in the [OWASP session guidance](https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html).
Do not log the raw key, put it in a URL, or send it over an unencrypted remote connection.
Record string representations redact credentials and lease identifiers, but explicit serialization
still requires care. There is not yet a key-distribution UI or public registration endpoint.

`acquire(key, characterId)` checks account ownership and creates one database-timed writer
lease. Concurrent acquisition fails while the lease is active. The server must renew before
expiry and stop authoritative actions if renewal fails. A replacement receives a new lease ID;
the old writer cannot save, award rewards, renew or release the replacement's lease. On a clean
logout, persist the final checkpoint before releasing. After a crash, wait for lease expiry before
rejoining; load the database state rather than replaying an old in-memory snapshot.

Key rotation locks the account before its characters, changes the verifier, and clears all its
leases in one transaction. Session acquisition uses the same lock order to serialize with rotation.
Already-committed rewards remain recorded; retry them under the new lease with the same event ID.
Version-two migration preserves existing characters and receipts. Legacy account IDs receive
no usable credentials automatically; an operator must explicitly provision a replacement key.

The native client now uses a private profile to reacquire its character and load the last committed
position. Full gameplay state, public account flows and remote encrypted connections remain work.

## Running persistent native clients

Use PowerShell 7.4 or newer for the launch scripts. Configure the database environment above,
then provision each test player once (create `multiplayer/local` first):

```powershell
java -jar multiplayer/server/target/t4c-server.jar --provision-online assets/maps/worldmap/worldmap.colbin multiplayer/local/player-one.json "Player One"
.\multiplayer\start-preview.ps1 -Persistent
.\multiplayer\start-client.ps1 -Profile multiplayer/local/player-one.json
```

Provision a different profile for the second player; one character cannot connect twice. Profiles
are ignored by Git and restricted to their filesystem owner before a credential is written. The
provisioning command never overwrites an existing profile or prints its secret. Do not upload
these files, paste them into chat, or use them as command-line credential values. The launcher
passes only the profile path to the client through its environment.

The server uses world-space tile coordinates in PostgreSQL and validates the loaded position
against the current starting-area collision map. Unsupported maps or blocked saved positions
are rejected rather than silently teleporting or overwriting progress. A character's provisional
appearance is stable across reconnects; full appearance selection remains future work.

Movement checkpoints are periodic. Clean logout waits for a final checkpoint and lease release.
`stop-preview.ps1` authenticates through a separate, owner-only server control token and requests
a bounded graceful flush. Player tokens cannot shut down the server. `stop-preview.ps1 -Force`
is reserved for intentional crash testing: uncommitted movement may be lost, and reconnection
must wait for the previous database lease to expire. Startup never imports local single-player saves.

On storage failure, the actor disconnects and the server fails closed. The coordinator also stops
the actor before its last confirmed lease can expire, even if storage workers are still waiting.
Recovery loads committed database state; it never invents a successful save. Server logs report
persistence failure without logging credentials. Reconnect with R after the database/server recovers;
if a lease remains active after a crash, retry once it has expired.

Local QA on 2026-10-26 used a user-owned PostgreSQL 16.15 runtime inside the existing Ubuntu WSL
installation because Docker Desktop could not start. Data and private database credentials live
under that user's `.local/share/t4c-online-pg`, outside desktop game saves. The Windows Java server
connects over loopback port 55432. This local test runtime is separate from the checked-in Docker
recipe and CI's PostgreSQL 18 service; do not assume it exists on another developer's machine.

## Backup and recovery check

Use PostgreSQL client tools matching the server major version, with `PGHOST`, `PGPORT`, `PGUSER`
and private password-file/environment authentication already configured. Keep backup files outside
the repository in an owner-only directory. A custom-format archive supports a consistent database
snapshot; cluster roles need separate provisioning. See PostgreSQL's
[pg_dump documentation](https://www.postgresql.org/docs/16/app-pgdump.html).

```sh
pg_dump -Fc -d t4c_online -f /private/backups/online.dump
createdb t4c_restore_check
pg_restore --exit-on-error --no-owner --no-privileges -d t4c_restore_check /private/backups/online.dump
```

Choose a fresh destination name on each run. Do not use `--clean` or `--create` for this drill:
the destination must be the explicitly created test database. The latter flag can restore into
the database name recorded in the archive. See
[pg_restore documentation](https://www.postgresql.org/docs/16/app-pgrestore.html).
Use an isolated server process/port with `T4C_DB_URL` pointing to that destination. Confirm table
contents, authenticate a test character, move, log out and reconnect. Never run this drill against
the source database. Existing copied leases may require expiry before login.

Local evidence on 2026-10-26: all four restored tables matched the source, followed by successful
authenticated movement/logout/rejoin in the restored database. Separately, forced server termination
rejected immediate takeover and recovered the last committed position after lease expiry. Local
reports remain under ignored `server/target`; these are movement-slice recovery checks, not the
full-game recovery gate. Production retention, off-machine backup storage, scheduled verification,
reward/transfer crash recovery and database-outage testing remain outstanding.
