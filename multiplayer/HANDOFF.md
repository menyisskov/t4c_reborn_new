# Multiplayer implementation handoff

Last reviewed: 2026-10-27, Asia/Jerusalem. This file is the restart point for a different model. Recheck Git and running processes before acting; this describes recorded evidence, not live state.

## Read first

1. `../AGENT.md`, `../AGENTS.md`, `../CLAUDE.md` are authoritative engineering, review and delivery instructions.
2. `../DESIGN_GUIDELINES.md`, section Multiplayer delivery, records lasting owner decisions and implementation contracts.
3. `ACCEPTANCE.md` is the release evidence checklist. Unchecked Alpha/Beta gates remain unfinished.
4. `../TASKS.md` and `../CHANGELOG.md` record shipped work. This document records integration state and what to do next.

The owner is PM/QA and has authorized autonomous implementation, focused tests, PRs, CI, self-review and merges. Use parallel sub-agents for independent tasks with explicit file ownership. Continue assigning the next crucial release item; optional polish does not outrank required gameplay. Never claim Beta complete from service unit tests alone.

## Preserve single-player and personal state

The normal `launch.bat` and offline game entry point must keep working. Online launch is `com.perso.T4C.online.OnlineClient` through `start-client.ps1`; it never initializes offline saves/screens. Online account/character state belongs in PostgreSQL, not desktop character files.

The checkout intentionally contains user changes in account_storage.json, characters.json, characters/*.json, editor_camera_position.json, game_preferences.json, monsters_report.csv and t4c-chat.log. There are also private ignored online profiles, .lwjgl, crash reports and Python caches. Do not broadly stage, reset, clean or stash these. Select files for each delivery. JVM crash reports contain machine details and must not be committed.

A protected-file backup from before multiplayer work exists in the user's temporary directory named `t4c-local-before-merge-0ad2911c72e641bd81173c0978d5f05a`. Hash comparisons have shown zero content differences during this work; do not restore over newer user changes.

## Current implementation

The server is a separate headless Maven reactor (`pom.xml` in this folder) with domain/shared rules and a server module. The simulation owns mutable world state at 34 ticks/sec, caps sessions at 20, validates direction-only movement, authoritative collision/sanctuary/LOS and bounded authenticated request budgets. Starting world is an 80 by 60 crop at world 0, origin 2904,1029. Six authored Olin Haad guards are loaded from the packaged registry.

PostgreSQL is authoritative for accounts, leases, character payloads, balances, operation receipts and creature lives. CharacterProgress v1 is immutable; progression is cumulative XP within the current incarnation. Shared offline helpers supply progression, equipment eligibility/bonuses, weapon damage/timing and combat resolution. Do not trust client damage, coordinates, inventory or rewards.

PersistentSessions uses bounded worker queues and simulation-thread publication. Gameplay results become visible only after commit. Ambiguous writes disconnect; reload committed state after reconnect. Receipts bind operation payloads and durable creature life IDs prevent duplicate kill grants. Creature HP/life/respawn recovery is persistent. One authoritative process per realm is assumed; no distributed world-leader lease exists yet.

Native melee is currently a restricted immediate-impact preview: Space attempts nearest target, with range/LOS/cooldown/revision validation. Existing shared targets publish HP; per-hit/death XP and gold commit with damage. Scripted targets, item-drop targets, advanced effects, ranged/stun attacks and active quest/buff behavior remain unsupported. Authored stationary actors remain protected, matching offline rules; temporary lack of AI movement is not that authored flag.

## Active deliveries and ownership

- T4C-0189 / PR #113: online-only Windows GLFW crash correction. Pinned upstream source, minimal owned-window patch, license, reproducible build, bundled checksum-verified DLL and first-line OnlineClient bootstrap. The single-player native selection is untouched. Two rendered clients survived repeated direct focus activation and Alt-Tab, and combat still responded. Native load/checksum and desktop package/tests passed. Merged as 8ad6c30a after green CI; local runnable package/tests passed.
- T4C-0190 / PR #114, merged as beaf80ec: retaliation scheduler, schema-v8 durable aggro/attack deadlines, transactional monster hits, offline-compatible death penalties/half-HP return, durable owner-recoverable corpses, public committed player HP, protocol v3 and native recovery intent. The actual native two-connection adapter probe passed targeted damage, observer isolation and fatal half-HP return. HTTP corpse recovery ownership/range/identity/retry checks passed. A three-minute twenty-adapter smoke completed with zero connection/invariant/health failures, 185 attack attempts/112 accepted, 46 HP changes and ten reconnects; this is not rendered or two-hour evidence. Green CI, scoped self-review and local rebuild passed; rendered retaliation validation remains pending. Do not label this shipped until integrated tests and delivery are complete.
- T4C-0191, backup verification delivery: consistent-snapshot PostgreSQL backup/restore verification into a new t4c_restore_* database. Tooling restored and matched both schema7 QA and schema8 test databases; independent delivery is being committed/reviewed. Table comparisons alone do not satisfy a restored gameplay playthrough.

Root coordinates shared docs, selective commits and GUI QA. Retaliation agent owns scheduler/PreviewWorld/PersistentSessions; death agent owns OnlineStore/death rules and tests; crash agent moved to backup tooling. Inspect actual live agent status rather than assuming these agents remain available after a model switch.

## Integration contracts under construction

`OnlineStore.listCorpses(session)` is worker-only and owner/lease checked. `recoverCorpse(session, revision, corpseId, world, x, y)` commits exact stored instances/currency and marks the corpse recovered atomically; trusted server position is range checked again in the transaction. Native/server intents contain only revision and corpse UUID. `GET /corpses` exposes only owner summaries; `POST /recover` queues recovery. Never accept client corpse contents.

PlayerView/OnlineConnection.Actor append hp/maxHp under wire protocol v3; the browser diagnostic version must match. The native client refreshes private state by committed revision and refreshes corpse summaries with it. The gameplay command queue remains single flight; no automatic mutation replay after ambiguous failure.

Retaliation binds aggro to durable character identity, not a temporary network token. Movement must continue for other players while writes are pending. Fatal transition/recovery freezes the affected actor; location/HP changes publish only after atomic commit. Logout during death must checkpoint the committed respawn location. Invalid/outside-crop saved respawns need an explicit validated local fallback until map transfer is supported.

## Build and focused validation

Use PowerShell from the repository root with Java 21 and Maven. Avoid `mvn clean` while runtime JARs are open. Root API changes may need full source recompilation (`-Dmaven.compiler.useIncrementalCompilation=true`); partial stale bytecode once caused a misleading NoSuchMethodError, fixed by a full compile.

```powershell
mvn -B '-Dmaven.compiler.useIncrementalCompilation=true' '-Dtest=OnlineNativeLibraryTest,OnlineConnectionTest,DeathPenaltyServiceTest' package
mvn -B -f multiplayer/pom.xml test
```

PostgreSQL tests are enabled only with T4C_DB_URL/T4C_DB_USER/T4C_DB_PASSWORD. Do not treat skipped DB tests as validation. The local WSL Ubuntu PG16 cluster uses 127.0.0.1:55432 with databases `t4c_online` (QA) and `t4c_test` (tests), role `t4c_online`. Read the password privately from WSL `/home/menyiss/.local/share/t4c-online-pg/password`; never print it or include it in command lines, logs or docs. Configure connect/socket timeouts. The PG runtime is under WSL `~/.local/share/t4c-online-pg/runtime/usr/lib/postgresql/16/bin`. Docker is unavailable on this machine; do not change security settings to fix it.

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

## Native QA and desktop state

Use the Computer Use skill and its supported sky API for all Windows UI interaction. The owner stopped desktop automation with the physical Escape key on 2026-10-27 after crash candidate focus checks. Do not resume GUI interaction merely because a heartbeat fires; continue non-UI implementation/tests until the owner resumes desktop testing. Do not claim retaliation was rendered with two clients until that happens.

Before stopping, patched native processes loaded separate t4c-online-glfw-* DLLs, remained connected as two players and responded to attacks. Existing old clients do not acquire a new DLL or new classes until restarted. Window handles/PIDs are ephemeral: rediscover them.

Required next rendered checks: two profiles, provoke guard with one, verify targeted HP changes only, move beyond range/behind wall, reconnect/restart with aggro intact, fatal return at halfHP, owned corpse recovery and no duplicate contents, repeated focus switching and ordinary Alt-Tab. Verify offline launch/save/load separately without mutating personal files merely to test.

## PR delivery

Use ship-pr/steward. Self-review the actual scoped diff. Commit task/changelog/design decisions together, then a small task-hash follow-up. Open PR with GitHub tools and attach it to this chat. Green Build and test on current head plus no conflict and handled real findings permits direct merge; do not wait for a running automated review. Check existing review threads first and respect AGENTS.md one-original-review/fix-verification protocol. Preserve pending independent work when switching main/next branch. Rebuild local checkout after merge.

## Remaining release sequence

Finish current crash/retaliation/death/recovery integration first, then backup restore evidence. Next required joins are secure remote accounts/transport and native character selection, authoritative quests/NPC interaction and ground loot, map transfer, required spells/abilities/inventory controls, full creation-to-progression playthrough, crash/reconnect/database/restore playthroughs, attack/replay/duplication abuse checks, and sustained two-hour 20-client mixed gameplay with recorded tick/DB/latency/resource measurements. Follow ACCEPTANCE.md rather than declaring Beta from this list alone. Deployment credentials or external hosting choices may need the owner; exhaust local work before asking.
