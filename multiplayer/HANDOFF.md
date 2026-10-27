# Multiplayer implementation handoff

Last reviewed: 2026-10-27, Asia/Jerusalem. This file is the restart point for a different model. Recheck Git and running processes before acting; this describes recorded evidence, not live state.

## Read first

1. `../AGENT.md`, `../AGENTS.md`, `../CLAUDE.md` are authoritative engineering, review and delivery instructions.
2. `../DESIGN_GUIDELINES.md`, section Multiplayer delivery, records lasting owner decisions and implementation contracts.
3. `MILESTONES.md` is the owner-facing stage/six-milestone roadmap with exact completion criteria. `ACCEPTANCE.md` is the release evidence checklist. Unchecked Alpha/Beta gates remain unfinished.
4. `../TASKS.md` and `../CHANGELOG.md` record shipped work. This document records integration state and what to do next.

The owner is PM/QA and has authorized autonomous implementation, focused tests, PRs, CI, self-review and merges. Use parallel sub-agents for independent tasks with explicit file ownership. Continue assigning the next crucial release item; optional polish does not outrank required gameplay. Never claim Beta complete from service unit tests alone.

## Preserve single-player and personal state

The normal `launch.bat` and offline game entry point must keep working. Online launch is `com.perso.T4C.online.OnlineClient` through `start-client.ps1`; it never initializes offline saves/screens. Online account/character state belongs in PostgreSQL, not desktop character files.

The checkout intentionally contains user changes in account_storage.json, characters.json, characters/*.json, editor_camera_position.json, game_preferences.json, monsters_report.csv and t4c-chat.log. There are also private ignored online profiles, .lwjgl, crash reports and Python caches. Do not broadly stage, reset, clean or stash these. Select files for each delivery. JVM crash reports contain machine details and must not be committed.

A protected-file backup from before multiplayer work exists in the user's temporary directory named `t4c-local-before-merge-0ad2911c72e641bd81173c0978d5f05a`. Hash comparisons have shown zero content differences during this work; do not restore over newer user changes.

## Current implementation

The server is a separate headless Maven reactor (`pom.xml` in this folder) with domain/shared rules and a server module. The simulation owns mutable world state at 34 ticks/sec, caps sessions at 20, validates direction-only movement, authoritative collision/sanctuary/LOS and bounded authenticated request budgets. The realm initially hosts an 80 by 60 starting crop (world 0, origin 2904,1029) and an 80 by 60 interior crop (world 1, origin 264,353), connected only by authored portals 16/17. Six guards occupy the starting crop; eleven interior residents retain unsupported scripted/combat restrictions. Global capacity is twenty across regions.

PostgreSQL is authoritative for accounts, leases, character payloads, balances, operation receipts and creature lives. CharacterProgress v1 is immutable; progression is cumulative XP within the current incarnation. Shared offline helpers supply progression, equipment eligibility/bonuses, weapon damage/timing and combat resolution. Do not trust client damage, coordinates, inventory or rewards.

PersistentSessions uses bounded worker queues and simulation-thread publication. Corpse reads use a separate two-worker bounded pool and one in-flight lookup per character, so polling bursts cannot exhaust gameplay-write capacity. Death/transfer invalidates pending creature reads as well as cached matching targets, preventing stale reads from restoring cleared aggro. Sanitized failure diagnostics expose exception class/SQL state or queue rejection, never raw exception messages. Gameplay results become visible only after commit. Ambiguous writes disconnect; reload committed state after reconnect. Receipts bind operation payloads and durable creature life IDs prevent duplicate kill grants. Creature HP/life/respawn recovery is persistent. One authoritative process per realm is assumed; no distributed world-leader lease exists yet.

Native melee is currently a restricted immediate-impact preview: Space attempts nearest target, with range/LOS/cooldown/revision validation. Existing shared targets publish HP; per-hit/death XP and gold commit with damage. Scripted targets, item-drop targets, advanced effects, ranged/stun attacks and active quest/buff behavior remain unsupported. Authored stationary actors remain protected, matching offline rules; temporary lack of AI movement is not that authored flag.

## Active deliveries and ownership

- T4C-0189 / PR #113: online-only Windows GLFW crash correction. Pinned upstream source, minimal owned-window patch, license, reproducible build, bundled checksum-verified DLL and first-line OnlineClient bootstrap. The single-player native selection is untouched. Two rendered clients survived repeated direct focus activation and Alt-Tab, and combat still responded. Native load/checksum and desktop package/tests passed. Merged as 8ad6c30a after green CI; local runnable package/tests passed.
- T4C-0190 / PR #114, merged as beaf80ec: retaliation scheduler, schema-v8 durable aggro/attack deadlines, transactional monster hits, offline-compatible death penalties/half-HP return, durable owner-recoverable corpses, public committed player HP, protocol v3 and native recovery intent. The actual native two-connection adapter probe passed targeted damage, observer isolation and fatal half-HP return. HTTP corpse recovery ownership/range/identity/retry checks passed. A three-minute twenty-adapter smoke completed with zero connection/invariant/health failures, 185 attack attempts/112 accepted, 46 HP changes and ten reconnects; this is not rendered or two-hour evidence. Green CI, scoped self-review and local rebuild passed; rendered retaliation validation remains pending.
- T4C-0191, backup verification delivery: consistent-snapshot PostgreSQL backup/restore verification into a new t4c_restore_* database. Tooling restored and matched both schema7 QA and schema8 test databases; merged as f3b76d76 after green CI. Table comparisons alone do not satisfy a restored gameplay playthrough.

- T4C-0192: configurable HTTPS, standard native trust/hostname validation, origin allowlist and local administration are implemented and tested; merged as 4a453cd5 after green CI. See TLS.md for foreground server/client commands and external deployment requirements.
- T4C-0193: authored portal catalog/export/destination audit and MAP_TRANSFER_DESIGN.md merged as 01593c7f after green CI.
- T4C-0194 / PR #118, merged as e7989d7d: protocol 4/schema 9 two-region runtime, native map switching and persistence read/cache fixes passed green CI. Full PostgreSQL verification passed; both two-HTTP-client and actual two-native-adapter direction-only 16/17 round trips/reconnect/isolation passed. Native read-race tests pass without replaying mutation. Rendered map switching remains pending.
- T4C-0195 / PR #119, merged as397d9ace after green CI: shared Kilhiam letter rules, NPC catalog/replication, authenticated transactional interaction and native network intent are delivered. Full PostgreSQL reactor passed 18 domain plus 72 server tests; 17 focused desktop tests and the two-actual-native-adapter NPC probe passed. First letter/flag, repeat greeting, observer isolation, unrelated-state preservation and reconnect UUID retention are verified. Visible NPC rendering/dialogue controls and the full quest chain remain unfinished. See NPC_INTERACTION_DESIGN.md and tools/NATIVE_NPC_PROBE.md.

Root coordinates shared docs, selective commits and GUI QA. The map/server agent has drafted the next authenticated account-roster slice; the NPC agent has completed service integration; the load-test agent owns the fresh isolated two-hour baseline. Inspect actual live agent status rather than assuming these agents remain available after a model switch.

## Integration contracts under construction

`OnlineStore.listCorpses(session)` is worker-only and owner/lease checked. `recoverCorpse(session, revision, corpseId, world, x, y)` commits exact stored instances/currency and marks the corpse recovered atomically; trusted server position is range checked again in the transaction. Native/server intents contain revision, location epoch and corpse UUID. `GET /corpses` exposes only owner summaries; `POST /recover` queues recovery. Never accept client corpse contents.

PlayerView/OnlineConnection.Actor include committed hp/maxHp. Current wire protocol 4 adds OnlineRegion and locationEpoch to maps/snapshots/private state and every region-sensitive intent; the browser diagnostic payload/version matches. The native client refreshes private state by committed revision and refreshes corpse summaries with it. The gameplay command queue remains single flight; no automatic mutation replay after ambiguous failure.

Retaliation binds aggro to durable character identity, not a temporary network token. Movement must continue for other players while writes are pending. Fatal transition/recovery freezes the affected actor; location/HP changes publish only after atomic commit. Logout during death must checkpoint the committed respawn location. Hosted cross-map respawns use the same coordinator. Production rejects unavailable saved worlds/respawns before admission; it does not silently rewrite a destination.

## Build and focused validation

Use PowerShell from the repository root with Java 21 and Maven. Avoid `mvn clean` while runtime JARs are open. Root API changes may need full source recompilation (`-Dmaven.compiler.useIncrementalCompilation=true`); partial stale bytecode once caused a misleading NoSuchMethodError, fixed by a full compile.

```powershell
mvn -B '-Dmaven.compiler.useIncrementalCompilation=true' '-Dtest=OnlineNativeLibraryTest,OnlineConnectionTest,DeathPenaltyServiceTest' package
mvn -B -f multiplayer/pom.xml test
```

PostgreSQL tests are enabled only with T4C_DB_URL/T4C_DB_USER/T4C_DB_PASSWORD. Do not treat skipped DB tests as validation. The local WSL Ubuntu PG16 cluster uses 127.0.0.1:55432 with databases `t4c_online` (QA) and `t4c_test` (tests), role `t4c_online`. Read the password privately from WSL `/home/menyiss/.local/share/t4c-online-pg/password`; never print it or include it in command lines, logs or docs. Configure connect/socket timeouts. Protocol 4 clients and servers must be rebuilt together. Schema 9 rejects older server binaries; rollback requires compatible code or a separately restored matching backup. `--online` requires `assets/maps/worldmap/worldmap.colbin` and its sibling `assets/maps/dungeon/DungeonMap.colbin`; the native renderer also requires their mapbin assets. The default respawn `(0,2951,1038)` is valid; other unhosted saved locations/respawns are refused without rewriting them.

The PG runtime is under WSL `~/.local/share/t4c-online-pg/runtime/usr/lib/postgresql/16/bin`. Docker is unavailable on this machine; do not change security settings to fix it.

```powershell
./multiplayer/stop-preview.ps1
# Set private T4C_DB_* environment for the QA database before the next command.
./multiplayer/start-preview.ps1 -Persistent
./multiplayer/start-client.ps1 -Profile multiplayer/local/qa-melee-one.json
./multiplayer/start-client.ps1 -Profile multiplayer/local/qa-melee-two.json
```

Private QA profiles must never be printed or committed. Existing Rowan/Willow test characters were positioned near guard1 for melee checks; do not silently rewrite their state. New isolated QA characters can be provisioned with the operator CLI when needed.

The server's open target/lib JAR can prevent Maven copy-dependencies on Windows. Stop it gracefully, rebuild, then restart; do not kill unrelated Java processes. Metadata under server/target/preview-session.json includes a private control token: never print it. `stop-preview.ps1` validates process identity and flushes checkpoints.

Re-export the compendium after changes as required by ship-pr, check `scripts/compendium_freshness.py --check`, and commit generated changes only if semantic output changed. Current recorded content fingerprint is d8e599c4b4b9d40612fd9750b032f47590061051355908bf0f8e30387b523f18.

## Running isolated stability check

The immutable protocol3/schema8 baseline is preserved at `target/soak-runtime-20261027_082605_f43e4b6d`. It recorded a persistence disconnect after about 30 minutes while health/ticks stayed normal, and was stopped gracefully after about 50 minutes. `soak-report-at-requested-stop.json`, supervisor logs and `DIAGNOSIS.md` retain the evidence. It is **not** a clean two-hour pass. Two separate deterministic regressions failed before and passed after the corpse-read queue separation and pending-aggro read fence; this does not establish the precise cause of that baseline disconnect. Final PostgreSQL verification after these fixes passed on 2026-10-27 (`$env:TEMP/t4c-0194-final-verify.log`). A fresh immutable protocol4/schema9 smoke passed: 180 seconds, 146 seconds at twenty connected adapters, 213 attacks/126 accepted, 63 HP changes, ten reconnects, zero adapter/invariant/health failures, observed 33.99 Hz. Its snapshot and fresh long-run restore live at `target/soak-runtime-v4-20261027_092643_4e10b372`. The long run started 09:34:58 Asia/Jerusalem on port18766, but recorded a player disconnect after about 101 seconds. It was subsequently stopped gracefully using stop.request; the supervisor records operator_stop/serverStopped=true/completed=false. Sanitized diagnostics report PSQLException SQLSTATE08001 (connection establishment); that player had made no attacks. It is not a clean pass. Inspect `long/soak-report.json`, supervisor logs and current investigation before restarting. High connection churn is measured, but ephemeral-port exhaustion is not established: Windows PG TIME_WAIT3345 versus 16384 dynamic ports, WSL TIME_WAIT1685, PG max_connections100 versus six observed backends, and no PG FATAL at the failure. The precise network cause is unproven. T4C-0197 is adding bounded JDBC reuse and sanitized cause diagnostics without automatic write retries. Server/adapter/supervisor initial PIDs were 43180/35372/42856; verify identities before actions. `long/stop.request` requests bounded graceful adapter logout and isolated server stop. Binary/source/map/supervisor hashes match the clean smoke. This run predates NPC integration and cannot establish final release acceptance. Never stop copied servers by broad Java-process commands. Main local 18765 realm runs the fixed protocol4/schema9 two-region build for adapter QA; its metadata is separate.

## Native QA and desktop state

Use the Computer Use skill and its supported sky API for all Windows UI interaction. The owner stopped desktop automation with the physical Escape key on 2026-10-27 after crash candidate focus checks. Do not resume GUI interaction merely because a heartbeat fires; continue non-UI implementation/tests until the owner resumes desktop testing. Do not claim retaliation was rendered with two clients until that happens.

Before stopping, patched native processes loaded separate t4c-online-glfw-* DLLs, remained connected as two players and responded to attacks. Existing old clients do not acquire a new DLL or new classes until restarted. Window handles/PIDs are ephemeral: rediscover them.

Required next rendered checks: two profiles, provoke guard with one, verify targeted HP changes only, move beyond range/behind wall, reconnect/restart with aggro intact, fatal return at halfHP, owned corpse recovery and no duplicate contents, repeated focus switching and ordinary Alt-Tab. Verify offline launch/save/load separately without mutating personal files merely to test.

## PR delivery

Use ship-pr/steward. Self-review the actual scoped diff. Commit task/changelog/design decisions together, then a small task-hash follow-up. Open PR with GitHub tools and attach it to this chat. Green Build and test on current head plus no conflict and handled real findings permits direct merge; do not wait for a running automated review. Check existing review threads first and respect AGENTS.md one-original-review/fix-verification protocol. Preserve pending independent work when switching main/next branch. Rebuild local checkout after merge.

## Remaining release sequence

Map-transfer delivery is merged and reliability regressions pass; diagnose the new SQL08001 load failure without mutating its captured runtime, then rerun after a verified fix. The opening NPC service is merged; complete its rendered interaction and native character selection/account lifecycle, quests/general ground loot, required spells/abilities/inventory controls, broader hosted world routes and encounters, full creation-to-progression playthrough, crash/reconnect/database/restore playthroughs, and attack/replay/duplication abuse checks. Repeat sustained two-hour 20-client mixed gameplay on the final integrated release with tick/DB/latency/resource measurements. HTTPS configuration exists, while external deployment and native remote QA remain open. Follow ACCEPTANCE.md rather than declaring Beta from this list alone. Deployment credentials or external hosting choices may need the owner; exhaust local work before asking.

## Peer coordination and latest verification

The owner added an external Claude session; after the initial not-started confirmation, Claude claimed0201/0202 scope/loot work in merged PR121 (db83004b). Follow
AGENTS.md section8 and the canonical PEER_WORK.md register before any new work. Codex retains
0196–0198 account/pool/metrics integration and shared Git/build/server/database operations.
Claude owns0201 scope/loot design and0202 loot domain rules (new domain files only); server wiring waits for Codex. The lane is claimed via PR121; adoption of the new canonical register still needs its acknowledgement. Codex coordination docs moved to0203 to preserve Claude task IDs.
All Codex sub-agent work remains under Codex ownership; use section6 model routing for new phases.

Latest integrated verification after the shutdown correction: cheap-model gpt-6-sol validation
passed18domain+82server=100PostgreSQL tests, none failed/skipped. The corrected pooled server
was gracefully stopped/rebuilt/restarted on18765/t4c_online; actual native roster and network-fault
probes passed again. Logs: `$env:TEMP/t4c-0200-integrated-reverify.log`,
`t4c-0200-roster-probe.log`, `t4c-0200-network-fault-probe.log`. stop-preview.ps1 is aligned to90s
and parsed successfully. Root Maven/build window is idle; the next measured smoke still remains.
This supersedes the earlier99-test/older-running-package checkpoint below. No GUI was opened.

## Current work and model routing checkpoint

Read AGENTS.md section 6 before selecting or delegating a model. The owner explicitly requests
cheap models for implementation, mid-tier models for bug investigation/RCA, and top-tier models
for deep architecture, milestone planning and design. Exact preferred names/tool aliases are
maintained there only; AGENT.md and CLAUDE.md reference that policy. Existing test/build processes
need not be restarted solely to change tiers; route the next phase correctly. A fresh gpt-6-sol
agent validated milestone/policy links and consistency without touching runtime code.

Owner-facing requirements: start with MILESTONES.md. TASKS.md tracks child deliveries; ACCEPTANCE.md
records release evidence. None of the six two-player milestones is complete, and the two-player
checkpoint is not an approved replacement for the agreed twenty-player Beta. Scope choices and
unverified checks are explicit. Do not infer a release date or completion percentage from task counts.

Current base: main merge397d9ace (PR119, opening NPC service); all CI passed on541fd5b3. The current
pending integration includes0196 roster service/native account adapter,0197 bounded database pool
and teardown,0198 scalar runtime metrics and new fault/roster probes. They remain unmerged.

Verified on the working tree at approximately10:05 Asia/Jerusalem: full PostgreSQL reactor
verify/package passed18domain+81server=99tests, zero failures/errors/skips. Twenty focused desktop
tests/package passed (`$env:TEMP/t4c-0196-native-account.log`). NativeRosterProbe passed owner-only
roster discovery, active-lease availability, selected-character joins and unchanged progress.
NativeNetworkFaultProbe passed150ms delayed reads and loss of a confirmed committed NPC reply:
owner disconnected without automatic replay, observer continued, reconnect retained the exact
single-letter UUID/progress. See ACCOUNT_LIFECYCLE.md, tools/NATIVE_ROSTER_PROBE.md and
 tools/NATIVE_NETWORK_FAULTS.md. This is networking evidence, not rendered UI or real packet loss.

Subsequent self-review found a shutdown timeout issue not covered by that99-test result: the pool
could close before persistence workers exited while shutdown appeared successful. The server agent
fixed bounded write/read/creature termination, aggregate region cleanup and explicit incomplete-flush
failure reporting. Ten focused PostgreSQL shutdown tests passed at10:14 (`$env:TEMP/t4c-shutdown-tests.log`),
including a blocked accepted write and concurrent closers. Full verification/package/restart after
that correction is still required. Align stop-preview.ps1 from60to90seconds before delivery.

Ownership: root owns milestone/task/changelog/design/handoff docs, selective commits/PRs and visual
integration. The former roster/server agent most recently owned the shutdown correction plus
SimulationLoop/ServerRuntimeMetrics/LOAD_METRICS groundwork. The native agent owns fault-probe files
and completed the integrated verification/probes. The original pool agent failed due to model
capacity; do not assume it remains active. Inspect live agents/processes and coordinate build windows.

Main18765 currently runs the earlier pooled99-test QA package on t4c_online, before the last shutdown
correction. Native GUI control remains paused after the owner's physical Escape stop. Both previous
immutable long runs failed and were stopped; no passing two-hour result exists. Do not restart another
long run before scoped self-review, final integrated verification/probes and a useful clean smoke.
Metrics code is not yet wired into an operator endpoint or soak report; no benchmark is established.

Visual feedback remains pending. The owner asked to show the preview again, which is not approval.
The tracked review document is design/online-ui-preview.html, and the inline preview is
`C:/Users/menyi/.codex/visualizations/2026/10/26/01a178e3-4d45-78f0-b0c7-d7aa3bdc5c2c/multiplayer-ui-preview.html`.
Reload the full visualize skill before creating/updating a visualization. AGENT.md requires previews
and incorporating feedback before live installation. Backend work continues; never treat silence
as approval. Opening/showing a preview does not authorize resuming desktop game automation.

Next integration order: finish pooled shutdown verification and align operator wait; run actual
roster/fault probes against the matching final package; fresh isolated measured smoke; self-review,
CI and merge the pending account/pool delivery. Then wire metrics/reporting, resume the failed
sustained gate after a verified fix, and implement M1–M4 gameplay/UI/creation/loot/quest/ability work
with the correct model tiers. Independent work can run in parallel with explicit file ownership.

## Agent lanes (Codex vs Claude) — check before claiming work

Two agent families work in parallel. The canonical live assignments and serialized claim/handoff protocol are now in PEER_WORK.md and AGENTS.md section8. Keep TASKS rows aligned; do not maintain a competing live ownership table here. The summaries below preserve Claude's PR121 claims.
Two agent families work in parallel. Claim a TASKS.md ID on main (merge the row first) before starting; do not start work another lane owns. Row prefix "(Claude lane)" / "(Codex lane)" marks the owner.

- **Codex lane:** T4C-0196 roster/native account, T4C-0197 DB pool/shutdown, T4C-0198 runtime metrics, their uncommitted files in the main checkout (all of multiplayer/server persistence, SimulationLoop, HostedWorlds, PersistentSessions, OnlineStore), soak/load gates (M5), operator tooling.
- **Claude lane:** T4C-0201 scope + loot design docs (new files SCOPE_PROPOSAL.md, LOOT_DESIGN.md), T4C-0202 M2 loot domain rules (new files under multiplayer/domain only). Server-side loot wiring waits until the Codex lane's pending server changes are merged.
- Unclaimed: M1 native UI, M3 quest service, M4 abilities. Whoever claims first posts the row; ask the owner if both want it.

## T4C-0196/0197 delivery checkpoint

User selected a fully implemented task; Codex finished0197 bounded database reuse and safe shutdown,
including readable-zero-exit enforcement in the operator script. Final local checks passed:
100PGtests including pending separate metrics prep;20focuseddesktoptests; real native roster and
lost-committed-reply recovery probes;180-second20-adapter smoke with120acceptedattacks,35HPchanges,
10reconnects and zero adapter/invariant/health failures; real operator graceful stop/restart.
The smoke report is target/native-adapter-soak-0197-pooled-smoke.json; operational settings and
failure/recovery contracts are in DATABASE_RUNTIME.md. Scope includes the coordinated0196 account
roster/service/adapter but not rendered account UI.0198metrics source remains unstaged/separate.
Self-review handled the slow-worker teardown and operator exit-status findings. CI/merge is next;
claims on shared core files remain held until the corresponding explicit release in PEER_WORK.md.
Claude's0201/0202 scope/loot lane remains distinct; the coordinator documentation is0203, not0201.

## Latest delivered checkpoint — PR124

T4C-0196/0197 merged as55ea45ff after all checks passed onb20e1dc1. Database reuse, safe teardown,
operator exit-status enforcement and roster discovery are delivered. Operational proof and limits
are in DATABASE_RUNTIME.md; normal single-player bootstrap was not changed and protected file
hashes match baseline. A100-test local PG pass included pending separate0198 metrics prep;
CI verified the actual delivered diff. The3-minute20-adapter smoke/recovery probes passed, but
no2-hour/full Beta/rendered-selector claim exists. The recorded QA server runs the verified pooled
package; desktop package refresh after merge is underway. See PEER_WORK.md for exact current windows.

Codex explicitly releases the completed0196/0197 file holds in PEER_WORK.md. PreviewServer and
0198 metrics/harness files remain Codex-owned. Claude has acknowledged the register in PR123 and
owns its distinct0201/0202 scope/loot work; it can claim released core files for a new declared phase,
while requesting handoff for still-owned PreviewServer. Root must not duplicate Claude's M2 work.
The cheap-model0198 agent is completing a protected loopback metrics endpoint and bounded load
report collection; no0198 completion claimed until actual endpoint/harness checks pass.

Post-merge desktop package refresh completed successfully.0197 is fully delivered in PR124;
0196 roster discovery delivered alongside it. The actual20-adapter/operator/native recovery checks
remain bounded evidence, not full Beta completion. PEER_WORK.md now records Claude's explicit
PR123 acknowledgement and exact pure-loot file scope.0198 code is prepared and awaiting focused
validation; no root Maven is currently running.

## T4C-0198 verification checkpoint

Metrics implementation is complete for delivery: protected actual-loopback/private-token endpoint,
nonblocking fixed scalar simulator/JVM snapshots, optional bounded operator sampling in the native
load report, missing-control unavailable state and explicit whole-response timeout/cancellation.
FullPGverify passed18domain+83server checks on55ea45ff plus0198; actual20s enabled/10s disabled
20-adapter reporting checks passed. The stalled-body diagnostic regression failed beforefix
(5.123s) and passedafter (2.021s). See LOAD_METRICS.md/NATIVE_ADAPTER_SOAK.md and ignoredreports.
Claude's PR123/main89dd0f64 is integrated without overwriting its tasks/docs. The combined source
passed 19 focused desktop tests, 21 domain plus 86 PostgreSQL server tests; the durable stalled-body
probe returned in 2.026 seconds. A 15-second 20-native-adapter metrics run collected four private
operator samples with zero metric/adapter/health failures and 13.975 seconds at twenty connected;
the credential/path was absent from its ignored report. These are local adapter checks, not full
Beta, two-hour or rendered-client evidence. Delivery still requires green CI and merge. Current QA metrics server
PID52688 runs the matching persistent build; verify current metadata before actions.

## Loot groundwork checkpoint (T4C-0201/0202, Claude lane)

Delivered: SCOPE_PROPOSAL.md (all PROPOSED, awaiting owner approval) and LOOT_DESIGN.md; loot tasks L1–L5 (shared MonsterLootTables, CarryCapacityRules, ItemKeys, GroundLootView/LootDropDraft, server OnlineLootDrafts and OnlinePickupRules). Verified by unit tests only (root targeted + multiplayer reactor; PostgreSQL tests skipped); no server wiring, no rendered or native evidence. Remaining: L6 schema, L7 server loot handling, L8 melee wiring, L9 pickup endpoint, L10 native client, L11 corpse weight (needs owner decision), L12 acceptance — L6/L7/L9/L11 wait for the Codex lane's pending OnlineStore/PersistentSessions/PreviewServer changes to merge. OnlinePickupRules.effectiveStrength throws on active buffs; L7 must map to a refusal.

Peer task-ID correction: remotePR125 had already reserved0204 for Claude loot wiring. Keep that
claim; independent Codex distribution packaging is0205 and nextID0206. Root missed the fresh
remote claim when allocating locally; both agents paused and preserved all work. AGENTS8 now
requires remote-main claim refresh plus the canonical shared ledger. Claude's requested
OnlineStore/PersistentSessions/HostedWorlds handoff is granted explicitly in PEER_WORK; keep
PreviewServer/SimulationLoop reserved until0198 merge. Packaging remains static-only pending
0198's validation/delivery window release.

## Current checkpoint after PR126 (supersedes earlier pending statements)

T4C-0198 merged as8da43482; all registeredCI green and post-merge desktop package passed.
The actual combined source (including ClaudePR123) passed19focuseddesktop and107PGchecks
(21domain+86server), deadlineprobe2.026s and a15-second20-adapter metrics smoke with4operator
samples, zero metric/adapter/health failures. Metrics are private local operator observations,
not a sustainedBeta/rendered-client/real-packet-loss acceptance result.

Codex explicitly released the completed0198 core files, including PreviewServer/SimulationLoop,
for new claims in PEER_WORK.md. Claude's0204loot wiring can claim them under the recordedhandoff;
no duplicate gameplay lane is being started byCodex. Codex's next task is0205 client distribution.
The packaging agent owns only new tools/docs and its isolated output windows. Its completed
source-snapshot validation is recorded below. Source/UI game automation remains paused;
prototype feedback is still pending.

## T4C-0205 Windows online test package checkpoint

The create-only builder/checker/fixture and installation guide are in
`tools/package-online-client.ps1`, `tools/OnlinePackageCheck.java`,
`tools/test-online-package-check.ps1` and `CLIENT_DISTRIBUTION.md`. The verified final bundle is
`target/online-client-packages/qa-0205-8da43482-b`, built from committed source
`8da43482ee5913cb7bf63273e49edce2cc2e38a2` in an isolated source archive. The isolated
Maven package succeeded after freshly compiling all 8,655 desktop sources. Correct runtime scope
contains 64 dependency JARs; 860 tracked assets and 929 manifest entries total 795,466,143 bytes.
Manifest SHA-256: `a368c29c6f987003223421029259bd1bcfe9087ce4cdae517cdeb4bfd4b201c3`.

The builder's headless self-check passed. Positive, unlisted-private-file, tampered-asset and
duplicate-path checker fixtures passed against the earlier package with unchanged checker code.
The corrected final bundle passed PowerShell 7, Windows PowerShell 5 and batch `-CheckOnly` from a
copied path with spaces. The first bundle's 73 dependencies came from an incorrect scope property
and are superseded; preserve it only as failure/correction evidence. No renderer, server connection,
remote clean machine, offline regression or owner QA was run. M6 remains incomplete.

## T4C-0207 isolated restart probe checkpoint

Owner-resumed rendered QA subsequently exercised the final T4C-0205 bundle from source
`8da43482` against unchanged schema-9 QA port 18765 with two separate native windows.
One player attacked a guard (55→28 HP), took retaliation (13/35 HP), died and rendered at
the temple with 17/35 HP, XP 439 and gold 238. The other player stayed at 35/35 HP,
XP 804 and gold 252. Closing and relaunching the attacked window with its same external
profile restored the rendered respawn and those balances while the observer stayed connected.
Several focus switches survived; seven protected account/character/preference file hashes
were unchanged. Both clients remain open for the owner, who controls the desktop/QA window.
This brief run adds partial M4/M5/M6 evidence only. It did not exercise full quests, loot,
corpse pickup, remote deployment, offline save/load, sustained focus behavior or the schema-10
server. Earlier failed focus/soak runs remain recorded; the owner explicitly resumed this
desktop session after the prior automation pause.

`tools/NativeRestartProbe.java` and `tools/NATIVE_RESTART_PROBE.md` provide a bounded two-adapter
server-process restart drill. The exact final-source SHA-256
`9F324B943D982B5D14F0FB20A60A5E3650DCE23020DF3E2D3894C091ECBA6AEA` passed on an
immutable committed `8da43482` protocol-4/schema-9 runtime, fresh
`t4c_restart_0207_final_03e1c342` database and isolated port 18767. Both distinct letter UUIDs,
flags, complete progress hashes, positions, gold and XP survived graceful exit 0, forced child
kill, real 29,690 ms lease expiry and rejoin. Revisions moved 1→2→3 across the graceful
checkpoints; the forced kill left the committed revision at 2. The sanitized private report is
`%TEMP%/t4c-0207-evidence-finalsource-03e1c342/report.txt`; keep the dedicated database and
reports for inspection. The tool also compiled against the later combined schema-10 API in an
isolated build snapshot, but that runtime was not played through. No shared QA server, rendered
clients or PostgreSQL outage was involved. M5 still needs the wider recovery, anti-cheat and
sustained-load gates.

## T4C-0209 pursuit design checkpoint

`MONSTER_PURSUIT_DESIGN.md` is the reviewed architecture-only plan for authored monster
acquisition, pursuit, leash, reach, path budgets, durable moving combat and peer handoffs.
It was prepared against source through PR132/PR130 and distinguishes that schema-10 source
from the live owner-controlled source-`8da43482`/schema-9 two-client QA realm. No online
pursuit code or runtime test was delivered. M4 remains open.

T4C-0210 is separately reserved for the small offline-preserving `BaseMonster`/pure
`MonsterPursuitRules` extraction and focused test. Its proposed `multiplayer/domain/pom.xml`
inclusion awaits an explicit narrow Claude handoff; do not assume ownership or change it
before acknowledgement. The later moving physical AI phase requires explicit latest-commit
handoffs for `OnlineStore`, `PersistentCreatures`, `PreviewWorld`, `HostedWorlds`,
`OnlineMelee`, `OnlineRetaliation`, `PersistentSessions` and snapshot/protocol interfaces
after Claude's T4C-0204 loot and T4C-0206 quest hooks are ready. Do not start that core
phase from this design alone.
