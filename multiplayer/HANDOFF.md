# Multiplayer implementation handoff

Last reviewed: 2026-10-27, Asia/Jerusalem. This file is the restart point for a different model. Recheck Git and running processes before acting; this describes recorded evidence, not live state.

### Online ability-menu paging (T4C-0351)

Implemented on `codex/online-ability-pages-0351`, commit `179cec80`, based on main `acb7f2ce`. Online spell and potion lists now page through all learned, server-supported entries; Left/Right changes pages and 1–9 selects within the active page. Focused tests passed 13/13, root package and `git diff --check` passed. No rendered-client or owner-QA test was run. Local PR integration is next; M4/Beta remain open. T4C-0350 day/night remains in open PR #315, still labeled T4C-0349 pending author renumbering.

## Read first

### Online Sneak parity (T4C-0344)

Implemented on `codex/online-sneak-0344` from `origin/main` 0dcdb164. Learned Sneak now uses the persistent online socket, server-committed concealment/cooldown, moving witness checks and melee/ranged/spell reveal. Focused client tests passed 49/49; focused server tests passed 19 total (0 failures/errors, 6 skipped including database-gated cases); root/server packages and `git diff --check` passed. The PostgreSQL persistence/observer test and rendered-client acceptance remain unverified; no DB, server, desktop or owner QA resources were used. PR #309 is open from commit 392d2ca1. Next: merge after local validation, then arrange database and rendered checks without taking the owner's desktop. M4/Beta remain open.

### Online HUD and completed quest greeting (T4C-0343)

Commit `398cfd25` on `codex/online-ui-quest-0343`, rebased onto main `3e147d8b`. Online clicks now route through the existing HUD actions; a repeated completed `renewed_wards` greeting returns the authored completed line without changing rewards or progress. Focused tests passed: HUD 1/1, offline Warden 2/2, online quest 6/6; root and server packages and diff check passed. No rendered-client test or owner QA resource was used. Next: open/merge the locally validated PR, then coordinate a rendered check without taking the owner's desktop; M1/M3 and Beta remain open.

### Ranged monster attacks (T4C-0320, 2026-10-30)

Implemented on `codex/monster-ranged-0320` from main `c1f941de`. Online pursuit and hostile-clan combat now honor each authored attack's min/max range, preserving line of sight and the melee fallback for physical-only definitions. `OnlineMonsterPursuitTest` passed 8/8; PostgreSQL 16 `OnlineClanCombatTest` passed 1/1 on a disposable cluster that was destroyed. No protocol, schema, client or single-player changes; no rendered M4/Beta acceptance. Next: self-review, local package validation, then open/merge the PR after tests.

### Authored monster self-heal (T4C-0319, 2026-10-30)

Implemented on branch `codex/monster-heal-0318` from main `bc179a76`. Online retaliation now
supports authored 10616 Heal Serious, caps it at authored maximum HP, and atomically persists health,
cooldown, target/life checks and event receipt. A focused PostgreSQL 16 test passed 1/1; the server
package and `git diff --check` passed. Disposable cluster `t4c_acc_d11855a0c9f1` on 56921 was
destroyed. This is server-only evidence, not rendered parity; no M4 or Beta completion is claimed.
Merged as PR #280 at b4e261d7. No rendered-client evidence; M4 and Beta remain incomplete.

### Hostile clan spell damage (T4C-0317, 2026-10-30)

On branch `codex/clan-spell-0317` based on main `7249c3d4`, online clan combat now filters
authored attacks at their declared ranges, rolls supported elemental/armor-damage
spells and applies the same monster-vs-monster combat resolution as offline. The Goblin
Bomberman spell 10219 → Wild Horse PostgreSQL test passed 1/1 and persisted the death/life
identity through restart with no player reward; server package and diff check passed. No
schema/protocol change or rendered-client run. Two earlier failures remain preserved in `%TEMP%`
and are summarized in `ACCEPTANCE.md`. Current task is delivery/review of PR #278; after merge,
continue with the remaining exact gameplay parity gaps, not Beta completion.

### Live rendered gameplay panels (T4C-0312, 2026-10-29)

A second rendered pass drove the inventory (equip/unequip with a restart and rejoin), Light Heal and a healing potion
through C and U, the Sigfried shop (buy, too poor, over-weight, sell), the other player's walking frames and appearance,
and the `renewed_wards` quest kills and turn-in. Results, the unscripted list and nine findings (upside-down panels, raw
NPC key in the shop title, no mana potion, no player appearance/name/health bar, no NPC topic control, overlapping NPC click boxes, the busy-click retry
fixed here) are in [CLIENT_UNIFICATION_DESIGN.md](CLIENT_UNIFICATION_DESIGN.md) 3.16.
### Clean-machine rehearsal (T4C-0311, 2026-10-29)

Both QA packages were rebuilt from `f3d4ed62` and rehearsed in a scrubbed environment on this machine (not a second machine); four Windows
PowerShell 5.1 packaging defects were fixed and the remaining second-machine checklist is in [MILESTONES.md](MILESTONES.md) M6. Rebuild with
`package-online-client.ps1 -Name <new>` and `package-online-server.ps1 -Name <new> -SourceRevision <sha>`; both now run under stock 5.1.

### Latest live rendered evidence (T4C-0306, 2026-10-29)

Two real rendered windows were run against a real server on a disposable PostgreSQL cluster with the test-scope
`OnlineLiveRunDriver` and `multiplayer/tools/LiveRunProvision.java`: lobby selection, mutual sight, Kilhiam talk, a graceful
server stop/restart with the disconnect-to-lobby rejoin, Skeleton combat, a drop and a pickup race. What was and was not
driven, the reproduction recipe and seven findings (dark default light, frozen owner-window countdown in the public loot
view, simultaneous-talk refusal, generic race-loser text, the unneeded 33 s hold after a restart) are in
[CLIENT_UNIFICATION_DESIGN.md](CLIENT_UNIFICATION_DESIGN.md) 3.15. Next for rendered acceptance: equip and inventory
refusal, a scripted death/corpse recovery, spells, the quest loop and travel, a forced-kill restart, then the sustained mixed session.

### Current gameplay slice: caster character creation (T4C-0272)

The online create form and authenticated server endpoint now offer Warrior, Paladin, Cleric,
Healer, Mage and Battle Mage. Starting attributes, equipment and spells come from the shared
`CharacterClass`/`OnlineCharacterCreator`; server class resolution explicitly rejects Archer
until ranged attacks are supported. Focused local form, kit and class-resolution tests passed;
the authenticated creation integration tests passed 2/2 against a disposable PG16 cluster,
which was destroyed and its port released. No desktop or owner QA resource was used. This is
not full character parity or Beta acceptance. Next gameplay gaps remain ranged combat, broader
monster/NPC/shop coverage, spells and the remaining online systems; inspect `PARITY.md` and
the live peer register before claiming the next task.

1. `../AGENT.md`, `../AGENTS.md`, `../CLAUDE.md` are authoritative engineering, review and delivery instructions.
2. `../DESIGN_GUIDELINES.md`, section Multiplayer delivery, records lasting owner decisions and implementation contracts.
3. `MILESTONES.md` is the owner-facing stage/six-milestone roadmap with exact completion criteria. `ACCEPTANCE.md` is the release evidence checklist. Unchecked Alpha/Beta gates remain unfinished.
4. `../TASKS.md` and `../CHANGELOG.md` record shipped work. This document records integration state and what to do next.

The owner is PM/QA and has authorized autonomous implementation, focused tests, PRs, CI, self-review and merges. Use parallel sub-agents for independent tasks with explicit file ownership. Continue assigning the next crucial release item; optional polish does not outrank required gameplay. Never claim Beta complete from service unit tests alone.

**Current owner decisions:** Beta and GA require two **real rendered clients** for mixed gameplay/stability evidence. The owner removed the fixed two-hour minimum on 2026-10-28: record actual session duration, but there is no duration threshold. Preserve older 20-adapter measurements and failures as historical evidence. The existing server capacity remains 20. All other intended launch content/gameplay, anti-cheat, security, recovery, operations, distribution and owner QA gates remain open until verified. The starter checkpoint alone does not complete Beta; 20-player qualification is follow-up scale evidence.

## T4C-0212 moving pursuit integration checkpoint

The reviewed schema-11 candidate activates server-authoritative patrol, natural and hit-provoked pursuit, bounded collision-aware pathing, safe-area/leash return and guarded moving-creature persistence. It preserves the existing protocol-5 snapshot path for native creature motion. Immutable committed base `3ac9c0aadc7463043436f67ef9e5623364962ed1` plus 16 exact owned files matches `%TEMP%/t4c-0212-integrated-check-df7eb51c02004a039fe3413c041438c2/candidate-hashes.tsv` (SHA-256 `251D90B63649936738F4E84872E5760A1A30D21BA508051FA9007658EA9EA2C5`); clean isolated PostgreSQL multiplayer suite passed 182/182. The frozen server JAR hash is `4DB9A06E71F3B82080B07B78E7934EE1B97514A03BCE667E14B4ED376BB2D737`.

The final [native pursuit probe](tools/NATIVE_PURSUIT_PROBE.md) exited 0 on fresh `t4c_pursuit_0212_e827fc99a23e`, protocol 5/schema 11: accepted guard hit, HP 55→28, 28.455 px pursuit closure on stopped A over six advancing settled snapshots/530 ms, B state unchanged, durable RETALIATION checkpoint revision 5/epoch 0, exact A private rejoin after real lease release, then same guard life/HP/pose and both committed private states after owned graceful restart. Both owned server shutdowns exited 0 and 18771 was free. The copied client JAR SHA-256 is `754F14BAF1552A841C099DDABBA39479CCD71D1418642A0A66C0504896C0CB09`; runtime 16-source manifest SHA-256 is `B90609CFE404CE93C8322D3479707A2463343ED0383913461164F7CBAB039B6F`; sanitized report `%TEMP%/t4c-0212-native-runtime-1bdb4b74/output-08/report.txt` SHA-256 is `F7288489F3CE18A8EED3BFE9F7D7FEF740C631BF11843FE2E5E9B9942383FC32`. Preserve seven earlier failed fixture/probe runs and their distinct databases/reports. The separate optional 20-adapter smoke stopped after ~66 seconds on a Windows harness report `AccessDeniedException`; its 180-second/reconnect stages did not pass and its partial occupancy/performance numbers are in ACCEPTANCE. T4C-0220 makes 20-player scale a follow-up, while the two-real-rendered-client mixed run and other Beta gates remain.

After delivery, return the four temporarily loaned `OnlineStore.java`, `PersistentSessions.java`, `HostedWorlds.java` and `OnlineMelee.java` to Claude with the exact merged main commit, schema-11 moving-state API and this test evidence; do not assume return from a passing candidate alone. The native PASS predates merged native UI task 0218, so current-head CI and later rendered pursuit QA remain separate. Owner QA18765 and the existing desktop windows were not replaced or driven for this probe. No full M4, M5, Beta or remote-completion claim is made.

## T4C-0219 live account-key rotation checkpoint

`tools/NativeKeyRotationProbe.java` tested two actual native adapters on an isolated
build of committed `130bc3c0389aa08f801c8c69f8f11abdc32c724d`, protocol 5/schema 10.
The final source SHA-256 is `3FB42EA1628B924B6039318E6B54DF1D4E56C27FEDC914458214BB2C9DA37448`;
server/client JAR SHA-256 values are `D5BBD75EAAC7E19AF976B316BA6FCFBD5321FC0803E604BB29760CB18A9F950C`
and `EEF5FDD51199487D6F14F4B16686154A82C9168A22E81049AD1E87CE4E8D808B`.
The immutable source/build/runtime is in
`%TEMP%/t4c-0216-final-130bc3c0-ef9e7e43828444d9833675f04e654ba9/rotation-runtime`.
An isolated client/server package and `javac -proc:none` passed. With newly created
`t4c_rotation_0218_312c3ec2435a` on existing PG55432 and owned loopback 18775,
the probe exited 0: rotating A fenced its active writer, old-key roster/join/native
rejoin were refused (401/409/no view), replacement credentials rejoined the same
committed character, and B kept its session, progress and movement. The owned
server reported the expected persistence conflict, drained and exited 0; read-only
queries found schema 10, two accounts, two characters, zero held leases and port
18775 free. Sanitized report SHA-256 `2F83E9DE19FB6473339FFC11627568FB14CB6F5FC7BA11E2AA7AC3D630A68569`:
`%TEMP%/t4c-0218-evidence-07fecbb11bff4844a72c1711703fc745/report.txt`.

Preserve the first failed probe/database: `t4c_rotation_0218_91def691bf71` and
`%TEMP%/t4c-0218-evidence-71076f38fd124617862a1ed4e4c8ab37/report.txt`
(SHA-256 `B8625B3C08B61E4D5D17F7FFB5BC5DCBC67EA50BA947ABDC7ADD3515F6299D12`).
That probe expected HTTP 401 from direct join rather than the actual Store-conflict
409; its early-abort shutdown exited nonzero while revoked A was still draining.
The corrected full run waited for disconnection and stopped cleanly. T4C-0219 was
renumbered from its original 0218 reservation after the separate native UI PR used
0218; immutable DB and report labels remain historical. Keep private control and
database credentials out of Git. This is a local adapter case, not rendered,
foreign-ownership, remote or complete M1/M5/Beta evidence; see
[NATIVE_KEY_ROTATION_PROBE.md](tools/NATIVE_KEY_ROTATION_PROBE.md) for the replay command.

## Preserve single-player and personal state

The normal `launch.bat` and offline game entry point must keep working. Online launch is `com.perso.T4C.online.OnlineClient` through `start-client.ps1`; it never initializes offline saves/screens. Online account/character state belongs in PostgreSQL, not desktop character files.

The checkout intentionally contains user changes in account_storage.json, characters.json, characters/*.json, editor_camera_position.json, game_preferences.json, monsters_report.csv and t4c-chat.log. There are also private ignored online profiles, .lwjgl, crash reports and Python caches. Do not broadly stage, reset, clean or stash these. Select files for each delivery. JVM crash reports contain machine details and must not be committed.

A protected-file backup from before multiplayer work exists in the user's temporary directory named `t4c-local-before-merge-0ad2911c72e641bd81173c0978d5f05a`. Hash comparisons have shown zero content differences during this work; do not restore over newer user changes.

## Delivered — T4C-0323 online player healing (PR #285)

Branch `codex/player-heal-0323`, based on `1d8820f` (PR #284/main). Learned single-target heals now target another live actor through the persistent socket; caster cost and recipient HP commit in one receipt-bound PostgreSQL transaction. Focused service and DB-backed socket-route tests pass; root command/socket tests passed 31/31 and 11/11. A parser regression requiring a space before `a:` was fixed. PR #285 merged as b66f12e0. Source/test files are released. No two-rendered-client heal validation or M4 completion is claimed; next is rendered two-player validation when owner desktop permission is available.

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

Use ship-pr/steward. Self-review the actual scoped diff. Commit task/changelog/design decisions together, then a small task-hash follow-up. Run and record applicable local tests against the proposed head before opening a PR; attach the PR to this chat. Once local validation passes, there is no conflict, and real findings are handled, merge directly without waiting for GitHub Actions or automated review. Do not bypass enforced branch protection; report if GitHub blocks a merge on a pending required check. Check existing review threads first and respect AGENTS.md one-original-review/fix-verification protocol. Preserve pending independent work when switching main/next branch. Rebuild local checkout after merge.

## Remaining release sequence

Map-transfer delivery is merged and reliability regressions pass; diagnose the new SQL08001 load failure without mutating its captured runtime, then rerun after a verified fix. The opening NPC service is merged; complete its rendered interaction and native character selection/account lifecycle, quests/general ground loot, required spells/abilities/inventory controls, broader hosted world routes and encounters, full creation-to-progression playthrough, crash/reconnect/database/restore playthroughs, and attack/replay/duplication abuse checks. Run mixed gameplay with two real rendered clients on the final integrated release, recording session duration and tick/DB/latency/resource measurements; no fixed duration minimum applies. Qualify 20-player scale separately as follow-up evidence. HTTPS configuration exists, while external deployment and native remote QA remain open. Follow ACCEPTANCE.md rather than declaring Beta from this list alone. Deployment credentials or external hosting choices may need the owner; exhaust local work before asking.

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
checkpoint alone does not complete Beta under the owner's later two-rendered-client concurrency
decision. Scope choices and unverified checks are explicit. Do not infer a release date or
completion percentage from task counts.

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
immutable long runs failed and were stopped; no passing integrated rendered-client mixed-session result exists. Do not restart another
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
Beta or complete mixed rendered-client gameplay evidence. Delivery follows the current local-validation merge policy in `AGENTS.md`; CI may finish after merge. Current QA metrics server
PID52688 runs the matching persistent build; verify current metadata before actions.

## Loot groundwork checkpoint (T4C-0201/0202, Claude lane)

Delivered: SCOPE_PROPOSAL.md (all PROPOSED, awaiting owner approval) and LOOT_DESIGN.md; loot tasks L1–L5 (shared MonsterLootTables, CarryCapacityRules, ItemKeys, GroundLootView/LootDropDraft, server OnlineLootDrafts and OnlinePickupRules). Verified by unit tests only (root targeted + multiplayer reactor; PostgreSQL tests skipped); no server wiring, no rendered or native evidence. At this earlier checkpoint, remaining work was L6 schema, L7 server loot handling, L8 melee wiring, L9 pickup endpoint, L10 native client, L11 corpse-item recovery and L12 acceptance. The later owner L11 decision and current file holds are recorded below. OnlinePickupRules.effectiveStrength throws on active buffs; L7 must map to a refusal.

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

## T4C-0210 shared pursuit-rule checkpoint

The offline `BaseMonster` now calls pure `MonsterPursuitRules` for existing acquisition,
leash and reach decisions. The exact three-file delivery includes a focused new rules test;
a fresh isolated source snapshot passed 16 focused offline tests with zero failures/errors/skips
and targeted Spotless on those three files. The global formatting check has pre-existing
unrelated failures and was not used as the gate. See the retained private logs under
`%TEMP%/t4c-0210-final-source-b3a4fe4d2ece429e8306f6631d84b0f5/`.

This is offline behavior-preserving groundwork only. Claude explicitly acknowledged the
additive `multiplayer/domain/pom.xml` rule/test includes; this delivery adds only those two
entries and must retain Claude's other POM entries during integration. A create-only isolated snapshot of main0fb90aeb plus the four scoped files passed focused domain `MonsterPursuitRulesTest` 2/2; log `%TEMP%/t4c-0210-domain-ack-test.log`. No online creature pursuit
is running. T4C-0211 separately reserves snapshot-driven native facing/walk rendering in
`OnlineClient` plus a new motion helper/test; it does not claim server movement. Future P2
server motion still needs coordinated latest-commit file handoffs after Claude's loot,
quest and protocol hooks. The live source-`8da43482`/schema-9 QA clients are untouched.

## T4C-0208 isolated PostgreSQL recovery checkpoint

The four new tools in `tools/NATIVE_DATABASE_RECOVERY.md` create an owned PostgreSQL 16
cluster on loopback 55433 and a copied server on 18768. The exact final Java SHA-256
`94099C3B19382BD126F5B51AFD60CFFB4DEB3034F7B8A93B681AA9E22974CDB3` passed
on preserved source-`8da43482`/protocol-4/schema-9 binaries. A real postmaster crash
changed its PID/start and PostgreSQL logged automatic recovery; schema 9 was queried
before and after. Two native adapters rejoined: A kept its original letter/full progress
and balances; B's one raw outage request returned `503 busy` without replay and B gained
its distinct letter only from a new explicit action. The owned game server exited 0.
Final sanitized private report: `%TEMP%/t4c-0208-evidence-t4c_db_0208_dacdb459af35/report.txt`.

The final helper SHA-256 `8CB456589B775A10E0E3975B999E4EC2685559717D67CD8A8266A542638EEDC7`
also passed a controlled partial-create failure fixture: exit 77 stopped only its
owned postmaster, left `PG_VERSION` and logs for inspection, and released ports 55433/18768.
Earlier failed and preliminary runs are retained; see the tool document for their causes.
The cluster is down and the shared PG55432/QA18765/owner clients were untouched. This
does not establish current schema-10, rendered, backup-restore or broad M5 acceptance.

The unchanged T4C-0208 tools subsequently passed on a clean committed-main `0fb90aeb`
source archive with matching client/server binaries, protocol 4 and measured schema 10
before/after actual PostgreSQL crash recovery. Server JAR SHA-256
`FC54A617660D0DDEAD2757FE59C0B3EA205DF640A49A33E799AEC964C71BEEC8`, client JAR
SHA-256 `C7CAA3512E24442C7641044520B5E543522C3C5987590FB9C64DD8D8DB751890`.
The single outage request returned `503 busy` without replay; A kept its letter/full
progress and B gained a letter only after explicit post-recovery native action. Both
adapters rejoined, the owned server exited 0, the cluster is down and ports 55433/18768
are free. Private report:
`%TEMP%/t4c-0208-evidence-t4c_db_0208_db5ba020be71/report.txt`. This is still a
narrow adapter drill, not rendered or broad M5 acceptance.

## T4C-0211 native creature motion checkpoint

`OnlineClient` now maps authoritative creature snapshot position deltas to existing
`MonsterMovement`/`MonsterAnimations` facing and walk cycles. `OnlineCreatureMotion`
idles on first spawn, region reset and disconnect, with no interpolation or predicted
movement. A fresh isolated archive from committed main `0fb90aeb` plus only these three
files compiled 8,665 desktop and 210 test sources; the focused test passed 1/1.
Private log: `%TEMP%/t4c-0211-motion-7e2d32a3645649e3b3070a07db489875/focused-test.log`.
The live owner-controlled source-`8da43482`/schema-9 clients were not replaced.
No server pursuit is active or rendered movement verified. T4C-0212 reserves a separate
new server controller/test slice; existing moving core files remain conditional on
explicit Claude handoff, so neither task completes M4.

## Loot and quest server checkpoint (T4C-0204/0206, Claude lane)

Merged and CI-verified (including PostgreSQL tests): schema v10 ground loot and receipt-bound atomic claim (PR128); region loot cache, pickup, expiry sweep, lethal-kill drops, looted monsters attackable (PR130); spell/quest catalogs and quest drafts (PR132); self-cast and potion drafts (PR134); quest flag allowlist and Warden Aelric greeting (PR135); quest turn-in commits gold/XP/item/spell/flags once (PR138); protocol 5 `/pickup`, `loot` in `/state`, `/interact` topics (PR142). At this checkpoint, native loot/dialogue controls L10, NativeLootProbe L12, `/cast` and `/use` A7, regeneration A5, renewed_wards Q10 and corpse-item recovery L11 remained open. The later owner L11 decision and current file holds are recorded below. This checkpoint is backend/CI evidence, with no rendered-client M2 or M3 evidence. SCOPE_PROPOSAL.md remains unapproved.

## Current peer coordination after PR145 (T4C-0203 follow-up)

Claude PR144 released PreviewServer, PreviewServerTest and OnlineProtocol after the shared HTTP/protocol work; PR145 delivered native loot controls, but its OnlineClient hold has no recorded release. Claude still holds OnlineStore, PersistentSessions, HostedWorlds and OnlineMelee. PersistentCreatures is reserved for Codex but not edited.

T4C-0212 has six WIP runtime files held by client_package and two new WIP acceptance tools held by restart_recovery; no functional server-pursuit merge or behavioral native probe pass exists. The owner-controlled QA18765/source-8da/schema-9 two-client session remains untouched. Independently, the retained T4C-0198 pooled baseline is running on server18770 and a dedicated t4c_soak_0198_4e09eaef1c6a database; its 7200-second report is pending, not a passing Beta result. The T4C-0208 isolated cluster ports55433/18768 are stopped and available. T4C-0212 future port18771/new database is reserved but not started.

The coordinator's A5 regeneration default follows the existing offline Player.java behavior: regenerate only while a connected living authoritative player is actively updated, use shared RegenerationRules cadence, re-anchor on join, and do not catch up logged-out time. This routine parity choice does not approve proposed starter classes/content or alter the separate owner-approved corpse-item pickup decision.

Owner L11 decision (superseding earlier pending language): online corpse items use individual pickup within normal carry limits and may be recovered partially per item; single-player behavior stays unchanged. The current priority is cohesive moving server pursuit. Codex requests a temporary exact-commit loan of Claude-held OnlineStore, PersistentSessions, HostedWorlds and OnlineMelee before L11 mutates them, with return after P2; Claude has not yet acknowledged the loan. The coordinator A5 regeneration default remains technical offline parity, not a new owner class/content approval.

## Pursuit continuation while peer handoff is pending (2026-10-27 13:26 +02:00)

Database recovery delivery0208 is merged (PR139), including actual clean0fb90aeb/protocol4/schema10 crash/recovery evidence; see tools/NATIVE_DATABASE_RECOVERY.md and ACCEPTANCE. Native creature motion0211 is merged (PR143); it does not activate pursuit.

P2 task0212 remains WIP: client_package holds PreviewMap/PreviewWorld/OnlineRetaliation/PreviewWorldTest plus new OnlineMonsterPursuit/OnlineMonsterPursuitTest; latest isolated map/world/controller checks15/15. restart_recovery holds only NEW NativePursuitProbe.java/NATIVE_PURSUIT_PROBE.md, isolated javac passed but no behavioral run. Do NOT stage or merge these as a partial enabled feature. Claude still holds OnlineStore/PersistentSessions/HostedWorlds; OnlineMelee handoff is also explicitly requested. Current shared main42662f33 publishes the temporary-handoff request and owner-approved individual corpse pickup within normal carry limits. No explicit core release has been received yet; do not infer release or take over idle files.

Root has pre-authorized client_package's cohesive persistence/AI integration as soon as an explicit latest-commit Claude handoff is recorded. Trigger a followup task on that agent (messages to a completed agent do not start work). Read MONSTER_PURSUIT_DESIGN and live claims, reserve schema11 only if still unused, keep current-position combat/loot/quest transactions and actor checkpoints atomic, then isolated PostgreSQL/native-adapter checks and rendered two-client acceptance. QA18765 and its two old8da/schema9 rendered clients belong to the owner; no restart/migration/desktop takeover without coordinating.

Retained0198 pooled baseline continues independently until about14:57 Asia/Jerusalem: supervisor55484, server59124/18770, adapter66808; private run directory TEMP/t4c-0198-soak-t4c_soak_0198_4e09eaef1c6a. Check status.json and soak-report.json read-only. It is NOT yet passed and is not final mixed-gameplay/rendered Beta acceptance. The supervisor stops only its own server and checks exit0. Preserve all private profiles/control and reports; never stop the shared PG service.

## Live schema-10 backup verification (T4C-0213)

The original T4C-0191 verifier failed against a running source-`0fb90aeb`/protocol-4/schema-10 soak database because PostgreSQL printed one restored `online_ground_loot` CHECK with an equivalent cast placement. Retain failed restore `t4c_restore_a58c46eaa2` and diagnostic restore `t4c_restore_9ab5f3b826` plus their private archives/logs. The diagnostic showed matching COPY row counts and hashes for all eleven tables but did not produce an official passing verifier report.

T4C-0213 now checks exact table/column/default/primary-key/non-CHECK definitions first, then reparses only differing CHECKs in empty temporary clones inside the **new restore database**, rolls back those clones, and still compares every ordered COPY hash. The real PostgreSQL regression accepted the observed equivalent expression and rejected weakened/missing CHECKs and a changed non-CHECK constraint; four focused tests passed. Final-source create-only restore `t4c_restore_d0ef4b8e96` passed with all eleven tables under one exported source snapshot. Private WSL report: `/home/menyiss/.local/share/t4c-backup-verify/t4c_restore_d0ef4b8e96/verification.json`; archive SHA-256 `54815c6bb028b5c68576ac3e77b67f05cbf27efd0bc31b3e016f3a69c003cb12`. Ground-loot and loot-claim rows were both zero. A prior passing pre-final-source restore `t4c_restore_39f0ecaa5b` remains retained alongside the failures. Source soak /health ticks advanced and healthFailures remained zero. At PR148 merge, no restored native adapter/client run or populated-loot recovery had been done; the later adapter read is recorded below, while M5 and Beta remain open. Keep the two-hour baseline, owner QA and shared PostgreSQL service untouched.

### Separate restored native-adapter read after PR148

An isolated follow-up used the same verified `t4c_restore_d0ef4b8e96` database and copied committed source `0fb90aeb` (protocol 4/schema 10) runtime: server JAR SHA-256 `FC54A617660D0DDEAD2757FE59C0B3EA205DF640A49A33E799AEC964C71BEEC8`, client JAR SHA-256 `C7CAA3512E24442C7641044520B5E543522C3C5987590FB9C64DD8D8DB751890`. The private run used only port 18772 and `%TEMP%/t4c-0213-restored-read-fbc0096319*`; no source database, baseline server, QA client or repository runtime file was changed. Two restored world-0 characters with no `online_spawns.target_character` references were selected from 16 eligible profiles; copied private profile hashes still matched their source files. Roster reads found their restored writer leases expired by real elapsed time, with no manual clear.

The final TEMP-only `RestoredRealmReadProbe.java` SHA-256 is `E5A947A78CCA3CAA26C461907E2FA362F2B7705F70628C27F8E0A662B9D0325B`. It was compiled with `javac -proc:none -d <TEMP-run>/probe-classes4 -cp "<copied-client-jar>;<copied-server-jar>;<copied-server-lib/*>;<copied-client-lib/*>" <TEMP-run>/RestoredRealmReadProbe.java`, then run with `java -cp "<probe-classes4>;<same-copied-runtime-classpath>" com.perso.T4C.server.RestoredRealmReadProbe <copied-runtime> <TEMP-attempt3>` while private `T4C_DB_URL` pointed **only** to `jdbc:postgresql://127.0.0.1:55432/t4c_restore_d0ef4b8e96`, with credentials passed in process environment. The fixture hard-guarded that database/port and stopped only its own child through private admin shutdown.

Both actual `OnlineConnection` adapters joined and confirmed region readiness. Complete private progress, gold, XP, world, epoch and global position matched the stored restored state; the public actor is region-local, so the fixture added the map origin before comparing positions. Normal leave kept those fields unchanged at revisions 903 and 847. Its owned server exited 0; port 18772 was free. Sanitized report: `%TEMP%/t4c-0213-restored-read-fbc0096319-attempt3/restored-native-read.txt`. Preserve the first failed `javac` log and two failed TEMP runtime attempts. The initial runtime assertion was a fixture coordinate-frame error; no game or backup-verifier source was changed to make the check pass. Source-soak ticks advanced 139079→142478, healthFailures stayed zero. This is a two-adapter **read** of the restore, not a rendered or complete gameplay/reconnect playthrough. The backup report still says `native_playthrough_verified=false`, and populated loot plus the full M5/Beta gate remain open.


## Static integration blocker: NativeLootProbe147 (a71fed80)

Bounded read-only RCA confirmed the new probe provisions world0 (3700.5,1450.5), while its owned ServerMain --online uses only startingSlice world0[2904,2984)x[1029,1089) and world1[264,344)x[353,413). No fixture override exists. Store creation permits those finite coordinates, but native join then returns409 unhosted_region and releases the failed lease; no loot check can run. No runtime was started and no peer file was edited. The nearest authored skeleton is also about98physical pixels from the fixed fixture, outside64px player melee reach, and this probe never walks. Proper future validation requires actual authored hosted route/content plus collision-valid movement, not weakening region/range guards or synthetic warps. Keep L12/M2 unverified until a valid candidate is actually run. This is continuation evidence, not a new PR review or a claim of an original automated finding.

## Authored starter-route hosting design (T4C-0214)

[HOSTED_STARTER_ROUTE_DESIGN.md](HOSTED_STARTER_ROUTE_DESIGN.md) is a reviewed **design-only** artifact from a read-only audit of committed main `42b32fea` (protocol 5/schema 10). Its minimum contiguous candidate world-0 region `[2735,2984) × [1029,1146)` is 249 × 117 tiles, below the current 256-axis cap. Static terrain/LOS analysis found 19,539 cells reachable from the temple and legal routes to Aelric and all 13 authored quest Skeletons; no map was expanded. Among 39 exported spawn records in this crop, 38 are resolved ordinary actors and Sundial belongs to the NPC system, but the current monster exporter/world constructor rejects it as unresolved. The design records a narrow ownership-classification handoff, existing portal16/17, explicit refusal for unhosted authored routes, saved global-coordinate/rollback concerns, and modeled 166,878-byte map plus 6,289-byte creature-view payload. Actual runtime serialization, load, native walking/loot/quest checks and rendered two-client acceptance have not run. TEMP diagnostic command was `python C:/Users/menyi/AppData/Local/Temp/t4c-0214-route-audit-20261027.py`; source SHA-256 `F7020D104319E8EB93579D7590C3C075057652D2CA5B8934EBFBC3876BB519E6`, final JSON report SHA-256 `35857CD3BE9F4D2DC4CC34D174721B2C2C0A8CCF83D3DFE384CB605109A218BA` (private TEMP paths and full input hashes are in the design doc).

This proposal does not approve starter classes, enable the route, fix the unhosted PR147 loot-probe fixture, grant Claude-held moving-core files or complete M2/M3/Beta. T4C-0215 separately reserves four **new** server-package files for an active cheap implementation agent; do not stage its work in this design/evidence delivery. T4C-0212 pursuit remains WIP until explicit peer handoff and cohesive runtime validation; owner QA18765 and the retained0198 baseline remain untouched.

## T4C-0215 local server QA package

[SERVER_DISTRIBUTION.md](SERVER_DISTRIBUTION.md) describes the create-only local-loopback operator bundle, not a remote production deployment. Four new files are package-online-server.ps1, ServerPackageCheck.java, test-online-server-package.ps1 and SERVER_DISTRIBUTION.md. The final qa-0215-42b32fea-e package came from exact committed source 42b32feaa9f052da8a64d435132c0d7703c98c4d (protocol 5/schema 10) in an isolated TEMP build. SHA256SUMS.tsv SHA-256 ED4B65F5B9C7EADBC938A574D5858DEB64893D0BC39B76B1AA39CBD970FFF7CB; server JAR SHA-256 ECC14BC34BB0F310E0F06B02072559A7FF490DA7EBDA99B19284AAFAFA663168. Seven pinned runtime libraries, two starting-slice collision maps and generated scripts were checked; 14 files plus manifest facts, no private state in bundle. Preserve earlier packages and failed parser diagnostics.

Fresh isolated Maven/content checks and valid/private/tamper/duplicate/traversal fixtures passed. A complete copy to a TEMP path with spaces passed generated preview start, loopback health and exact-process stop. A separate copied final bundle on owned port18773 and fresh private t4c_serverpkg_0215_342a68c2e6ed database passed generated persistent start, local-persistent health, actual online_schema10 and 11 online_* tables, PowerShell 5/7 check, private generated shutdown with readable exit0; port18773 free afterward. Private state/control/logs and database are retained outside the bundle; the DB and test window were released, not reused. Owner QA18765, t4c_online/t4c_test, retained0198 baseline, root targets and GUI were untouched. The package does not prove clean-machine, remote TLS, gameplay, rendered clients, M6 or Beta. T4C-0212 moving pursuit remains WIP, and Claude's core file handoff is still requested, not granted.

## Retained T4C-0198 two-hour baseline assessment

Read-only assessment of %TEMP%/t4c-0198-soak-t4c_soak_0198_4e09eaef1c6a/soak-report.json (SHA-256 76B0DD1C0B1A29E06CAEF09073685FFDE94C2BE90227B74AC9796C8AC62E03F7) and status.json (SHA-256 FF53DB2F7DB08134F72A9575C0B7D78CC3982F4810B8E092F84016050C56FDF1). Source 0fb90aeb/protocol4/schema10 runtime and harness hashes matched retained metadata; no private control/profile contents were read. The 7,200.031-second run exited adapter/server0 with all three owned PIDs gone and port18770 free, zero health/metric/player failures. It held all 20 connected for only 58.213 minutes (48.511% of elapsed); 19 connected for61.768 minutes, with1,151 intentional reconnects. Activity was attacks/recoveries/movement/HP, no quest/loot/travel. Observed 33.999Hz/244,720 completed ticks, two over-budget ticks, one schedule reset, max debt134.751ms, 1,428 metric samples, peak used heap612.172MiB and average processCPU10.889% of one core. All6,469 recorded command latencies <=1s; DB rollbacks rose0→56 without attribution. See LOAD_METRICS.md for exact counters and limitations. Preserve prior failures/short tests and raw ignored logs. This is not continuous20 occupancy, rendered, final mixed-gameplay Beta or M5 pass; bandwidth and DB-operation latency remain unmeasured. No DB/process/ownerQA/Git actions were taken in assessment; only scoped documentation is prepared for root review.

## Local native TLS acceptance (T4C-0216)

[tools/NATIVE_TLS_PROBE.md](tools/NATIVE_TLS_PROBE.md) records a two-actual-OnlineConnection local HTTPS check from immutable source130bc3c0389aa08f801c8c69f8f11abdc32c724d (protocol5/schema10). Isolated desktop/server package and javac probe compile passed. Probe/server/client JAR SHA-256 values: 1344B1DDFBAEB1182A89546CCBD06997F30DBB5DEABF72E72453F9F83DA40ABE / D5BBD75EAAC7E19AF976B316BA6FCFBD5321FC0803E604BB29760CB18A9F950C / EEF5FDD51199487D6F14F4B16686154A82C9168A22E81049AD1E87CE4E8D808B. Owned fresh t4c_tls_0216_fe2b78abb682 on existing PG55432 and loopback18774 only. Standard JSSE explicitly failed unknown-CA and wrong-hostname handshakes, then both native adapters disconnected with no private view or lease. Trusted adapters joined, observed each other and authoritative movement, checked committed global position and reconnected both identities. Owned server private shutdown exit0, port18774 free, database/fixture retained outside checkout. Sanitized TEMP/t4c-0216-evidence-19aceb89c0b1400f9fbfb63c8fd5079f/report.txt SHA-256 CFAC272C3085DFB5542A8FCD9E804E51546418BB3891545BEF03FC965527AACC. No OS trust change or owner QA/GUI touch. Lack of credentials in refused native HTTP join is inferred from TLS handshake failure plus lease absence; no packet capture. This is local adapter evidence, not remote trusted-host deployment, rendered two-client play, complete M1/M5 or Beta.

PR152 Claude starter kits T4C-0217 is now merged main91296e12. Its separate content and approved scope are not part of T4C-0216 runtime evidence. The owner-authorized PR152 core-file handoff comment remains a request without a recorded Claude ACK; Codex T4C-0212 held-core edits stay blocked pending explicit handoff.

## T4C-0212 brief rendered pursuit and reconnect follow-up

PR157 merged the moving-pursuit implementation at main `d4dc908a` with 182 isolated PostgreSQL tests and the separate two-adapter checkpoint/restart PASS above. Under the owner's resumed desktop grant, root observed two actual rendered game windows from frozen base-`3ac9c0aa` client JAR `754F14BA` and server JAR `4DB9A06E`, protocol 5/schema 11, on the retained dedicated `t4c_pursuit_0212_e827fc99a23e` database and loopback 18771. Newly provisioned D pressed Space while the authored guard displayed its preexisting HP 28/55; this rendered run did not measure a new guard-damage change. Root saw rendered guard movement and targeted retaliation (D HP 34→33→12); C's HP 33/gold 243 stayed unchanged. A brief A-key tap moved D. Root closed D normally, left C connected, then reopened D's same external profile after real lease expiry. D rendered HP 12/34, gold 236, XP 0 and its saved location (2931.0864,1040.5; committed revision 56); two clients were connected again. No focus crash was observed. The two windows and server PID 45824 were left running for owner QA; they now belong to the owner and must not be stopped or driven by agents without a fresh handoff. Existing QA18765 stayed healthy and seven protected account/character/preference files had zero hash changes at 17:39 (private TEMP baseline `t4c-0212-rendered-protected-before.json`). This was a short local rendered check on the earlier approved visual assets; no long route, all facings, reset/death/respawn, rendered server restart, quest/loot, remote or full M4/M5/Beta pass is claimed. The bounded native adapter pursuit/restart probe separately measured guard HP 55→28.

T4C-0221 then corrected the Windows native-adapter soak report writer: retry transient atomic-replacement access denial for at most two seconds without deleting the prior valid JSON, and fail visibly if the denial persists. The new focused Java/real-Windows-FileStream fixture passed brief-lock recovery and bounded persistent-lock failure under normal machine PowerShell policy. Exact writer/probe/log hashes and private TEMP log location are in [ACCEPTANCE.md](ACCEPTANCE.md). The earlier schema-11 optional 20-adapter run still failed at about 66 seconds; no 180-second or two-hour rerun occurred, and no M5/Beta gate is closed.

## T4C-0223 starter encounter authority architecture

T4C-0223 separately delivers [STARTER_ENCOUNTER_AUTHORITY_DESIGN.md](STARTER_ENCOUNTER_AUTHORITY_DESIGN.md)
and corrects the stale Beta concurrency sentence in SCOPE_PROPOSAL.md. Root
reviewed the architecture-only, read-only audit (main `55de14ce` and pursuit
merge `d4dc908a`, protocol 5/schema 11). Design SHA-256
`0A5B4A55C78E02F0EF160F96ADF6E2D8AB4FFA3A176685F14D197D3FF0A2FB66`;
SCOPE correction SHA-256
`186799300E88CF522E0740BD87F16FE9FD1518560CFB7064DC9F5903DCB6C5F6`.
Erratum after the v1/v2 source join: offline MonsterManager resolves MonsterDef
before skipping a recognized NPC alias. There are 1,935 source records with both
an NPC alias and a nonnull legacy MonsterDef (for example Skraug Grubbringah);
early NPC filtering would remove existing single-player monsters. Keep these
MONSTER records in the staged monster inventory and retain NPCManager's
independent appearance inventory, even where the source anchor/type overlaps.
Sundial's null MonsterDef is still NPC-only. Hosted membership must preserve
both supported actors or explicitly refuse an unresolved dual registration;
it must not silently deduplicate by anchor. The 39-record starter-crop census
and prior read-only measurements are unchanged by this correction.
The corrected design SHA-256 is
`D00ECC4364B7C73BA02301723B564F58C07DDEC4CCF17BEB558B7C35CF9FA5BD`;
the earlier `0A5B4A55` hash identifies the original PR160 design, not this erratum.
Next implementation must classify Sundial with definition-first precedence,
preserve every authored spawn, and add directional Goblin-to-Horse combat with
durable life/reward separation before enabling the proposed 249×117 route.
Request explicit Claude file/interface handoff before dependent B/C core edits.
T4C-0224 independently implements classification/export preparation; its active
files and evidence remain separate. No schema 12 reservation, route enablement,
clan runtime, native test or M3/Beta completion follows from this design.

## T4C-0224 staged actor classification

The corrected 16-file source/data/test candidate is frozen against immutable
main `55de14ce`; exact per-file hashes are in private
`%TEMP%/t4c-0224-classified-55de-4a8c/final-candidate-hashes-v2.tsv`
(SHA-256 `53531C0DFF3235C47003AB8C732ED619FA0D485B524ED43BB57168A382424643`).
Isolated focused validation passed 20 offline/export/lookup tests and three
server-catalog tests. Preserve the failed first nondeterministic NPC export,
the real definition-precedence regression found during review, and old manifest
`92267A93` as superseded evidence. MonsterManager keeps its legacy
definition-first spawning; a real Skraug Grubbringah manager-spawn regression
now covers the 1,935 dual-registered alias case. Monster-v2 has 14,393 primary
records, 1,935 nonnull-definition MONSTER overlaps and 154 null-definition
NPC-only records. The independent NPC appearance manifest has 2,574 entries.
Both inventories retain their source anchors; future hosted membership must
support a dual actor or refuse it explicitly. Current v1 JSON/default loader,
hosted crop, protocol, schema, client renderer and single-player behavior stay
unchanged. The staged inventories are not live actors or an M3/Beta pass.
Dependent clan/hosted implementation still requires exact Claude handoff.

## T4C-0227 private HTTP application traffic counters

Six released files (`PreviewServer.java`/test, `ServerRuntimeMetrics.java`,
new `HttpTrafficMetrics.java`/test and [LOAD_METRICS.md](LOAD_METRICS.md))
match private `%TEMP%/t4c-0227-httptraffic-3ee1-6c5b/final-six-file-hashes.tsv`
SHA-256 `834B0FE49AB137FB16B770DFAB95AFCF7C450D185367CC54449E7CB466A12067`.
Immutable base `3ee10fcb` (protocol 5/schema 11) plus those six files passed
focused traffic metrics 2/2, admission 2/2 and PreviewServer 5 enabled tests
(five expected PG skips); full isolated no-DB multiplayer verify passed
domain 44 and server 144 total, 57 expected PG skips, zero failures/errors,
log SHA-256 `A9672CF2B777C17A66641F0CD63885CD5A27B51ACF245435DA8E1BABFBB9C5F5`.
The first shared-context-attribute approach was caught and replaced before
the frozen test run; explicit request observation arguments pass a real
overlapping slow-POST attribution regression. Private loopback operator
samples now include bounded fixed-label application bytes actually read,
fully written response-body bytes, selected statuses and handler-lifetime
buckets. Premerge review proved the old native soak 64 KiB consumer cap was
too small for this fixed-label schema: 897 dense rows serialize to at most
434,724 bytes with all numeric values at `Long.MAX_VALUE`, and a plausible
273-row case measured 65,745 bytes. The released three-file sampler fix
(NativeAdapterSoak, its existing deadline probe and guide) has manifest
SHA-256 `19C68CA670681504E77510016B616D2A5E41E7CFCA42A935D992E722E8A9C2AB`.
It raises the finite response cap to 1 MiB while retaining the two-second
whole-body deadline. Private ephemeral-loopback validation passed a 2,013 ms
stalled-body failure, accepted a valid 433,111-byte/897-row snapshot in
`initial`/`latest`, and refused a 1,048,590-byte response; log SHA-256
`ACBBE924AB75141E2B1C2A3F6BDC85591C9F975430F0B8D207FFD7F8B61B2319`.
The original green PR163 head predates this fix and cannot establish the
final contract; rerun current-head CI after the follow-up commit.
These are not wire bandwidth, database latency, live load,
rendered play or the mixed rendered-client M5/Beta gate. No DB/server/GUI/owner QA
resource was used for the isolated checks. T4C-0225 remains Planned pending
Claude handoff; next free ID T4C-0228.

## T4C-0226 two native sessions under hostile HTTP

The two released new files are [NativeHttpResilienceProbe.java](tools/NativeHttpResilienceProbe.java)
(SHA-256 `3AEC0090D6C458DAE43AD26C2064DEDCF23033AD67D4B3A3F0801B4FA9E1170F`)
and [NATIVE_HTTP_RESILIENCE_PROBE.md](tools/NATIVE_HTTP_RESILIENCE_PROBE.md)
(SHA-256 `160DADC4164B42D088801F682FDC16706D36C6B82724556A6A8CD8A9E6E38852`).
The guide records reproduction against a fresh, explicitly granted
`t4c_http_native_0226_*` database at 127.0.0.1:55432 and owned port 18777;
the tool rejects an owner database, remote host or wrong port before opening
a connection. The matching immutable `f6175cc7` client/server packages are
protocol 5/schema 11 (JAR hashes in ACCEPTANCE). Final fresh-DB native PASS
report SHA-256 `FFD24BFC49993971225E7501CA684D00ADB43BADC54B63649DD82260E54188DF`
measured eight held joins, eight forged early 401 responses, health 200,
mutual two-actor native views, three advancing ticks and >0.15 cardinal
movement for each within 200 ms, postclose committed location/private
balances intact, normal leaves releasing both leases, exact owned server
exit 0 and free port. Preserve the first oversize-101 probe failure and
second fresh-DB PASS (source `64959051`, report `A55545BD`) separately;
all three databases, logs and reports remain retained. This does not verify
four seconds of continuous attack, rendered/remote traffic, arbitrary DoS,
complete mixed rendered-client gameplay or completed M5/Beta. T4C-0225 remains Planned pending
explicit Claude handoff; next task ID is T4C-0227. Owner QA and GUI remain
under their own resource claims.

## T4C-0222 HTTP admission delivery history

PR159 merged at main `50326fe9` after the historical sequence below; current-head
Build and test, PostgreSQL persistence contracts, Headless build/lifecycle,
Vercel and Preview Comments all passed. The final four-file manifest is
`6D1CD761ED14218564116DA853F36A44415BF97F07DB9864FD548705C64C321D`.
Earlier failed CI heads and preliminary manifests remain retained evidence.

Four frozen source/test files are `PreviewServer.java`, `PreviewServerTest.java`, new `HttpRequestAdmission.java` and new `HttpRequestAdmissionTest.java`. Their exact SHA-256 values are recorded in the canonical peer register at 18:12; use the retained **pre-CI passing-suite** private `%TEMP%/t4c-0222-admission-check-7b9d1e4a/final-candidate-hashes.tsv` (SHA-256 `4734AE8651EE3B0E08945C625AF0C6F8DE9A1A50063500676929BF1A5D6DBA39`) for the original full-suite-tested candidate. PR159 Linux CI timed out at a 1.5-second forged-request raw-socket read in PreviewServerTest before valid-player liveness assertions. The narrow test-only CI fix extends that one bounded read to four seconds, without sending a body or changing the separate <2.5-second gameplay assertion. A fresh isolated no-DB PreviewServerTest run passed 10 tests with five expected PostgreSQL skips; focused log SHA-256 `028BB9DF020360C71D5B7409725E852368E58CD885F59FAB5AE87DC390AA1311`. Updated test SHA-256 `93010A6A51CE1FE6E814B9569CE445A381D6ADC15E4DE4EEE44F17296B028D9E`, new create-only `ci-fix-candidate-hashes.tsv` SHA-256 `F5FCF75EE77C9C111A13A6C580E155126532CA5D5E329ABD67961057020308BA`; the next Linux CI again timed out at that forged response even after four seconds. JDK 21 `HttpServer` uses synchronized blocking request-body reads; two slow bodies can pin both virtual-thread carriers on a small Linux runner. The follow-up changes only the exchange executor to capped reusable platform threads with a `SynchronousQueue` and no FIFO, preserving the 128 admission bound and separate public/roster/session permits. A fresh isolated no-DB focused run passed 12 tests with five expected PostgreSQL skips; log SHA-256 `F25AD225F166034004366016E3B3A22847D52FC4D4E1962E95FF9ADE1DD43AE4`. Current helper SHA-256 `F9C92EEF8E0D8818570E11D4DDCBB04B3B18FC7F27D986058589FEB26C6C500B`; exact final platform-worker manifest `%TEMP%/t4c-0222-admission-check-7b9d1e4a/platform-worker-candidate-hashes-v2.tsv` SHA-256 `F7FD08F2DB3A1D7A8B341A9C7BFF30FDF40D2F97AC66BF1C8E022AA1243CF9C0`. The first platform-worker manifest write was malformed and retained, not cited. PR159 platform-worker head `335045b5` passed Linux Build and test, Headless lifecycle and PostgreSQL persistence contracts; Vercel failed solely external 24-hour deployment quota on this nonweb PR. Before merge, root found cheap `/health` fell through the same two public-body permits as slow `/join`, returning 503 with two held joins. The narrow follow-up exempts only `/health` from that application permit; global 128 exchanges and JDK socket bounds remain. Existing held-body test now asserts health 200. Fresh isolated no-DB focused tests passed 12/12 with five expected PostgreSQL skips (log SHA-256 `FB90C39B3CDB193305812EBE54BB00CEAA8CB6F0B6A09D10357B5626A8E0C640`). Exact current PreviewServer SHA `F7F0580ADA81B96C58658FA8F33DF4252CABDE2DB10ECCBDC602C67AB4EF98C6`, PreviewServerTest SHA `29F5FA84B1DBA61C4C04C78CE867380E87C68384CF8A6444935B50425B48AFC1`; create-only health-gate four-file manifest SHA-256 `6D1CD761ED14218564116DA853F36A44415BF97F07DB9864FD548705C64C321D`. Full current-head CI still required. The earlier `candidate-hashes.tsv` is stale preliminary history and must not be cited for the passing source. Base commit `d4dc908a`, protocol 5/schema 11. On fresh private `t4c_admission_test_0222_7b9d1e4a`, focused persistent HTTP checks passed 16/16; the isolated full multiplayer suite passed 186/186 across 54 reports with zero failures/errors/skips (log SHA-256 `903BFC2FB04D9ED38FD7B0E9F2A6FEAEE611454A76375D98D11BB65E29BE8E07`). Preserve failed protocol-version precedence (roster bad version 401 instead of 426) and first PostgreSQL-stat-poll diagnostic attempts, which were corrected in the frozen candidate. The passing tests held eight slow public plus eight forged-body requests while two joined players continued preview GET/POST movement; forged/unknown/oversized requests returned 401/404/413 before body, and no extra actors joined. Two SQL-blocked roster GETs caused a third 503 without blocking the two gameplay sessions; roster requests recovered after commit. The original candidate used at most 128 concurrent virtual exchanges, distinct public/roster/session permits, and validates a real session before treating a Bearer string as authenticated. Existing rate/epoch checks and the JDK five-second socket backstop remain. This is local bounded resilience, not full denial-of-service protection or a completed M5/Beta gate. Four files are released to root for scoped review/integration; no root Maven, live QA18765/18771, desktop, PostgreSQL service or other database was touched. T4C-0223 is a separate architecture-only design reservation and its two files must stay out of this delivery.
