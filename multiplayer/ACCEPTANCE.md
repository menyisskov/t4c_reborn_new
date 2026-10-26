# Multiplayer delivery gates

This is a working evidence checklist, not a declaration that Alpha or Beta is complete.
Single-player must remain independently launchable and must retain its existing saves and rules.
Do not turn an unchecked item into a release claim without a passing, recorded check.

## Verified foundation (2026-10-26)

- Separate headless reactor and server lifecycle; shared pure combat and static collision rules.
- Server-owned movement, bounded input queues, input/session expiry and a 20-session cap.
- Real native renderer and sprites; owner confirmed downward/diagonal facing and two players
  connected to the same server. Native client PR: #84.
- Persistent native QA: two private profiles connected against local PostgreSQL; a moved character
  retained its position after graceful server stop/start and reconnect. Database leases were released
  on shutdown. Integration tests also verify two-player persistence, stable provisional appearance,
  unauthorized shutdown rejection and continued world ticks during a blocked storage worker.
- Network adapter tests cover direction-only commands, authoritative snapshots and session cleanup.
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

Open reliability issue: during automated focus switching between two native clients on Windows,
one process crashed in GLFW's Win32 event polling (`glfw.dll+0x101ab`, LWJGL 3.3.3).
The second client survived and restored its saved position after restart. The cause is not yet
confirmed; native multi-client stability remains an unmet Beta gate. The local JVM crash report
is intentionally excluded from version control because it contains machine details.

Native authenticated movement now uses the asynchronous checkpoint path and writer leases.
Next verify crash/database/backup recovery, then expand authoritative combat, inventory/quests
and map transfer. Validate each consequential
join against actual PostgreSQL and the real native client. Optional utilities and polish do not
take precedence over these release gates.
