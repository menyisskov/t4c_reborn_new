# Multiplayer delivery gates

This is a working evidence checklist, not a declaration that Alpha or Beta is complete.
Single-player must remain independently launchable and must retain its existing saves and rules.
Do not turn an unchecked item into a release claim without a passing, recorded check.

The owner-facing stage definitions, six intermediate two-player milestones and exact
native completion scenarios are in [MILESTONES.md](MILESTONES.md). That checkpoint does
not replace the agreed 20-player Beta gates below. [TASKS.md](../TASKS.md) tracks individual
implementation deliveries; a completed task is not automatically a completed milestone.

## Verified foundation (updated 2026-10-27)

- T4C-0209 records the authored monster pursuit architecture, current server gap, bounded path work, persistence/fencing joins and future peer file handoffs in [MONSTER_PURSUIT_DESIGN.md](MONSTER_PURSUIT_DESIGN.md). It is a reviewed design artifact only. T4C-0210 is reserved for an offline-preserving pure-rule extraction; `multiplayer/domain/pom.xml` inclusion still awaits an explicit Claude handoff. Online monster pursuit, abilities and M4 acceptance remain open. The owner-controlled live QA clients remain on source `8da43482`/schema 9 and were not changed for this design.

- Owner-resumed local rendered QA used two separate native windows from the final T4C-0205 bundle (`8da43482`, protocol 4/schema 9) against unchanged QA port 18765. Both connected with distinct HUDs. An attacker moved and struck a guard from 55 to 28 HP; retaliation reduced that player to 13/35 while the observer stayed at 35/35 with unchanged XP 804 and gold 252. Fatal return rendered at the temple with 17/35 HP, XP 439 and gold 238. After closing the attacked client and relaunching the same external profile, its rendered character rejoined at that respawn with the same HP/XP/gold while the observer stayed connected. Several focus switches survived in this brief smoke. Hash checks found zero changes across seven protected account/character/preference files. Both windows were left open for the owner. This is partial M4/M5/M6 rendered evidence, not the complete quest/loot/corpse playthrough, a sustained focus test, offline save/load regression, remote machine check or milestone completion.

- T4C-0207 ran two actual native network adapters on an isolated committed `8da43482` protocol-4/schema-9 runtime and fresh `t4c_restart_0207_final_03e1c342` PostgreSQL database. Both retained distinct opening-letter UUIDs, flags, complete progress hashes, position, gold and XP through graceful exit 0, forced server-child kill, 29,690 ms real lease expiry and rejoin; revisions advanced 1→2→3 at the graceful checkpoints. The exact final Java source SHA-256 is `9F324B943D982B5D14F0FB20A60A5E3650DCE23020DF3E2D3894C091ECBA6AEA`, and it also compiled against the later combined schema-10 API. The private sanitized report is `%TEMP%/t4c-0207-evidence-finalsource-03e1c342/report.txt`. This is not a rendered-client restart, schema-10 runtime playthrough, PostgreSQL outage/recovery or the complete M5 acceptance gate. See [NATIVE_RESTART_PROBE.md](tools/NATIVE_RESTART_PROBE.md).

- T4C-0205 built a create-only Windows x64 online test package from committed source `8da43482ee5913cb7bf63273e49edce2cc2e38a2`, not the developer checkout's dirty files. The corrected final bundle has 64 runtime dependency JARs, 860 tracked assets and 929 manifest entries; its manifest SHA-256 is `a368c29c6f987003223421029259bd1bcfe9087ce4cdae517cdeb4bfd4b201c3`. The builder's headless self-check passed; positive, private-file, tampered-asset and duplicate-path checker fixtures passed on the earlier bundle with unchanged checker code. The corrected bundle passed PowerShell 7, Windows PowerShell 5 and batch `-CheckOnly` from a copied path with spaces. The earlier 73-dependency bundle is superseded by the corrected runtime-scope build. This verifies packaging and file integrity only: no rendered launch, clean remote machine, server connection or full M6 acceptance is claimed. See [CLIENT_DISTRIBUTION.md](CLIENT_DISTRIBUTION.md).

- T4C-0198 actual scheduler/debt/reset counters and JVM CPU/memory snapshots are exposed only through the local private-control endpoint; public health is unchanged. Initial PG verification passed 18 domain + 83 server checks. After integrating separate loot-rule groundwork, the combined source passed 19 focused desktop, 21 domain and 86 server tests with zero failures/skips. A 20-second 20-adapter reporting check collected five operator samples; a 10-second no-control check reported samples unavailable rather than zero metrics. The combined-source 15-second adapter check collected four operator samples with zero metric/adapter/health failures, all 20 connected for 13.975 seconds, and no credential/path exposure. Stalled-header/body reproduction verifies an explicit two-second whole-response deadline (2.026 seconds on the combined source). These are short reporting checks, not a sustained/native-rendered benchmark; bandwidth, DB latency and final two-hour gates remain open. See LOAD_METRICS.md.

- T4C-0196/0197 integrated account discovery, bounded JDBC reuse and explicit shutdown fencing passed full PostgreSQL verification (18 domain +82 server tests, none failed/skipped; includes separate pending metrics groundwork), native roster/fault probes, and a180-second twenty-native-adapter smoke. Smoke evidence:147.121seconds at20connected,184attacks/120accepted,35HPchanges,10reconnects, zero adapter/invariant/health failures, observed33.9736Hz, unchanged DB rollbacks. Normal operator stop required/read zero process exit and restart health advanced. A shutdown timeout now reports failure and uncertain writes are never automatically replayed. See DATABASE_RUNTIME.md and tools/NATIVE_NETWORK_FAULTS.md. No two-hour, rendered-selector or complete account lifecycle claim.

- Kilhiam's authored first-letter service now replicates public NPC data and validates authenticated, ready, current-revision/current-epoch, same-region, strict-range/raw-sight interaction. Letter/flag commit atomically; repeat greetings and reconnect do not regrant. Full PG reactor passed 18 domain/72 server tests, 17 focused desktop checks passed, and two actual native adapters verified first/repeat dialogue, observer isolation, unchanged unrelated progress and the same letter UUID after reconnect. Visible NPC/dialogue controls and full quest completion remain unfinished; see NPC_INTERACTION_DESIGN.md.
- After two deterministic read-queue/cache-race fixes, a fresh protocol4/schema9 180-second twenty-adapter smoke passed with zero adapter/invariant/health failures, 126 accepted attacks, 63 HP changes and ten reconnects. The fresh long run started 09:34:58 Asia/Jerusalem on October 27 but recorded a disconnect after about 101 seconds, identified as PostgreSQL connection establishment SQLSTATE08001; investigation is ongoing and this is not a passing soak. The earlier protocol3 run recorded a disconnect and cannot count as a passing soak. Final integrated release/native gameplay coverage remains required.

- Protocol4/schema9 two-region travel is implemented. PostgreSQL checks cover receipts/altered replay/leases, region-isolated players, global20 capacity, freeze/ack, slow-checkpoint portal queuing and logout destination. Two HTTP clients walked the real16/17 route and reconnected inside. A separate actual native-adapter probe also walked entrance16, reconnected in world1, returned through17 and preserved full progress/gold while the observer stayed isolated. The native read-race test verifies matching map/private epochs and no mutation replay. Rendered assets/world switching remain unverified while desktop control is stopped; only these two regions/routes are hosted and the interior's11 scripted residents retain unsupported restrictions. Reproduce with tools/NATIVE_TRANSFER_PROBE.md.
- Authored portal export and bounded catalog/collision validation are implemented. Registry-order parity and focused source/destination tests pass; actual audit found 1,103 available destinations, 15 blocked and four shadowed duplicate sources. Diagnostics preserve raw collision codes. This data-only check is distinct from the two-region runtime evidence above. MAP_TRANSFER_DESIGN.md records the implemented portal16/return17 integration.
- HTTPS transport configuration and native origin validation are integrated. Focused tests use temporary certificates to verify trusted TLS, unknown-CA/hostname refusal, actual HTTPS preview join, cross-origin refusal and no remote plaintext. Local administration also checks peer loopback. Native endpoint/adapter tests and launcher syntax/refusal checks pass. Production hostname/certificate/firewall configuration and rendered remote-client testing remain unverified; the full encrypted-account acceptance gate stays open. See TLS.md.
- Reproducible backup tooling uses one exported source snapshot for pg_dump and streamed table hashes, restores into a newly created t4c_restore_* database, and compares every online table definition/count/hash. Real schema7 QA (six tables) and schema8 test (eight tables, including combat/corpses) drills passed; destination collision and concurrent-write snapshot checks passed. Three focused tool tests pass. Reports retain native_playthrough_verified=false: the restored rendered-gameplay acceptance gate remains unchecked. See BACKUP.md.
- Schema-v8 physical retaliation persists aggro and attack deadlines, then atomically commits targeted HP or fatal penalties/respawn/corpse. PostgreSQL checks cover deferred publication, observer isolation, cooldown/reconnect, range, death/logout position, rollback, restart/retry, protected items, quantity conservation and owner/range/exact-once recovery. A two-connection probe using the actual native networking adapter against the real QA map verified targeted HP loss, unchanged observer HP and fatal half-HP return. That probe is not rendered two-client retaliation evidence; desktop control remains stopped. Corpse recovery HTTP tests verify owner privacy, malformed/stale/foreign/distant rejection, exact instance restoration and duplicate protection. A three-minute twenty-adapter smoke completed with zero connection/invariant/health failures, 185 attacks (112 accepted), 46 HP changes and ten reconnects; all twenty were connected for 146.384 seconds of 180.1 seconds. Observed tick rate was 33.98 Hz and health mean latency 18.86 ms. This is not twenty rendered clients or the required two-hour Beta soak. The reproducible driver and raw ignored report are documented in tools/NATIVE_ADAPTER_SOAK.md. Creatures remain at authored positions, with physical attacks only. The hosted-region integration now supports valid hosted cross-map respawns and rejects unavailable saved respawns without rewriting them.
- Online Windows startup now selects its own pinned/checksummed GLFW build with an owned-window lookup. Two rendered clients loaded separate corrected DLLs and survived repeated direct activation plus ordinary Alt-Tab; combat input and shared state remained responsive. Native build, checksum packaging test, desktop adapter/death-rule tests and runnable package pass. The earlier crash is corrected for this reproduction; sustained multiplayer soak remains outstanding. Desktop control was subsequently stopped with Escape, so further rendered QA awaits a resumed desktop session.

Entries below record incremental checks; later integration evidence supersedes earlier unconnected-service limitations.

- Online melee rejects authored stationary targets before mutating character/cooldown or creature life; the existing PostgreSQL melee integration also proves passive nonstationary targets still receive hits/rewards.
- The native networking adapter now submits target/revision-only melee intent and refreshes committed private state. Server melee connects shared combat/weapon/monster rules to durable damage, separate per-hit/death XP, gold, cooldown and respawn. PostgreSQL checks reject distant/stale intents, reserve a pending kill against a second player, avoid predicted death, grant one recipient and preserve cooldown after reconnect. Rendered native QA with two separate online profiles confirmed both see the same 24/55 guard health, separate XP, a kill/gold award and subsequent respawn. A graceful server restart followed by native R reconnect retained the first character's 202 XP and 217 gold. Direct client focus switching reproduced the existing GLFW crash; switching through a neutral window allowed the shared-state check. This is a restricted playable melee preview; retaliation, ranged/stun attacks, active quests/buffs, scripts and ground-item drops are explicitly outside this slice.
- Online progression drafts call the shared offline level-up engine and curve. Checks cover multiple level crossings, preservation of owned state, no healing without a level, huge awards at the existing cap, and a PostgreSQL damage/level/refill transaction recovered and retried without regranting. Existing desktop progression and save-mapping tests cover the shared boundary. This is not a playable combat or quest completion claim.
- Schema-v7 strikes commit creature damage and character rewards together. PostgreSQL checks compete two attackers against the same starting health, reject altered retries, roll damage back on reward overflow, retain wounded health after lost commit acknowledgement/reconnect, and reset health only with a new life. Runtime recovery republishes identical wounded health to two sessions without removing a living actor. The session queue dispatches strikes, but no network attack producer is enabled yet.
- Persistent server startup now loads the real crop's durable creature lives; a bounded worker performs guarded respawn and simulation-thread publication. PostgreSQL integration gates the worker, verifies movement continues, checks two sessions see the same absent/live state only after commit, recovers the new identity on restart and preserves old reward deduplication. A committed-reward callback hides the creature while recovering. This does not yet expose an attack command or produce live kills.
- Schema-v6 storage preserves spawn generations and their world-reward event IDs. PostgreSQL checks race initial creation, recover a claimed life after store restart, reject premature or unclaimed respawn, allow only one concurrent generation advance and retain old reward deduplication afterward. Runtime recovery/respawn is covered above; player attacks and live death production remain unconnected.
- Schema-v5 world reward claims enforce one recipient across characters in the same transaction as payload, balance and receipt updates. PostgreSQL checks race two contenders, retry after reconnect/store restart, reject changed payloads and verify rollback removes failed claims/receipts. The simulation queue can dispatch world awards without starving movement checkpoints. Durable spawn IDs and claimed-life recovery are covered above; live kill reward production remains unconnected.
- Per-session command budgets now gate authenticated network requests before movement/state serialization or equipment persistence. A deterministic simulation test checks burst exhaustion, server-tick refill, separate player allowances and logout/removal. Existing twenty-client HTTP and PostgreSQL recovery tests pass. Global connection/login abuse and slow-request protection remain incomplete; this is not a denial-of-service acceptance claim.
- Shared weapon timing preserves authored-delay priority, configured-speed fallback and millisecond conversion in both offline callers and server loadouts. Focused timing and existing combat/exhaustion checks pass; catalog integration verifies the main-hand delay overrides configured speed. Server attack scheduling and replay-resistant cooldown enforcement are not connected yet.
- Online combat loadout derivation combines committed equipment, base attributes, shared skill-boost routing, armor/dodge costs and weapon inputs. A representative catalog/state test covers combined bonuses, mirrored two-hand suppression, trained proc skills, server stun/hidden state and refusal to ignore active spell/world-event effects. Offline equipment and damage checks pass. No attack command or active-effect evaluator is connected yet.
- Explicit wire-version discovery and join gating reject legacy/incompatible clients before allocating sessions. Native tests prove missing/future server versions never receive login credentials; HTTP tests reject incompatible joins and then fill all twenty available slots. Compatible native equipment networking and persistent server tests still pass. This does not provide TLS, client integrity or content-version matching.
- Native networking now supports single-flight equip/unequip intents, publishes refreshed private state before completion, retains the session after a normal conflict and disconnects without replay on ambiguous failure. Adapter tests cover authentication, stale rejection, pending-action bounds and logout after failure. A local probe using the actual adapter and PostgreSQL server unequipped/re-equipped the QA paladin's headgear and verified complete progress and gold were unchanged afterward. This was a network integration check, not rendered inventory interaction; native controls and appearance remain unimplemented.
- Authenticated equipment commands commit reference-only equip/unequip transitions through the gameplay save queue. Tests cover owned-instance checks, base requirements, wrong slots, two-hand displacement, shield/quiver alias conflicts, stale commands, private committed publication and logout/server restart recovery with unchanged inventory/currency. Existing offline equip and class kit tests pass with shared eligibility rules. Native controls and equipped rendering remain absent.
- Separate headless reactor and server lifecycle; shared pure combat and static collision rules.
- Equipment boost evaluation is shared with offline play, retaining base-stat/skill expressions,
  eligibility gates, boost-ID replacement and saturated totals. The server derivation adapter uses
  saved owned instances and packaged definitions, rejecting unknown equipment/slots. Pure tests,
  existing offline equipment tests and a catalog/state integration test pass. This is not an equip
  authorization service, full effective-stat calculation or playable online combat.
- Packaged immutable equipment definitions now come from actual runtime item lookup winners after
  JSON overrides. Freshness checks cover requirements, formulas, boosts and appearances; every
  provisioned class kit resolves against the headless catalog. Startup rejects invalid schema/key
  mappings. This does not yet implement equip/use commands, effective stats or item spell execution.
- Headless weapon damage now shares the offline dice evaluator, melee/bow attribute contributions,
  quiver formula input and mirrored off-hand suppression. Focused pure and offline adapter checks
  cover these contracts. Authoritative equipment/effective-stat resolution and attack commands are
  still required before this can become playable online combat.
- Server-owned movement, bounded input queues, input/session expiry and a 20-session cap.
- Real native renderer and sprites; owner confirmed downward/diagonal facing and two players
  connected to the same server. Native client PR: #84.
- Persistent native QA: two private profiles connected against local PostgreSQL; a moved character
  retained its position after graceful server stop/start and reconnect. Database leases were released
  on shutdown. Integration tests also verify two-player persistence, stable provisional appearance,
  unauthorized shutdown rejection and continued world ticks during a blocked storage worker.
- Network adapter tests cover direction-only commands, authoritative snapshots and session cleanup.
- Starting-area creatures now replicate as immutable public actors to the native renderer. Server
  checks cover identical state for two players, restart reconstruction and immunity to movement
  input. Both native windows showed a guard using existing sprites and two connected player avatars;
  reconnect after the crashed session expired and peer logout also passed. QA used private local
  PostgreSQL profiles, with no differences in the protected single-player file backup.
  This is stationary replication only: no creature AI, attacks, rewards or respawn. Automated
  activation of a second native window reproduced the pre-existing GLFW crash (native poll-events
  access violation); multi-client focus stability remains an open gate, not a passing soak test.
- Forced process termination recovered the last committed movement after lease expiry; an early
  reconnect was rejected. A PostgreSQL custom-format backup restored into a new database with all
  four tables matching, then passed authenticated movement, logout and rejoin. These checks cover
  the current movement slice, not crash-safe combat/rewards/transfers or a complete playthrough.
- Versioned gameplay state round-trips through PostgreSQL with position under one revision fence.
  Movement/reward updates preserve it. Unknown versions fail closed, and legacy movement-only
  records remain uninitialized. New operator-provisioned characters now receive their shared class
  roll and kit in one database insert; reconnect preserves that state without regranting it. Native
  creation and gameplay consumers are not connected yet.
- Native creation smoke: an operator-provisioned female paladin connected in the actual renderer
  with its saved gender. Existing offline creation/class tests pass against the shared supply data.
  Equipment is persisted but the movement renderer still uses the default body appearance.
- PostgreSQL gameplay-result contracts cover atomic payload/balance/receipt updates, concurrent
  retry deduplication, stale-result rejection, changed-result conflicts and rollback on overflow.
  A committed result can be retried after a new session and later movement without regranting.
  Separate writer JVMs are forcibly terminated immediately before commit and after commit/before
  acknowledgement; recovery yields either no award or the complete award, and retry grants once.
  These tests advance the dead writer's lease deterministically; real-time expiry has separate QA.
  Combat/quest producers and durable world-event recovery remain unbuilt.
- The headless artifact packages real monster definitions and authored spawns, including runtime
  overrides, balance adjustments and aliases. Registry-freshness and starting-area reference checks
  pass; startup rejects invalid schema/references, and the content diagnostic lists unresolved custom
  spawn types. Data availability does not implement monster AI, custom scripts or combat.
- Native networking now receives owner-only committed character state and refreshes it by revision.
  HTTP/PostgreSQL checks verify account isolation and absence of private payloads from world snapshots;
  adapter checks cover caching, revision refresh and oversized/stalled reply rejection. A real native
  paladin profile connected and closed cleanly. The payload is available to the client model, but
  inventory UI, equipped appearance, combat and quest interactions are still unimplemented.
- Live sessions now serialize accepted server-authored gameplay results with movement checkpoints
  and final logout saves, exposing only committed state. Real PostgreSQL tests gate an in-flight
  checkpoint while accepting a result and moving the actor, then verify logout preserves both.
  Movement checkpoint deadlines survive frequent gameplay writes. Lost commit acknowledgement disconnects without publishing success; rejoin/retry grants once.
  No network reward command or combat/quest producer is exposed by this integration.
- The server crop now preserves raw terrain for combat sight and sanctuary checks, separately from
  movement clearance. Tests cover force fields, corner/edge shots, both participants, PvP-only
  sanctuaries and real starting-temple loading. These checks are not yet called by an attack service.
- Real-map local transport check: 20 simulated clients, 120 seconds, 16,214 input/snapshot cycles;
  no failed requests or invalid coordinates; cycle latency p50 73.5 ms, p95 96.7 ms, max 155.2 ms.
  This is not 20 rendered windows or a sustained Beta benchmark. Raw local report:
  `server/target/native-transport-soak.json` (ignored build output).

## Alpha: authoritative persistent gameplay

- [ ] Authenticated accounts and online character selection without importing desktop saves.
- [ ] Encrypted remote transport, protocol/version validation, bounded messages and abuse controls.
- [x] PostgreSQL movement storage integrated asynchronously with session fencing and clean recovery.
  Full character gameplay state and crash/backup recovery gates below remain incomplete.
- [ ] Full required character state: stats, equipment, inventory, abilities, quests and map location.
- [ ] Server-owned combat, NPC/monster behavior, loot, death and progression using shared rules.
- [ ] A playable quest/reward loop with transactional, retry-safe item and currency changes.
- [ ] Map transitions with destination validation and reconnect-safe transfer state.
- [ ] Native client HUD and interactions reflect authoritative state and reject local overrides.
- [ ] Disconnect/reconnect, server restart, database restart and backup restore preserve progress.
- [ ] Operator start/stop/restart, health, bounded logs and safe troubleshooting commands.
- [ ] Verified offline launch, save/load and gameplay regression checks.

## Beta: complete launch gameplay for 20 concurrent players

- [ ] All intended launch gameplay and content routes work through authoritative services.
- [ ] A reproducible full playthrough covers creation, combat, loot, quests, travel and progression.
- [ ] At least a two-hour 20-client mixed-gameplay soak; record hardware, tick debt, CPU/memory,
  bandwidth, DB latency, command latency, errors and disconnect/recovery results.
- [ ] Native multi-client checks under delay and packet loss; verify reconciliation and facing.
- [ ] Duplicate/reordered commands, client speed/stat edits, unauthorized account access, replayed
  rewards and inventory duplication attempts fail without corrupting legitimate progress.
- [ ] Crash during reward, checkpoint or transfer is recoverable without duplicate awards.
- [ ] Backups restore into a separate database and the restored realm passes a playthrough.
- [ ] Client distribution/update and server configuration are reproducible from a clean machine.
- [ ] Known limitations are explicit and do not break essential end-to-end play.

## GA: essential end-to-end release

GA is limited to required gameplay and the security/recovery needed to operate it. Optional additions do not block GA unless they are necessary to meet the accepted workload or prevent a real correctness issue.

- [ ] Beta evidence passes on the actual release client/server/content/schema versions, including the complete intended launch playthrough in real clients.
- [ ] Players can obtain a reproducible client, connect through the deployed trusted encrypted endpoint, authenticate/select a character and retain progress across sessions.
- [ ] Required account lifecycle, authorization, anti-cheat and economic integrity checks have no unresolved critical failures.
- [ ] Operators can install/configure/start/stop/restart, diagnose incidents, back up and restore a realm using documented procedures; version rollback respects database compatibility.
- [ ] The final 20-player sustained test and actual native multi-client/network tests pass with recorded results and honest limitations.
- [ ] Owner QA has accepted the essential end-to-end game and the release has no known issue that strands players, loses progress or duplicates value.

## Post-GA: optional additions and improvements

Prioritize these separately with the owner after the essential release is accepted: new optional content/features, utilities and administrative dashboards, convenience/UI polish, deeper observability/automation, and scaling beyond the agreed 20-player target. Existing-game rules, critical operator controls, security and save integrity stay release requirements rather than being deferred under this heading. No specific unrequested feature is promised by this list.

## Next integration order

The online-only GLFW correction and its bounded focus-switching evidence are recorded above and in `native/README.md`. Preserve the offline native dependency selection. Further rendered testing remains pending because the owner stopped desktop control.

Two-region travel and the opening NPC service are delivered; bounded pooled operation now passes its focused smoke. Next finish native account/character selection, NPC controls, required loot/quests/abilities and broader hosted routes, wire measured load evidence and repeat the failed sustained baseline on the integrated release. Then verify full native playthroughs, crash/database/backup recovery, remote deployment and final sustained mixed gameplay. Optional utilities and polish do not take precedence over these release gates.
