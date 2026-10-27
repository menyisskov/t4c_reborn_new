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

Two agent families work in parallel. Claim a TASKS.md ID on main (merge the row first) before starting; do not start work another lane owns. Row prefix "(Claude lane)" / "(Codex lane)" marks the owner.

- **Codex lane:** T4C-0196 roster/native account, T4C-0197 DB pool/shutdown, T4C-0198 runtime metrics, their uncommitted files in the main checkout (all of multiplayer/server persistence, SimulationLoop, HostedWorlds, PersistentSessions, OnlineStore), soak/load gates (M5), operator tooling.
- **Claude lane:** T4C-0201 scope + loot design docs (new files SCOPE_PROPOSAL.md, LOOT_DESIGN.md), T4C-0202 M2 loot domain rules (new files under multiplayer/domain only). Server-side loot wiring waits until the Codex lane's pending server changes are merged.
- Unclaimed: M1 native UI, M3 quest service, M4 abilities. Whoever claims first posts the row; ask the owner if both want it.

## Loot groundwork checkpoint (T4C-0201/0202, Claude lane)

Delivered: SCOPE_PROPOSAL.md (all PROPOSED, awaiting owner approval) and LOOT_DESIGN.md; loot tasks L1–L5 (shared MonsterLootTables, CarryCapacityRules, ItemKeys, GroundLootView/LootDropDraft, server OnlineLootDrafts and OnlinePickupRules). Verified by unit tests only (root targeted + multiplayer reactor; PostgreSQL tests skipped); no server wiring, no rendered or native evidence. Remaining: L6 schema, L7 server loot handling, L8 melee wiring, L9 pickup endpoint, L10 native client, L11 corpse weight (needs owner decision), L12 acceptance — L6/L7/L9/L11 wait for the Codex lane's pending OnlineStore/PersistentSessions/PreviewServer changes to merge. OnlinePickupRules.effectiveStrength throws on active buffs; L7 must map to a refusal.
