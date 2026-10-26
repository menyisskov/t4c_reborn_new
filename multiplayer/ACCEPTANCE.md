# Multiplayer delivery gates

This is a working evidence checklist, not a declaration that Alpha or Beta is complete.
Single-player must remain independently launchable and must retain its existing saves and rules.
Do not turn an unchecked item into a release claim without a passing, recorded check.

## Verified foundation (2026-10-26)

- Separate headless reactor and server lifecycle; shared pure combat and static collision rules.
- Server-owned movement, bounded input queues, input/session expiry and a 20-session cap.
- Real native renderer and sprites; owner confirmed downward/diagonal facing and two players
  connected to the same server. Native client PR: #84.
- Network adapter tests cover direction-only commands, authoritative snapshots and session cleanup.
- Real-map local transport check: 20 simulated clients, 120 seconds, 16,214 input/snapshot cycles;
  no failed requests or invalid coordinates; cycle latency p50 73.5 ms, p95 96.7 ms, max 155.2 ms.
  This is not 20 rendered windows or a sustained Beta benchmark. Raw local report:
  `server/target/native-transport-soak.json` (ignored build output).

## Alpha: authoritative persistent gameplay

- [ ] Authenticated accounts and online character selection without importing desktop saves.
- [ ] Encrypted remote transport, protocol/version validation, bounded messages and abuse controls.
- [ ] PostgreSQL character storage integrated asynchronously with session fencing and recovery.
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

The PostgreSQL storage and account/session ownership foundations are implemented; database
CI verifies their contracts. Next add the asynchronous checkpoint path using those writer leases. Connect that path to native character entry/reconnect before
expanding authoritative combat, inventory/quests and map transfer. Validate each consequential
join against actual PostgreSQL and the real native client. Optional utilities and polish do not
take precedence over these release gates.
