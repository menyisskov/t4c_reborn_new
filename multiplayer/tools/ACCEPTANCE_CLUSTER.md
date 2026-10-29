# Disposable acceptance PostgreSQL cluster (T4C-0247)

Status: **first live use done 2026-10-28** (third use: paced soak rerun, T4C-0250; fourth and fifth uses 2026-10-29: tamper battery and soak after server movement parity, T4C-0287) (create, extra `t4c_loot_<id>` database, RunEnv, destroy all worked;
see the recorded loot probe run in [NATIVE_LOOT_PROBE.md](NATIVE_LOOT_PROBE.md)). Second use 2026-10-28: a
12-minute native adapter soak smoke, using the cluster's own `t4c_acc_<id>` database directly (the soak has no
naming rule), see [NATIVE_ADAPTER_SOAK.md](NATIVE_ADAPTER_SOAK.md) "Recorded run".

## Purpose

Live acceptance runs (the loot probe, a rendered two-client run, the soak, fault drills) need a
throwaway PostgreSQL that is not the shared one. Docker is unavailable on this machine and the
shared PostgreSQL on 55432 uses a private password that must never be read or touched.
`acceptance-cluster.sh` (run through `acceptance-cluster.ps1`) creates a private cluster from the
already-installed user-owned PostgreSQL 16 runtime in WSL Ubuntu
(`~/.local/share/t4c-online-pg/runtime`, no download, no Docker). It is adapted from the
T4C-0208 recovery script (`db-recovery-cluster.sh`, see
[NATIVE_DATABASE_RECOVERY.md](NATIVE_DATABASE_RECOVERY.md)), which stays untouched.

## Safety rules

- Run IDs match `t4c_acc_[0-9a-f]{12}`. The database name equals the run ID; the role is `t4c_acc`.
- The port is an argument. The script and the wrapper refuse **55432, 55433, 18765, 18766, 18770
  and 18771**, anything outside 1024-65535, and any port already listening (WSL `ss` and
  Windows `Get-NetTCPConnection`). The server listens on `127.0.0.1` only, `max_connections=40`.
- Data lives under `~/.local/share/t4c-online-pg-acceptance/<id>/` (mode 0700). The shared
  `t4c-online-pg` directory, `t4c_online`, `t4c_test` and the 0208 base are never touched.
- The password is generated with `openssl rand`, written only to `<id>/password` (mode 0600), and used
  by `initdb --pwfile`. It is never printed (except by the internal `password` subcommand, captured
  silently by the wrapper), never in argv, never logged, never committed.
- `stop`, `crash`, `start`, `destroy` first verify ownership: directory owner and real path, marker
  equals the ID, `cluster.meta` id and port, and `postmaster.pid` pid, data directory and port
  against `/proc/<pid>/cmdline`. A mismatch is a refusal; no other process is ever signalled.
- `destroy` stops only a verified postmaster, requires the port to be free, checks the path is
  exactly `<base>/<id>` with a real, non-symlink directory, then deletes only that directory.
- Never point a probe at a cluster that someone else's register claim owns, and never reuse an
  ID or port that PEER_WORK shows as claimed.

## Usage

From the repository root in PowerShell (add `-DryRun` to any command to print the plan only):

```powershell
$t = 'multiplayer/tools/acceptance-cluster.ps1'
& $t -Action Create -Port <free-port> -DryRun      # preview
& $t -Action Create -Port <free-port>              # prints CREATE_OK id=t4c_acc_... ; note the id
& $t -Action Status  -RunId t4c_acc_<id>
& $t -Action Crash   -RunId t4c_acc_<id>           # immediate stop (fault drill)
& $t -Action Start   -RunId t4c_acc_<id>           # crash recovery, checks the recovery log
& $t -Action Stop    -RunId t4c_acc_<id>
& $t -Action Destroy -RunId t4c_acc_<id>
```

`-Action RunEnv` reads the password through `wsl.exe` into process-scoped `T4C_DB_URL`
(`jdbc:postgresql://127.0.0.1:<port>/<id>?connectTimeout=5&socketTimeout=10`), `T4C_DB_USER` and
`T4C_DB_PASSWORD` for one child command, then removes them. Nothing is echoed.

### Loot probe

Prerequisites are the operator checklist in [NATIVE_LOOT_PROBE.md](NATIVE_LOOT_PROBE.md): a
claimed PEER_WORK window, a runtime snapshot with `client/T4C.jar`, `t4c-server.jar`, `lib/*` and both
hosted `.colbin` maps, and a dedicated probe server port (not a protected one, and different from the
database port). **Caveat:** the probe refuses any database not named `t4c_loot_<id>`, but this cluster's
database is `t4c_acc_<id>`. Before the first run, either add `t4c_acc_` as an accepted prefix in
`NativeLootProbe.java` (separate, claimed change) or create a `t4c_loot_<id>` database inside the
cluster (createdb from the same WSL runtime with the cluster password file read inside WSL) and use a wrapper
child that rewrites the database name in `T4C_DB_URL` (done on the first run). Do not weaken any other guard. With that sorted:

```powershell
# After building the runtime snapshot exactly as in NATIVE_LOOT_PROBE.md ($runtime, $output, $probeClasspath, $javaExe):
& $t -Action RunEnv -RunId t4c_acc_<id> -Command java `
  -CommandArgs @('-cp', $probeClasspath, 'com.perso.T4C.server.NativeLootProbe', $runtime, $javaExe, '<probe-port>', $output)
```

## Windows Java server to WSL loopback

[PERSISTENCE.md](../PERSISTENCE.md) records that the Windows-side Java server already reaches the
WSL PostgreSQL on 55432 over `127.0.0.1`: WSL2 forwards localhost listeners to Windows. The
cluster listens on `127.0.0.1:<port>` inside WSL, so the same URL works from Windows. If a run
cannot connect, check `wsl.exe -d Ubuntu -e ss -ltn` and that Windows localhost forwarding is enabled
(`.wslconfig` `localhostForwarding`), not the password.

## PostgreSQL 16 vs CI PostgreSQL 18

This runtime is PostgreSQL 16; CI's "PostgreSQL persistence contracts" job uses PostgreSQL 18.
Results from this cluster are local acceptance evidence only. **The CI job stays the authority**
for persistence contracts; a pass here never replaces it, and a divergence is investigated in CI.

## Cleanup

Run `-Action Destroy` when the run is finished and the evidence is saved. After a failed run,
keep the cluster and output for inspection (as the probes require), then destroy it later. If
`create` fails after starting the postmaster, it stops only that verified postmaster and keeps
the directory. Nothing else in the account is modified.

## PEER_WORK request/claim text (paste before the first use)

```text
- <date time zone>: <agent> requests a database/port window for the T4C-0247 acceptance cluster.
  Creates ONE private PostgreSQL 16 cluster t4c_acc_<id> in WSL Ubuntu under
  ~/.local/share/t4c-online-pg-acceptance/<id> on loopback port <port> (not 55432, 55433, 18765,
  18766, 18770, 18771; confirmed unused), plus probe/server ports <ports> and TEMP output
  directories, for <loot probe | rendered run | soak | fault drill>, up to <minutes> minutes.
  No shared PostgreSQL, t4c_online, t4c_test, owner QA, root Maven output or lease touched.
  Releases the ports and destroys the cluster on completion (or records why it was kept),
  and records the result here. Local PG16 evidence only; the CI PG18 job stays the authority.
```
