# Restored-database gameplay and reconnect check (T4C-0345, M5)

`run-native-restore-check.ps1` finishes the recovery drill that [BACKUP.md](../BACKUP.md) leaves open: it takes a backup of
a populated disposable database, restores it into a **separate database of the same disposable cluster**, starts a game
server on the restore only, and makes the same twenty native `OnlineConnection` adapters play and reconnect there.
Matching table hashes are checked (`verify_backup.py`) but are not the pass criterion; the pass criterion is that the
restored realm is playable and nothing the source had committed is lost, changed or granted twice.

It is native-adapter evidence on local PostgreSQL 16 (WSL), not rendered-client evidence, not a remote or TLS run and not
the CI PostgreSQL 18 authority. It never touches the shared `t4c_online`/`t4c_test` databases or any QA process.

## Safety

- Creates ONE new acceptance cluster (`acceptance-cluster.ps1`, id `t4c_acc_<12 hex>`, WSL Ubuntu PostgreSQL 16, loopback
  `-DbPort`) and starts only its own `ServerMain --online` children on `-ServerPort` (game socket on `-ServerPort + 1`).
  Refuses 55432, 55433, 18765, 18766, 18770, 18771 and any port that is already listening.
- The password stays in `acceptance-cluster.ps1 -Action RunEnv` (process-scoped environment) and in
  `restore-check-backup.sh`, which writes a mode-0600 `PGPASSFILE` inside the cluster directory for the duration of the
  dump/restore and deletes it on exit. It is never printed, never in an argument, never logged, never committed.
- The archive (it holds account verifiers) is written inside the cluster directory and disappears with the cluster. The
  evidence directory (`%TEMP%\t4c-0345-restore-<cluster id>`) holds only counts, hashes, logs and the soak reports; the
  profile JSON files (account keys) and control tokens are deleted when the run passes.
- The Java helper `NativeRestoreCheck` is read-only and refuses any database that is not a loopback
  `t4c_acc_<12 hex>` or `t4c_restore_<12 hex>`. `NativeFaultDrill`'s own guard (only `t4c_acc_*`) is unchanged.
- The restore database is created by `verify_backup.py` (it refuses an existing name and never drops a database). The
  cluster is destroyed after a complete run unless `-KeepCluster` is given; on any failure it is kept.

## What a run does

1. **Population** by real native play: `NativeLootFaultDrill` (18 hard-killed claim and corpse-recovery rounds with lease
   expiry and retries), then twenty characters provisioned beside the opening NPC, greeted (opening letter, quest flag,
   reward receipts) and played by a paced 120 s `NativeAdapterSoak` (combat, rewards, four portal walkers). The source
   server is **hard-killed 15 s before the soak ends**, so the dump contains twenty live copied writer leases.
2. **Source summary** (`NativeRestoreCheck summary`): characters, owned items, quest flags, gold and experience totals,
   reward/combat/transfer/loot-claim rows, ground-loot states, corpses (recovered and not) and one SHA-256 over every
   character's committed state. The committed-state verifier of the fault drills (`verify`) must pass on the source.
3. **Backup and restore** (`restore-check-backup.sh` running `verify_backup.py`): one exported snapshot, `pg_dump`
   custom archive, restore into a new `t4c_restore_<12 hex>` database in one transaction, definition/count/hash
   comparison of every `online_*` table.
4. **The restore must equal the source**: `summary` of the restore is compared with the source summary (identical on all
   compared measures) and `verify` passes on the restore before any server touches it.
5. **Native gameplay and reconnect on the restore only**: a server started on the restore; the same adapters (same
   account keys, so the accounts themselves were restored) play for 90 s; the server is stopped through the private
   `/admin/shutdown` (must exit 0); a new server is started on the same restored database (intentional restart); the
   adapters reconnect and play 45 s more; second graceful stop (must exit 0).
6. **Final judgement** of the restored database by `verify` against what the **source** sampler once saw committed and
   against what the **restore** sampler saw: no committed revision lowered, no changed state at an observed revision, every
   receipt/claim/event/drop/corpse id ever observed still present, ground-loot states never regress, every claimed drop has
   exactly one claim receipt, no experience beyond paid receipts, exactly one opening letter per character, no item id held
   twice. The restore must also have committed new play (more combat events than the source had).

A soak error other than the outage class (`adapter_stopped_*`, `connection_or_reconnect_timeout`,
`gameplay_result_unavailable`, `snapshot_stalled`) fails the run.

## Run

Build first (`mvn -q -DskipTests package` at the root and in `multiplayer/`, plus
`mvn -q dependency:build-classpath -Dmdep.outputFile=target/classpath.txt`). Claim the ports in `PEER_WORK.md` first.

```powershell
$env:T4C_SOAK_PORTAL_WALKERS='4'; $env:T4C_SOAK_PORTAL_INTERVAL_SECONDS='30'; $env:T4C_SOAK_PORTAL_DWELL_SECONDS='10'
./multiplayer/tools/run-native-restore-check.ps1 -DbPort <free> -ServerPort <free>   # about 8 minutes
```

## Recorded run (2026-10-30, local PG16 WSL, protocol 8, schema 13, `origin/main` 3e147d8b plus this branch)

Disposable cluster `t4c_acc_5a640327dd94`, restore database `t4c_restore_9de5c42da630`, destroyed afterwards
(`DESTROY_OK`); the script exited 0 and every `RESULT` line below was PASS.

| Step | Result |
| --- | --- |
| Loot/corpse population | `NativeLootFaultDrill` PASS (9 claim and 9 recover rounds, each applied exactly once after the retry); final graceful stop exit 0 |
| Source after the soak and the kill | 38 characters, 616 owned item stacks counted by quantity, 20 characters with quest flags (the opening letter flag), 78 reward receipts, 77 combat events, 9 loot claims (all 9 drops `CLAIMED`), 10 corpses (9 recovered, 1 not), 20 live leases copied into the dump; `verify` on the source PASS |
| Backup | `verify_backup.py`: all 12 `online_*` tables restored into the new database with identical definitions, counts and hashes (`accounts` 38, `characters` 38, `combat_events` 77, `corpses` 10, `ground_loot` 9, `loot_claims` 9, `rewards` 78, `world_rewards` 15, `spawns` 212), 0.34 s |
| Restore equals source | `summary` identical on all 24 compared measures including the SHA-256 over every character's committed state; `verify` PASS before any server ran |
| Server on the restore, 90 s soak | 20 adapters with the original account keys: 3.4 s with no adapter connected (5 refused connects while the copied leases were still in force), then 20 connected for 76.6 s of 90 s, 48 attacks (33 accepted); only outage-class errors; graceful stop HTTP 200, exit 0 |
| Intentional restart, 45 s soak | new server process on the same restore: 20 connected for 34.3 s of 45.1 s, 0 adapter failures, 39 attacks (29 accepted); graceful stop exit 0 |
| Final verification of the restore | `verify` PASS against the source sampler (every revision at or above what it saw, 20 advanced) and against the restore sampler; 139 receipts (source 78), 160 combat events (source 77), 9 loot claims (unchanged), 11 corpses (source 10, one death on the restore), exactly one opening letter per character, 320 distinct item ids none shared |

An earlier attempt of the same day stopped at the repeated-greeting step (the soak walks characters away from the opening
NPC, so a second greeting is out of range); that step was removed, replaced by the one-letter-per-character check, and the
whole run was repeated from a new cluster.

## What this does and does not prove

- Proves: an exported-snapshot backup of a database populated by native play, including live copied leases, loot claims,
  recovered and unrecovered corpses and quest flags, restores byte-identically (table hashes plus the gameplay-level
  summary) into a separate database; a server on that restore serves the original accounts, lets the same characters fight
  and earn on top of the restored state, survives a graceful stop and restart, and none of the restored value is lost or
  granted twice.
- Does not prove: rendered clients, portal travel on the restore (the four walkers made no transfer in these 90 s), loot
  claims or corpse recoveries performed on the restore (their rows are proved intact by hash and summary), a remote or TLS
  host, a restore on a different machine or PostgreSQL major version, or the CI PostgreSQL 18 authority.
