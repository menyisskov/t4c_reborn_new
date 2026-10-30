# Consistent realm backup and restore verification

`tools/verify_backup.py` creates a PostgreSQL custom archive and restores it into a
new database, then compares every `public.online_*` table's definition, row count,
and SHA-256 of a primary-key-ordered COPY stream. It discovers tables dynamically,
including new combat/corpse tables; it does not maintain a stale table allow-list.
Table names, columns, defaults, primary keys and non-CHECK constraints must match
exactly. If only CHECK definitions differ, the verifier reparses each differing
source and restored CHECK on separate empty temporary clones in the **new restore
database** and compares PostgreSQL's resulting definitions. The clones are made in
a rolled-back transaction; no source database DDL or persistent restored-table DDL
is performed. An unsupported CHECK, weakened or missing rule still fails. The JSON
report records whether exact comparison or PostgreSQL CHECK parsing was needed.

This verifies stored data recovery, **not a complete Beta recovery playthrough**.
The resulting report explicitly leaves `native_playthrough_verified` false.

## Requirements and authentication

Run Python 3.11+ inside Linux, macOS or WSL, with PostgreSQL `psql`, `pg_dump`,
`pg_restore`, and `createdb` available. No Python packages are required. Use client
tools compatible with the source PostgreSQL version. The operator role needs read
access to the whole realm and permission to create a test database. Do not grant
database creation to the game server solely to run this drill.

Set connection details through standard PostgreSQL environment variables:
`PGHOST`, `PGPORT`, `PGUSER`, and a private `PGPASSFILE` (preferred) or `PGPASSWORD`.
Remote connections should configure `PGSSLMODE=verify-full` and `PGSSLROOTCERT`.
Never put a password in a database name, command-line URI, shell history or report.
The tool suppresses subprocess stderr because database errors can contain data or
connection details. Correct credentials/access outside the tool if a connection
fails; do not add raw error logging.

## Run a drill

Choose a fresh destination name and a new directory outside the repository:

```sh
python3 multiplayer/tools/verify_backup.py \
  --source t4c_online \
  --restore t4c_restore_20261027_qa \
  --output-dir "$HOME/private-backups/t4c_restore_20261027_qa"
```

If PostgreSQL tools are not on PATH, add `--pg-bin /path/to/postgresql/bin`.
The local WSL developer runtime currently keeps them under
`$HOME/.local/share/t4c-online-pg/runtime/usr/lib/postgresql/16/bin`; its private
library directory may also require `LD_LIBRARY_PATH` from the existing runtime.
This path is a developer installation detail, not required by the tool.

The output directory is created with owner-only POSIX access. The archive contains
account verifiers, character state, session identifiers and reward receipts. Treat
it as private even though the JSON verification report contains only counts/hashes.
Keep archives out of Git and chat. Apply production retention/encryption/off-machine
storage policies separately.

The tool never drops a database, invokes `--clean`, or reuses a restore database.
`createdb` itself must succeed; an existing destination fails before dumping or
restoring anything, including if another operator wins a concurrent creation.
The restore uses one transaction with `--exit-on-error`, `--no-owner` and
`--no-privileges`, without `--create`, so it cannot restore into the source name
recorded in the archive. Failed runs retain their newly created database and output
directory for inspection. Choose another fresh name for another attempt.

## Why the comparison is consistent

A read-only repeatable-read transaction exports a snapshot and stays open through
the dump and source hashing. `pg_dump --snapshot` and all source readers import that
same snapshot. Concurrent commits after it therefore do not produce false failures
or silently change the baseline. Source writes are not paused. Concurrent DDL or
lock delays may make the drill fail; do not run schema migrations during a drill.

Restored reads use another exported snapshot. COPY text formatting is normalized
to UTC, ISO dates and fixed float/bytea settings. Each table is ordered by its
primary key's normalized text using C collation, and bytes are hashed incrementally without retaining rows in memory.
COPY escapes embedded newlines, allowing streaming row counts. A missing primary
key or mismatched schema/data fails verification. Subprocesses have a bounded
deadline (`--timeout`, default 600 seconds each); connection/lock timeouts are shorter.

PostgreSQL documentation: [exported snapshots](https://www.postgresql.org/docs/16/functions-admin.html#FUNCTIONS-SNAPSHOT-SYNCHRONIZATION),
[pg_dump](https://www.postgresql.org/docs/16/app-pgdump.html),
[pg_restore](https://www.postgresql.org/docs/16/app-pgrestore.html), and
[COPY](https://www.postgresql.org/docs/16/sql-copy.html).

## Finish the recovery check

Retain the restored database. Start an isolated server process on a different port,
pointing only at that database. Allow copied writer leases to expire before login.
Use separate private QA profiles to authenticate, fight, acquire rewards, travel,
log out and reconnect in two native clients. Check both players' progress. Record
this evidence separately; matching hashes alone do not establish a playable restore.
No single-player saves or preferences are read or modified by the backup tool.

T4C-0345 scripts this finish for twenty native adapters on a disposable cluster:
[tools/run-native-restore-check.ps1](tools/NATIVE_RESTORE_CHECK.md) populates a database by real play, runs this verifier
into a separate `t4c_restore_*` database, compares a gameplay-level summary of both, starts a server on the restore only
and makes the same adapters play, stop gracefully, restart and reconnect there, then judges the restore against everything
the source once showed as committed. It is native-adapter evidence (not rendered, not remote); a hand-run rendered
playthrough on a restore is still the stronger check.

Focused tooling tests:

```sh
python3 -m unittest discover -s multiplayer/tools -p 'test_verify_backup.py'
```

To exercise the parser regression against a private PostgreSQL fixture that contains
the source/restored `varchar[]` CHECK rendering difference, set
`T4C_BACKUP_TEST_SOURCE`, `T4C_BACKUP_TEST_RESTORE` and optionally
`T4C_BACKUP_TEST_PG_BIN`, along with the normal private PostgreSQL connection
environment. That test accepts the equivalent rendered CHECK and rejects a weakened
or missing one. It is skipped when no private fixture is configured.
