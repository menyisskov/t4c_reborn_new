# Database connections and shutdown (T4C-0197)

The long-lived `--online` server owns one bounded JDBC pool. Single-player never opens it.
The pool reduces the measured creation of short-lived PostgreSQL connections; it does not prove
the precise cause of the historical SQLSTATE08001 connection-establishment failure.

## Connection ownership and limits

`ServerMain` uses `OnlineStore.pooledFromEnvironment()`, backed by HikariCP 7.1.0 and PostgreSQL's
`PGSimpleDataSource`. The dependency is pinned; see the [maintainer's versioned documentation](https://github.com/brettwooldridge/HikariCP/blob/HikariCP-7.1.0/README.md).
Configure private `T4C_DB_URL`, `T4C_DB_USER`, `T4C_DB_PASSWORD` as described in PERSISTENCE.md.
Never put credentials in launch arguments, checked-in profiles or diagnostic output.

| Current code setting | Value / behavior |
| --- | --- |
| Maximum physical connections | 8 per persistent server process |
| Minimum idle | 2, limited by the configured maximum in focused tests |
| Connection acquisition | At most 1 second before refusal |
| Connection validation | 250 ms |
| Driver connection/login timeout | 5 seconds |
| Driver socket timeout | 10 seconds per blocked read; not an overall transaction deadline |
| Driver TCP keepalive | Enabled |
| Default returned connection state | Auto-commit, READ COMMITTED; dirty transactions roll back on return |
| Lifetime | Closed by server teardown, after bounded persistence cleanup |

The code applies driver limits after URL parsing. Operations borrow/close handles through the
existing store API; closing a handle returns it to the pool. The pool performs no automatic
gameplay transaction replay. An ambiguous write still disconnects and reloads committed state.

`OnlineStore.fromEnvironment()` remains the short-lived unpooled factory for operator commands
and existing tests. Injected connection factories are caller-owned; closing such a store does
not close an external pool. The explicit pooled factory returns an AutoCloseable owned store.
The launcher transfers that ownership to the server only after successful construction; startup
failure closes the untransferred pool. One process per realm remains the deployment assumption.

Account roster work has separate bounded workers and no simulation work, character lease or player
slot. It shares the physical pool limit; polling must not create an unbounded independent pool.
See ACCOUNT_LIFECYCLE.md. Simulation/metrics code must not borrow database connections on tick.

## Stop, restart and failures

Use `./multiplayer/stop-preview.ps1` for recorded local QA instances. It verifies process identity,
uses the private loopback shutdown endpoint and waits up to 90 seconds. Never print its control token
or kill all Java processes. `-Force` is intentional crash testing, not ordinary shutdown.

The server stops accepting work, stops the simulation and drains/terminates account, character and
creature workers. Character persistence has a 12-second normal flush budget plus a separate bounded
termination wait; region cleanup continues even if another region's cleanup fails. Concurrent close
callers wait on the same termination fence. Interrupts do not turn an unfinished flush into success.

A normal stop is successful only after the workers have exited and accepted work is settled.
On timeout/failed flush, outstanding results fail and shutdown reports an explicit failure. If the
owned pool must then be forcibly closed, the log says teardown was incomplete; it is not a safe-save
claim. Recover from the last committed database state after lease expiry. Never compensate with
guessed rewards, source positions or an automatic repeat of an uncertain mutation.

Sanitized diagnostics contain bounded exception/cause class names and SQLSTATE, or queue rejection.
They omit raw messages, SQL values, identifiers and connection settings. The server ships a NOP
SLF4J provider so library exception/config dumps cannot bypass that policy. Preserve useful category
counts and private local logs; do not publish profiles, archives or raw machine crash reports.

For SQLSTATE08001, inspect listener/reachability, measured connection pressure and the sanitized
cause category. Do not assume a gameplay conflict or a port exhaustion diagnosis. For pool acquisition
timeouts, inspect load/slow transactions and bounded worker use before changing limits. No unchecked
increase in database connections substitutes for a recovery or sustained-load test.

## Recorded verification

- Focused real PostgreSQL tests prove physical backend reuse, rollback/state reset between borrowers,
  bounded saturation, owned-pool closure, replacement of a deliberately terminated owned backend,
  and preservation of committed character state.
- Shutdown regressions cover a blocked accepted write which ignores interruption, incomplete-flush
  reporting, recovery of an unacknowledged commit, and two concurrent closers waiting for the drain.
- T4C-0345 added a server-level regression and a live drill for the other half of that rule. The regression
  (`PreviewServerTest.shutdownWhileTheDatabaseIsUnreachableFailsVisiblyAndNeverReleasesTheLease`) joins a character, makes
  every database connection fail, and requires `close()` to throw "teardown reported a failure", the writer lease to stay in
  force (nothing was committed or released) and a second `close()` to report the same recorded failure. The live drill is
  `run-native-fault-drill.ps1 -Mode ShutdownOutage` ([tools/NATIVE_FAULT_DRILL.md](tools/NATIVE_FAULT_DRILL.md)): PostgreSQL
  is crashed under twenty playing adapters and the operator stop is requested immediately; the server process must exit
  non-zero within 120 s with a failure line on stderr, committed state must survive, and a restarted server must serve all
  twenty again after the leases expire. The pool now also reports operation time, pool wait and acquire timeouts
  (`/admin/metrics` `database`, see LOAD_METRICS.md).
- Final local integration before delivery passed 18 domain + 82 server tests, zero failures/errors/skips.
  That count includes separate metrics groundwork still pending its own delivery.
- Actual native roster and network-fault probes passed after the corrected pooled server restart:
  owner-only discovery, selected-character joins, delayed reads, a lost committed NPC reply,
  no automatic replay and the same letter identity/progress after reconnect.
- A 180-second twenty-native-adapter smoke passed: 20 connected for 147.121 seconds, 184 attack attempts,
  120 accepted, 35 HP changes, 10 reconnects, zero adapter/invariant/health failures, observed 33.9736 Hz.
  Database rollbacks were unchanged. Report: `target/native-adapter-soak-0197-pooled-smoke.json`.

Reproduce with the private test DB environment and `mvn -B -f multiplayer/pom.xml verify`,
tools/NATIVE_ROSTER_PROBE.md, tools/NATIVE_NETWORK_FAULTS.md and tools/NATIVE_ADAPTER_SOAK.md.
Local logs: `$env:TEMP/t4c-0200-integrated-reverify.log`, `t4c-0200-roster-probe.log`,
`t4c-0200-network-fault-probe.log`. These results do not establish rendered clients, an actual
database restart drill, remote TLS deployment or a clean two-hour/final Beta soak.

The operator stop script also passed its normal recorded-server end-to-end check with a
readable zero exit, followed by successful persistent restart and advancing health ticks.
Hidden local child-process checks accepted exit0 and rejected exit7; no database failure was
injected into the owner's live realm. Log: `$env:TEMP/t4c-0197-stop-script.log`.
