# Isolated PostgreSQL crash and native adapter recovery (T4C-0208)

This drill starts a new PostgreSQL 16 cluster and a new online server from a copied,
matching runtime. Two fresh accounts enter through `OnlineConnection` adapters. Player A
commits the authored opening letter; player B has no letter. The probe obtains a private
B session after the initial adapter closes, asks the cluster helper to perform an
**immediate stop of that owned PostgreSQL process**, then sends one bounded B interaction
while the database is proven down. It never replays the request. After recovery, it
reads the actual committed records, waits for genuine lease availability, reconnects
both native adapters, and gives B a letter only through a new explicit interaction.

This is a database-process recovery drill. A Java HTTP timeout or simulated SQL error
does not satisfy it. The cluster helper verifies the ownership marker, UID, real data
path, postmaster PID, command line and port before any stop. After restart it requires
a changed PID and start timestamp plus PostgreSQL's own crash-recovery log evidence.

## Isolation and prerequisites

Use Windows x64, Java 21, PowerShell, WSL Ubuntu, and the existing PostgreSQL 16 runtime
at `/home/menyiss/.local/share/t4c-online-pg/runtime/usr/lib/postgresql/16/bin`.
That binary runtime is read only. The helper creates a **new** cluster under
`/home/menyiss/.local/share/t4c-online-pg-0208/t4c_db_0208_<random>` with mode 0700,
private SCRAM password file, loopback `127.0.0.1:55433`, private socket directory and
one database of the same name. It refuses an existing run directory and never removes
data or logs. Its only process stop uses `pg_ctl` on the checked owned data directory.

The Windows runner requires a preserved matching runtime outside the checkout with
`t4c-server.jar`, server `lib/`, client `client/T4C.jar`, client `client-lib/`, and
the starting world and dungeon collision maps in their normal relative paths. It
copies these to a new `%TEMP%/t4c-0208-runtime-*` directory and compiles the probe
there. The caller supplies the exact source revision, schema number and expected
server/client JAR SHA-256 values. The runner verifies the JAR bytes before and after
the copy. Those metadata arguments describe the selected binaries; they do not prove
the source revision produced them. The report records the actual JAR hashes.

Example for the preserved 0207 runtime from source `8da43482` (protocol 4, schema 9):

```powershell
./multiplayer/tools/run-native-database-recovery.ps1 `
  -RuntimeSource "$env:TEMP/t4c-0207-runtime-20261027_114527-b52ce648" `
  -SourceRevision 8da43482ee5913cb7bf63273e49edce2cc2e38a2 `
  -SchemaVersion 9 `
  -ExpectedServerSha256 38D6D00DAFCD4FB81D08476667C435BC6485149EA373FD3FAB8599DC8D903D4A `
  -ExpectedClientSha256 BC09B3328EDAB9EB0D34005E07E9FF325CDB8B6CC62AE57CE1247770AD3A2D1F
```

The runner checks Windows ports 55433 and 18768 before creation. The Ubuntu helper
also checks the database port before startup. Do not run against the existing
PostgreSQL 55432 cluster, `t4c_online`, `t4c_test`, QA 18765, the owner's live
clients, or repository Maven/target outputs. The default WSL distro on this machine
is not Ubuntu; the runner names Ubuntu explicitly. Do not manually run a generic
PostgreSQL service or process-kill command for this drill.

## Evidence and recovery

The probe creates a private `%TEMP%/t4c-0208-evidence-*` directory with a sanitized
`report.txt`, child logs, and a private admin control token. The WSL run directory
retains `postgres.log`, the crash marker, initialization output, private password
file and database files. Keep both directories private. Never attach token/password
files, account profiles or unsanitized logs to a PR. On failure the runner and probe
attempt to stop only the verified owned processes; all data and logs remain for
inspection. If the runner itself is interrupted, use the helper's `status` and
`stop` with the exact `t4c_db_0208_<random>` ID only after checking its marker and
directory. Do not delete or reset a cluster to convert a failed run into a pass.

A pass compares exact account/character identities, letter UUID, flag, full progress,
position, gold, experience and nonregressing revisions. It proves that player B's
unacknowledged request did not appear after database restart; then it checks that a
fresh explicit post-recovery request commits one B letter. Graceful server exit code
zero is required only after the database is back. The private report records the
outage response category, old/new PostgreSQL PID and start time, schema/protocol
facts and hashes, without credentials.

## Recorded isolated result

The exact final Java source SHA-256
`94099C3B19382BD126F5B51AFD60CFFB4DEB3034F7B8A93B681AA9E22974CDB3`
passed at 12:26 Asia/Jerusalem on 2026-10-27, using preserved source
`8da43482ee5913cb7bf63273e49edce2cc2e38a2`, protocol 4, schema 9, server JAR
SHA-256 `38D6D00DAFCD4FB81D08476667C435BC6485149EA373FD3FAB8599DC8D903D4A`
and client JAR SHA-256
`BC09B3328EDAB9EB0D34005E07E9FF325CDB8B6CC62AE57CE1247770AD3A2D1F`.
The dedicated cluster was `t4c_db_0208_dacdb459af35`; its old postmaster PID/start
were `152203`/`1793096736` and recovered PID/start were `152360`/`1793096739`.
The probe queried `online_schema` and verified version 9 before the crash and again
after recovery.
The retained PostgreSQL log records an interrupted database and automatic recovery.
The one B outage request returned `503 busy`; it was a bounded raw protocol request
from a preflight-verified private session, **not** an `OnlineConnection` call. Two
native adapters supplied the pre-outage and post-recovery state checks. A kept its
original letter UUID and full progress hash; B stayed letter-free through recovery
and lease expiry, then received a distinct letter only after explicit native
interaction. Final server shutdown exited 0. Windows ports 55433/18768 had no
listeners afterward, and the helper reported the owned cluster `DOWN`.

The sanitized private report is
`%TEMP%/t4c-0208-evidence-t4c_db_0208_dacdb459af35/report.txt`.
The matching cluster data and logs remain in the new private WSL directory. Three
earlier preflight attempts and one post-recovery native-view attempt were preserved
as failed evidence. The failures exposed legitimate checkpoint revision changes
after closing an adapter or waiting for lease expiry; the probe now reads the actual
committed baseline at those points. A preliminary full pass with the earlier Java
source was also retained; the final pass includes an actual schema query and a
bounded post-restart JDBC readiness check. One additional full rerun after the
cluster helper change failed that fast JDBC check before the readiness bound was
added; its own processes stopped and its data/logs remain. Every run
stopped its owned processes, and none reached a shared resource. The final Java
source and assertions were not changed after the passing run.

The helper's partial-create failure fixture used a fresh owned cluster
`t4c_db_0208_8b8743383dc0`. An explicit nonsecret test flag caused exit 77
immediately after its postmaster started. The scoped trap reported
`CREATE_FAILURE_OWNED_STOP_OK`; the helper then reported `DOWN`, the data file
`PG_VERSION` remained, and ports 55433/18768 had no listeners. The trap is eligible
only after its own `mkdir` and marker write; a pre-existing run directory is
refused before the trap is installed. The helper source SHA-256 for this run was
`8CB456589B775A10E0E3975B999E4EC2685559717D67CD8A8266A542638EEDC7`.

This initial 8da/schema-9 pass is evidence only for those binaries.
A later current-source/schema-10 run needs a new immutable matching runtime and its
own recorded evidence. Neither run is a rendered-client, clean-machine, remote,
backup-restore, general quest/loot replay or sustained-load acceptance test. M5 and
M6 remain open until their separate milestone criteria pass.
