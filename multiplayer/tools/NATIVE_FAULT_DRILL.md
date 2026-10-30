# Twenty-adapter fault drills (T4C-0310, M5)

`run-native-fault-drill.ps1` (with `NativeFaultDrill.java`) injects a real fault into a running realm that twenty paced
native `OnlineConnection` adapters are playing on (the unchanged `NativeAdapterSoak`), then judges recovery against the
committed PostgreSQL record. It uses no windows, so it is **native-adapter evidence, not rendered-client evidence**, and
local PostgreSQL 16 evidence only (the CI PostgreSQL 18 job stays the authority for persistence contracts).

| Mode | Fault | What must hold |
| --- | --- | --- |
| `ServerKill` | the owned `ServerMain --online` child is hard-killed (`Stop-Process -Force`, no `/admin/shutdown`) 100+ s into the soak, left dead for `-OutageSeconds`, then restarted on the same database | committed state intact, leases fence the old sessions until they really expire, all twenty adapters rejoin, no duplicate value, final graceful stop exits 0 |
| `DatabaseCrash` | the owned acceptance cluster is stopped in immediate mode (`acceptance-cluster.ps1 -Action Crash`, a verified `pg_ctl stop -m immediate` of the cluster's own postmaster) while the server keeps running, and started again (PostgreSQL crash recovery) | the server fails closed (characters disconnected, no invented success), stays alive, recovers on its own when the database returns, committed state intact, all twenty rejoin |
| `LostAck` | seven to twelve rounds that hard-kill the owned server a fixed number of milliseconds after one character's opening-NPC greeting request has been handed to the adapter (the kill races the reward commit), then restart; after lease expiry every character greets again | exactly one opening letter per character whether or not the kill landed before or after the commit; a committed-but-unacknowledged reward is visible on rejoin |

| `ShutdownOutage` (T4C-0353) | the acceptance cluster is crashed as in `DatabaseCrash` and the private operator stop (`/admin/shutdown`) is requested about 1.5 s later, while the sessions are still bound | the stop is a visible failure: the server exits **non-zero** within 120 s with `Server shutdown failed ...` / `Persistence flush incomplete` on stderr, never exit 0; committed state survives PostgreSQL crash recovery; the unreleased leases expire on their own clock; a restarted server serves all twenty adapters; its final graceful stop exits 0 |

## Safety

- Creates ONE new acceptance cluster (`acceptance-cluster.ps1`, id `t4c_acc_<12 hex>`, WSL Ubuntu PostgreSQL 16, loopback
  `-DbPort`) and starts ONE server on `-ServerPort`. The script refuses 55432, 55433, 18765, 18766, 18770, 18771 and any
  port that is already listening; the Java tool refuses any database whose name is not `t4c_acc_<12 hex>` on loopback.
- The password stays inside `acceptance-cluster.ps1 -Action RunEnv` (process-scoped environment, never printed, never in
  argv). The drill kills only processes it started itself: the server (by its `Process` handle; `greet-kill` additionally
  requires the target to be a `java.exe` that has the same parent process as the tool) and the soak, monitor and tool
  JVMs. PostgreSQL is only ever stopped through the cluster helper's ownership-verified `Crash`/`Stop`.
- A copied server runtime and the evidence go to a new `%TEMP%\t4c-0310-<mode>-<cluster id>` directory. Control tokens and
  the profile JSON (account keys) are deleted when the drill passes; on failure the cluster, the profiles and the logs are
  kept for inspection and the cluster must be destroyed afterwards with `acceptance-cluster.ps1 -Action Destroy`.
  The cluster is destroyed automatically only after a complete run without `-KeepCluster`.

## Run

Build first (`mvn -q -DskipTests package` at the root and `mvn -q -DskipTests package -f multiplayer/pom.xml`, plus
`mvn -q dependency:build-classpath -Dmdep.outputFile=target/classpath.txt`). Claim the ports and the TEMP directory in
`PEER_WORK.md` before running. From the repository root:

```powershell
# Optional: 8 of the 20 adapters also take paced portal trips (cave, Lighthaven door, sewer stairs).
$env:T4C_SOAK_PORTAL_WALKERS='8'; $env:T4C_SOAK_PORTAL_INTERVAL_SECONDS='30'; $env:T4C_SOAK_PORTAL_DWELL_SECONDS='10'
./multiplayer/tools/run-native-fault-drill.ps1 -Mode ServerKill    -DbPort <free> -ServerPort <free> -Seconds 330 -FaultAtSeconds 100 -OutageSeconds 12
./multiplayer/tools/run-native-fault-drill.ps1 -Mode DatabaseCrash -DbPort <free> -ServerPort <free> -Seconds 330 -FaultAtSeconds 100 -OutageSeconds 15
./multiplayer/tools/run-native-fault-drill.ps1 -Mode LostAck       -DbPort <free> -ServerPort <free>
```

A drill takes about 6.5 minutes (`LostAck` about 1.5). The script throws on the first failed criterion.

## How it judges

`NativeFaultDrill provision` creates twenty accounts and characters beside the opening NPC; `greet` has each character
obtain the opening letter through its adapter (one at a time, paced). `monitor` then samples the committed database every
second (20 characters: revision, experience, gold, a hash of the whole progress record, live lease; the ids of every
reward receipt, world-reward claim, combat event, transfer event, loot claim, ground-loot drop and corpse seen). Right after
the fault (while the server is dead for `ServerKill`; as soon as the database is back for `DatabaseCrash`) and again at the
end, `verify` checks the database against what the monitor had seen:

1. no character's committed revision went backwards, and a character still at the last observed revision has the identical
   experience, gold and progress hash (nothing committed was lost or silently changed);
2. every receipt, claim, event, drop and corpse id seen committed before the fault is still there;
3. ground-loot state never leaves a terminal state, and every claimed drop has exactly one matching claim receipt;
4. no character holds more experience than its reward receipts paid (a deaths-only lowering is allowed, an invented gain is not);
5. at most one opening letter per character (inventory plus any unrecovered corpse), and exactly one after the greeting;
6. no item id is held by two characters or by a character and an unrecovered corpse.

The PowerShell side adds: the soak may report only outage-class errors (`adapter_stopped_*`, `connection_or_reconnect_timeout`,
`gameplay_result_unavailable`, `snapshot_stalled`); **any other adapter error kind** (revision regressed, owner identity,
epoch regressed, position not walkable, tick regressed, region mismatch) fails the drill; all twenty leases must be live
again; for `ServerKill` they must be back within `OutageSeconds + 60` s of the fault (a lease is 30 s from its last renewal,
so every lease is free by fault + 30 s; restart, twenty paced opens at 0.5 s and the soak's 3 s retry follow); and the final
server must still stop through the private `/admin/shutdown` with exit code 0.

Limits of these checks: receipts and ids are compared only with what a one-second sampler saw; a row committed and lost
within one second would not be missed by the revision check but would by no other. The experience rule detects invented XP
only when a character has no matching receipts. A client "lost acknowledgement" is simulated for one request type only
(`LostAck`, the opening greeting); the soak's own in-flight attacks and moves at the kill are counted as
`gameplay_result_unavailable` but their outcome is judged only through the record afterwards.

## Recorded runs (2026-10-29, local PG16 WSL, protocol 7, schema 13, `origin/main` c079b1c0, disposable clusters, all destroyed)

Common setup: 20 adapters paced one open at a time, 330 s soak (360 s with the second walker run), fault 100 to 150 s in.
The fault times below are measured on the monitor's database sampler (1 s resolution). Every run's final graceful
`/admin/shutdown` returned 200 and the server exited 0, and `verify` printed
`RESULT PASS` (all checks above, both right after the fault and at the end).

| Run | Fault | Server / database | Adapters | Leases (live out of 20) |
| --- | --- | --- | --- | --- |
| ServerKill, no walkers | server pid hard-killed at +100 s of the soak, 12 s dead, restarted (healthy 1.55 s after launch) | no error output at all in either server log; after the kill and before the restart: 18 characters unchanged at their last observed revision, 2 had advanced, 28 combat events, 47 receipts, 3 world rewards, all present; end: 124 combat events, 109 receipts, 14 world rewards | 69 failures (all outage-class: 20 `adapter_stopped_after_connect`, 6 `adapter_stopped_before_connect`, 1 `gameplay_result_unavailable`), 88 reconnects, 265.9 s of 329.9 s at 20 adapters | 20 live for the first 25 s after the kill (old sessions fenced), first expiry +26.1 s, lowest 2 at +30.2 s, all 20 live again +40.4 s |
| ServerKill, 8 portal walkers | same, fault at +120 s | server logs were not inspected for this run (the TEMP evidence was deleted before the check); 20 transfer events committed over the run | 69 failures (`adapter_stopped_*`), 89 reconnects, 20 region transfers by all adapters (14 trips started, 7 completed, 3 leg timeouts); several walkers were mid-trip (walking to a portal, in a destination region) at the kill and carried on after the restart | first expiry +25.2 s, lowest 2 at +30.3 s, all 20 live +40.6 s |
| DatabaseCrash, no walkers | PostgreSQL immediate stop at +100 s, down 15 s of drill wait (monitor could not reach it from +0.8 s to +17.1 s), started again | server stayed alive and `/health` answered 200 throughout (it does not touch the database); 20 `Online persistence failed ... character disconnected. Recover after lease expiry.` lines (18 checkpoint with `ConnectException` 08001, 1 checkpoint `SocketException` 08006, 1 `monster_attack`); pool recovered on its own, 14 database rollbacks over the run | 36 failures (`adapter_stopped_*`, 1 `gameplay_result_unavailable`), 55 reconnects, 267.6 s of 329.9 s at 20 adapters | 20 live until expiry +25.3 s, lowest 6 at +30.4 s, all 20 live again +38.6 s |
| DatabaseCrash, 8 portal walkers | same, fault at +150 s | same shape, 19 disconnect lines (one adapter was mid-reconnect); 23 transfer events committed | 35 failures (adds one `connection_or_reconnect_timeout`), 60 reconnects, 23 region transfers | lowest 5 at +30.7 s, all 20 live again +49.2 s |

`LostAck` ran twice on a fresh cluster each. Sweep 1 (kill 0, 1, 2, 4, 8, 16, 32 ms after the request was handed over):
all seven requests lost their reply and the database held **no** letter (the kill came first). Sweep 2 (0, 8, 25, 40, 55, 70,
85, 100, 120, 150, 200, 300 ms): ten requests (up to 150 ms) lost their reply with no letter committed; the 200 ms and 300 ms
rounds lost their reply **but the letter was already committed** (the database showed it immediately after the kill).
After the 32 s lease wait and one more greeting from every character, all twenty held exactly one letter and `verify`
passed (20 receipts, 320 distinct item ids, no sharing). So both outcomes of a lost acknowledgement (nothing committed,
committed but unacknowledged) were produced and neither duplicated the reward.

Existing probes re-run on the same build (separate disposable clusters, destroyed afterwards):

- `NativeRestartProbe` (two adapters, graceful stop, forced child kill, real lease expiry, letters): PASS. Letters, flags,
  progress hashes, gold and XP unchanged through every phase; revisions 1 to 2 to 3 (graceful checkpoints); both leases still
  in use right after the kill; real lease wait 29,673 ms; all three server stderr logs empty.
- `NativeTamperProbe` (322 cases including the replay/reorder/duplicate group `IDEM`): 322 PASS, 0 FAIL, server exit 0.

Not run, and why: `NativeDatabaseRecoveryProbe` (T4C-0208) hard-codes PostgreSQL port 55433, server port 18768 and its own
`t4c_db_0208_*` cluster helper; the task forbids 55433, and its guards were not changed. The 20-adapter `DatabaseCrash` drill
above supersedes its scope but is a different tool. `NativeLootProbe` (retried pickup, lost response) needs a killer that
survives the lethal starter ground (open owner decision T4C-0248) and was not run. `NativeTransferProbe` was not re-run
(portal traversals are exercised by the walker drills). No rendered clients, no TLS, no remote host, no wire-level packet loss.

## Operator stop while the database is down (`ShutdownOutage`, T4C-0353)

Recorded 2026-10-30 (local PG16 WSL, protocol 8, schema 13, `origin/main` 3e147d8b plus the T4C-0353 branch, disposable cluster `t4c_acc_30b26fc029fe` on 57705, server on 18861, 300 s soak, destroyed afterwards):
PostgreSQL was stopped in immediate mode 101.5 s into the soak with 20 live leases; the private operator stop was requested 1,542 ms later. The server answered HTTP 200 (the request was accepted,
which is not a success claim) and **exited with code 1, 30 s later** (the 12 s accepted-write drain, the separate termination wait and creature flush). Its stderr held 19 checkpoint and 1 monster-attack
`Online persistence failed ... character disconnected. Recover after lease expiry.` lines, `Server shutdown failed; closing owned database pool after incomplete teardown. Recover from committed state.`,
`Server teardown reported a failure` and `Persistence flush incomplete; recover from last committed state after lease expiry` with suppressed `Creature persistence flush incomplete`; nothing claimed a save and nothing was replayed.
The monitor could not reach the database from +1.7 s to +35.2 s. After PostgreSQL's crash recovery `verify` passed on everything the monitor had seen committed; all 20 leases expired on their own clock (0 live from +36.2 s),
a new server was healthy 1.6 s after launch with empty stderr, all twenty adapters were live again at +60.7 s, the soak recorded only outage-class errors (107 failures, 126 reconnects), the final `verify` passed, and the final
graceful stop exited 0. A first attempt the same day stopped at the greeting step because this script let a native command's stderr line reach its parent's `Stop` error handling; `Java-Tool` now merges stderr into its output.
It is local PG16, native-adapter evidence; it does not show a stuck-worker stop (that is the unit test `incompleteFlushWaitsForAcceptedWorkerExitAndReportsFailure`) or a rendered client.

## Loot claim and corpse recovery under a hard kill (`NativeLootFaultDrill`, T4C-0342)

A separate, self-contained tool for milestone M2 ("a lost pickup acknowledgement retains exactly the committed outcome"). It needs no soak: it provisions one fresh character per round, seeds
one owner-window ground drop (claim rounds) or one two-stack corpse with 50 gold (recovery rounds) at the character's tile before the first server start, starts its own `ServerMain --online`
child, joins through the real `OnlineConnection`, waits until the drop or corpse is in the adapter's view, hands the request to the adapter and hard-kills the child 0, 10, 25, 50, 80, 120, 200, 350 or 600 ms later,
restarts it, and after the last round waits 38 s (lease expiry) and retries every request through a new adapter. It requires that the database is a loopback `t4c_acc_<id>` cluster (refuses the protected ports and a listening port), writes
`report.txt` to a new output directory and exits 1 on any failure. Checked per round: the database right after the kill holds exactly the applied outcome (potion +1 and one receipt, or two stacks and 50 gold with the corpse recovered)
or exactly the unapplied one, never half; no duplicate item id in the inventory; after the retry the item(s) are there exactly once, the drop is `CLAIMED` by the claimant, the corpse is recovered; the final graceful shutdown exits 0.

```powershell
# repository root, after the Maven builds of ACCEPTANCE_CLUSTER.md; $rt is a copy of t4c-server.jar, lib/* and both .colbin maps outside the checkout
javac --release 21 -proc:none -cp $cp -d $probeDir multiplayer/tools/NativeLootFaultDrill.java
& multiplayer/tools/acceptance-cluster.ps1 -Action RunEnv -RunId t4c_acc_<id> -Command java -CommandArgs @('-Djava.awt.headless=true','-cp',"$probeDir;$cp",'com.perso.T4C.server.NativeLootFaultDrill',$rt,$javaExe,'<free-port>',"$env:TEMP\lootfault-<id>")
```

Recorded run 2026-10-30 (cluster `t4c_acc_cebf2600e307`, destroyed): 18 rounds, `RESULT: PASS`. Claim: delays 0 to 200 ms, reply lost and nothing applied; 350 ms reply lost and applied; 600 ms acknowledged and applied. Recovery: 0 to 120 ms lost and nothing applied; 200 and 350 ms lost and applied; 600 ms acknowledged. Every retry was accepted for an unapplied case and
refused "unavailable" for an applied one; no duplicates. Details in [CLIENT_UNIFICATION_DESIGN.md](../CLIENT_UNIFICATION_DESIGN.md) 3.19.
