# Multiplayer delivery gates

This is a working evidence checklist, not a declaration that Alpha or Beta is complete.
Single-player must remain independently launchable and must retain its existing saves and rules.
Do not turn an unchecked item into a release claim without a passing, recorded check.

## Verified foundation (updated 2026-10-27)

- HTTPS transport configuration and native origin validation are integrated. Focused tests use temporary certificates to verify trusted TLS, unknown-CA/hostname refusal, actual HTTPS preview join, cross-origin refusal and no remote plaintext. Local administration also checks peer loopback. Native endpoint/adapter tests and launcher syntax/refusal checks pass. Production hostname/certificate/firewall configuration and rendered remote-client testing remain unverified; the full encrypted-account acceptance gate stays open. See TLS.md.
- Reproducible backup tooling uses one exported source snapshot for pg_dump and streamed table hashes, restores into a newly created t4c_restore_* database, and compares every online table definition/count/hash. Real schema7 QA (six tables) and schema8 test (eight tables, including combat/corpses) drills passed; destination collision and concurrent-write snapshot checks passed. Three focused tool tests pass. Reports retain native_playthrough_verified=false: the restored rendered-gameplay acceptance gate remains unchecked. See BACKUP.md.
- Schema-v8 physical retaliation persists aggro and attack deadlines, then atomically commits targeted HP or fatal penalties/respawn/corpse. PostgreSQL checks cover deferred publication, observer isolation, cooldown/reconnect, range, death/logout position, rollback, restart/retry, protected items, quantity conservation and owner/range/exact-once recovery. A two-connection probe using the actual native networking adapter against the real QA map verified targeted HP loss, unchanged observer HP and fatal half-HP return. That probe is not rendered two-client retaliation evidence; desktop control remains stopped. Corpse recovery HTTP tests verify owner privacy, malformed/stale/foreign/distant rejection, exact instance restoration and duplicate protection. A three-minute twenty-adapter smoke completed with zero connection/invariant/health failures, 185 attacks (112 accepted), 46 HP changes and ten reconnects; all twenty were connected for 146.384 seconds of 180.1 seconds. Observed tick rate was 33.98 Hz and health mean latency 18.86 ms. This is not twenty rendered clients or the required two-hour Beta soak. The reproducible driver and raw ignored report are documented in tools/NATIVE_ADAPTER_SOAK.md. Creatures remain at authored positions, with physical attacks only and a validated same-region respawn fallback.
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

## Next integration order

Historical reliability reproduction (corrected above): during automated focus switching between two native clients on Windows,
one process crashed in GLFW's Win32 event polling (`glfw.dll+0x101ab`, LWJGL 3.3.3).
The second client survived and restored its saved position after restart. The cause is not yet
confirmed; native multi-client stability remains an unmet Beta gate. The local JVM crash report
is intentionally excluded from version control because it contains machine details.

Diagnostic follow-up (2026-10-27): disassembly at the observed DLL offset matches the modifier-key repair read in [GLFW Win32 polling](https://github.com/glfw/glfw/blob/3.3.8/src/win32_window.c#L1908-L1933): `GetActiveWindow`, `GetPropW` and then dereference of the stored window pointer. A foreign-window pointer during temporarily attached input queues is a plausible cause, not yet proven; [Windows documents shared focus during AttachThreadInput](https://learn.microsoft.com/windows/win32/api/winuser/nf-winuser-attachthreadinput). The same unguarded lookup exists in upstream 3.4, so a blind dependency upgrade is not an evidenced fix. Next distinguish ordinary user Alt-Tab from automation activation, then verify process ownership at this lookup in an isolated native reproduction before choosing a fix. No native DLL or single-player dependency was patched.

Native authenticated movement now uses the asynchronous checkpoint path and writer leases.
Next verify crash/database/backup recovery, then expand authoritative combat, inventory/quests
and map transfer. Validate each consequential
join against actual PostgreSQL and the real native client. Optional utilities and polish do not
take precedence over these release gates.
