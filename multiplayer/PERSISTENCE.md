# Online persistence foundation

The server now has a PostgreSQL storage adapter, schema installation command and real-database
CI coverage. **The movement preview does not use it yet.** Its characters are still temporary.
No desktop save, preference, account-storage or character file is read or written by this adapter.

The adapter stores account ownership, a character's name and world position, gold, experience,
and a monotonically increasing revision. Operator-issued random access keys authenticate through `acquire`; claiming an account ID
is not authentication. Administrative creation and read APIs must stay inside trusted server code. Inventory, quests, equipment, abilities and full
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
checkpoint scheduling and recovery reconciliation are still required. Every gameplay save and
reward now requires a valid internal session lease; there is no unfenced write overload.

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

These are database-level guarantees, not yet a claim that the native client reconnects into a
persistent character. The simulation-to-storage worker and native credential path are next.
