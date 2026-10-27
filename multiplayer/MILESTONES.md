# Multiplayer milestones and completion criteria

Updated: 2026-10-27, Asia/Jerusalem. Owner-facing roadmap and acceptance checklist.
This is the starting point for “what remains?”; implementation tasks live in
[TASKS.md](../TASKS.md), and recorded release evidence lives in [ACCEPTANCE.md](ACCEPTANCE.md).

## Scope and release stages

The next proposed playable checkpoint is a complete starter adventure with two players in
the native game client. It is an intermediate checkpoint, not a replacement for the agreed
20-player Beta or a promise to port the entire single-player game within these six milestones.
The owner's question about two players did not cancel the original release requirements.

| Stage | Required outcome | Completion rule |
| --- | --- | --- |
| Alpha | Authoritative, persistent gameplay through the native client: accounts, required character state, combat, NPC/quest/reward loop, travel, recovery and essential operations. | Every Alpha gate in ACCEPTANCE.md has passing evidence; service tests alone are insufficient. |
| Two-player playable checkpoint | Two people can enter, play the selected starter adventure, progress and return to their saved characters. | M1–M6 below all pass on the same integrated build and declared content scope. |
| Beta | Complete intended launch gameplay and reliable concurrent operation for 20 players. | Two-player checkpoint plus the full Beta gates, broader launch content and final two-hour mixed 20-client evidence. |
| GA | Essential end-to-end game, secure deployed connection, reproducible client/server delivery and proven operations/recovery. | Beta gates plus deployed release checks and owner QA acceptance in ACCEPTANCE.md. |
| Post-GA | Optional content, addons, convenience utilities, dashboards, polish and scaling improvements. | Prioritize separately; essential gameplay, anti-cheat and data integrity cannot be deferred here. |

### Scope must be explicit before completion

**Owner approved [SCOPE_PROPOSAL.md](SCOPE_PROPOSAL.md) on 2026-10-27** with three amendments
recorded at its top: Paladin starts with a mace and shield, every mage starts with the new
Arcane Spark, and Archer starts with a basic bow and quiver. The frozen checkpoint scope is that
document; the list below is what it had to answer.

Record the following here before claiming the two-player checkpoint complete. These are
product choices still to freeze, not permission to quietly remove existing game behavior:

- Supported starter classes and the exact required abilities/skills for each.
- Authored quest ID, NPCs, objective/reward definitions, maps and connecting routes.
- Shared-kill credit and loot-claim rules, including simultaneous pickup and full inventories.
- Supported client platform and whether the test release is local-only or uses two machines.
- Any intentional exclusions. Public registration, trading, parties, PvP and unrelated endgame
  systems are not silently added by this starter checkpoint, nor silently removed from full Beta.

Candidate quest: Warden Aelric's existing `renewed_wards`, described in
[NPC_INTERACTION_DESIGN.md](NPC_INTERACTION_DESIGN.md). Its encounter area is outside the
currently hosted crop and its spell reward is not implemented online. Hosting only the current
two regions cannot complete that quest. Do not relocate objectives, alter rewards or weaken
requirements merely to make a demonstration pass. Kilhiam's opening letter is not a complete quest.

## How status is tracked

“Implemented” means code exists; “verified” identifies the exact checks that passed; “delivered”
means self-reviewed, green CI and merged. A milestone is Done only after its complete native
playthrough and recovery criteria pass. Never infer Done from a task count or test count.

| Milestone | Current status | Existing related tasks | Main outstanding work |
| --- | --- | --- | --- |
| M1 — Enter and interact | Roster/NPC backend and actual adapter probes pass; visible controls/creation incomplete | T4C-0192, 0195, 0196 | Native selection/creation and NPC controls; account lifecycle integration |
| M2 — Inventory and loot | Partial equipment/corpse services | Existing inventory/equipment work; T4C-0201, 0202 (design and pickup/drop rules only) | Visible inventory, general monster loot, claim/quantity integrity |
| M3 — Complete authored adventure | Opening letter only; full loop incomplete | T4C-0193, 0194, 0195 | Full quest service, kill credit, rewards and required hosted routes |
| M4 — Supported combat and abilities | Restricted melee/physical retaliation and brief rendered attack/death check; pure pursuit decisions and snapshot-driven native creature animation prepared, with no server pursuit | T4C-0189, 0190, 0209, 0210, 0211 | Server pursuit/AI and the declared classes' required abilities and state |
| M5 — Reliability and anti-cheat | Pool/shutdown/fault probes, final two-hour native-adapter baseline with partial 20-player occupancy, isolated two-adapter restart and schema-9/schema-10 database-crash probes, and brief rendered reconnect pass; full mixed recovery/load/abuse gates open | T4C-0191, 0197, 0198, 0207, 0208 | Broader gameplay recovery, rendered restart, full-capacity mixed soak, bandwidth/DB latency and abuse checks |
| M6 — Package and native acceptance | Create-only client and local-loopback server QA packages, headless integrity checks, isolated persistent server stop and brief two-window local client smoke pass; clean-machine, remote and full rendered playthrough open | T4C-0205, 0215 and cross-cutting work | Clean-machine delivery and complete two-rendered-client playthrough |

Task IDs not yet claimed are deliberately not represented as completed or assigned work.
Independent implementation can proceed in parallel; acceptance must use an integrated build.

## M1 — Enter the online game and interact

Required behavior:

- [ ] Each player uses a separate online account, with no desktop character/save import.
- [ ] The native client discovers only that account's characters, displays availability and lets
  the player select one. Pagination, an empty roster and unavailable characters remain usable.
- [ ] Native character creation uses shared name/slot/class rules and server-generated initial
  stats, items and gold. A retried or interrupted request cannot create duplicates or regrant a kit.
- [ ] Invalid keys, foreign characters, a character already in use and incompatible versions give
  usable errors. Selection alone acquires no gameplay lease or player slot.
- [ ] Kilhiam appears using authored appearance/location. The native talk control sends identity
  only; range, line of sight, region, readiness and current revision are server decisions.
- [ ] Authored English dialogue is readable in the client. First-letter item/flag commits together;
  repeated greetings, reconnect and a lost response never grant another copy.
- [ ] Credentials are absent from logs, public snapshots and ordinary preferences. Any non-loopback
  connection uses trusted HTTPS with hostname verification; no plaintext or trust-all workaround.

Done evidence: both rendered clients create/select their own characters, connect, converse, close
and reconnect. Retain operator profiles as a compatibility path. See [ACCOUNT_LIFECYCLE.md](ACCOUNT_LIFECYCLE.md)
and [NPC_INTERACTION_DESIGN.md](NPC_INTERACTION_DESIGN.md) for current contracts and limits.

## M2 — Use inventory and receive loot

Required behavior:

- [ ] Native inventory shows committed item identities, quantities, charges and equipped slots.
- [ ] Equip/unequip updates authoritative bonuses and the required appearance; shared attribute
  requirements, occupancy, weight, uniqueness and other applicable item rules remain intact.
- [ ] Supported monsters produce their authored drops through a server-owned persistent loot path.
- [ ] Pickup uses server position/range, current region and the declared ownership/claim policy.
  Two simultaneous claimants cannot both receive the same item or currency.
- [ ] Capacity or eligibility refusal preserves the loot and original inventory; partial stack
  operations conserve quantity and preserve appropriate identities/charges.
- [ ] Death/corpse recovery conserves protected equipment and dropped contents under shared rules.
  No alternate pickup path bypasses corpse ownership or duplicates recovered contents.
- [ ] Reconnect, restart and a lost pickup acknowledgement retain exactly the committed outcome.

Done evidence: player A earns/picks up/equips loot while player B observes the shared world;
repeat with a simultaneous claim and an inventory refusal. Reload from PostgreSQL and verify
the final items/currency exist in exactly one permitted place. General loot is distinct from
the already implemented owner-corpse service.

## M3 — Complete one authored adventure and travel safely

Required behavior:

- [ ] Freeze the actual quest/content scope above; export/validate its references from existing
  definitions instead of inventing a shorter online version.
- [ ] Native dialogue can accept the quest, show its objectives/progress and turn it in.
- [ ] Server combat supplies authoritative kill credit under the declared two-player policy.
  Replayed kills, old creature lives and unrelated players do not corrupt progress.
- [ ] Required quest items and turn-in consumption resolve by real item identity/key. Item,
  currency, XP, ability rewards and completion state commit together, exactly once.
- [ ] Both players can independently complete the selected adventure without blocking each other
  through a stale flag, consumed shared objective, duplicated claim or unsupported-state guard.
- [ ] Every required map/encounter is hosted. Authored entrances/returns and valid respawns work;
  unknown/unhosted destinations produce an explicit refusal rather than a silent substitute.
  [HOSTED_STARTER_ROUTE_DESIGN.md](HOSTED_STARTER_ROUTE_DESIGN.md) records T4C-0214's
  read-only candidate 249×117 contiguous starting region and its real collision paths;
  the region has not been enabled. Sundial needs explicit NPC-owned-spawn classification,
  and unhosted authored routes still need player-visible refusal.
- [ ] Native map assets switch before movement resumes. Players in separate regions cannot see,
  target, damage or recover items from one another across that boundary.
- [ ] Logout/rejoin and server restart preserve objective state, rewards and destination; delayed
  intents from a prior region cannot act on a new region's local entity IDs.

Done evidence: two rendered clients follow the declared quest from acceptance through travel,
combat and turn-in; both progress and reconnect with their own exact rewards. Include one
repeat turn-in and a lost-response/restart recovery check. See [MAP_TRANSFER_DESIGN.md](MAP_TRANSFER_DESIGN.md).

## M4 — Play the supported combat and ability set

Required behavior:

- [ ] Declare classes and required abilities above. A melee-only networking preview cannot be
  presented as a functional caster or as complete launch combat.
- [ ] Required monsters acquire valid targets, pursue where authored, attack/retaliate, lose or
  reset targets under the declared rules, die and respawn with durable life identities.
  [MONSTER_PURSUIT_DESIGN.md](MONSTER_PURSUIT_DESIGN.md) records the T4C-0209 architecture and
  required peer handoffs; no online pursuit behavior or M4 completion follows from that document.
  T4C-0210 extracted offline-preserving scalar decisions and included the pure rule in the
  multiplayer domain module; the moving server implementation awaits separate peer handoffs.
  T4C-0211 chooses native facing/walk from committed position deltas; no moving server
  snapshot or rendered pursuit verification is claimed yet.
- [ ] Server validates movement speed/collision, attack/cast timing, range, sight, sanctuary,
  resource costs, attribute eligibility, effects and any applicable interruption rules.
- [ ] Required skills/spells, mana, charges, buffs, cooldowns and advancement are usable through
  native controls, persisted appropriately and reflected by committed HUD/appearance state.
- [ ] Both clients observe consistent combat, deaths and respawns. One player's action does not
  damage or award another player incorrectly, and disconnect cannot leave an immortal target.
- [ ] Quest/story flags supported by M3 continue to work with combat; arbitrary unsupported
  scripts remain explicitly refused rather than bypassing authority checks.
- [ ] Level progression/stat allocation and required class behavior follow shared game rules.
  Single-player behavior and old save compatibility remain unchanged.

Done evidence: a native combat scenario for each declared starter role, including two players
contending for a target, leaving reach/sight, death/recovery and reconnect with costs/cooldowns intact.

## M5 — Preserve progress and resist cheating

Required behavior:

- [ ] Finish bounded connection reuse and shutdown ownership. A normal stop succeeds only after
  accepted work is drained and workers have terminated. A forced/incomplete flush is a visible
  failure; it must not be reported as a safe save.
- [ ] Reconnect and graceful/forced server restart recover the last committed state and correctly
  fence old leases, without inventing success or automatically replaying uncertain mutations.
  T4C-0207 verified the narrow two-adapter opening-letter case on schema 9, including real lease
  expiry after a forced server-process kill; the integrated rendered and broader mutation cases remain open.
- [ ] Database unavailability/recovery fails safely and resumes from committed state. Test actual
  database recovery separately from an HTTP timeout or a mocked SQL failure.
  T4C-0208 passed a narrow two-adapter opening-letter drill against a newly created schema-9
  PostgreSQL cluster with real process crash/recovery. An unchanged probe also passed a
  separate clean committed-source schema-10 run; broader gameplay and rendered checks remain open.
- [ ] Crash/lost acknowledgement around reward, pickup, quest turn-in, death and transfer produces
  no duplicated value or lost acknowledged progress. An unacknowledged commit is discoverable on rejoin.
- [ ] Account-key rotation/foreign ownership, stale/reordered/duplicate commands, speed/stat/item
  edits, oversized requests and forged targets/rewards are rejected without corrupting valid play.
- [ ] Restore a backup into a separate database and finish a native gameplay/reconnect check there;
  matching table hashes alone do not satisfy restored gameplay acceptance. T4C-0213 verified
  a live schema-10 exported-snapshot restore across all eleven online tables, including
  PostgreSQL-reparsed equivalent CHECK definitions; both new loot tables were empty. Two
  actual native network adapters subsequently rejoined the isolated restore and read matching
  committed progress, balances and positions after real lease expiry, then left normally.
  This was a network read check without rendered gameplay, new loot, combat or travel on the
  restore, so the full playthrough gate remains open.
- [ ] Run a two-hour mixed two-player session on the integrated candidate, with zero unexplained
  disconnects, crashes, invariant violations, duplicate value or lost acknowledged progress.
  Include combat, loot, quests, travel and intentional reconnects rather than idle connections.
- [ ] Record actual tick duration/debt, CPU/memory, bandwidth, database/command latency, occupancy
  and every error/recovery. Investigate stalls and growing queues/resources; do not count a failed
  run as passed because its timer ended. T4C-0198 recorded a two-hour native-adapter baseline,
  but 20-player occupancy lasted only 58.213 minutes and the run lacked quest/loot/travel,
  bandwidth and database-latency measures. Final 20-player acceptance is still a separate requirement.

Done evidence: reproducible fault/abuse scripts, separate restored-database playthrough, and the
completed two-player report tied to exact client/server/content/schema versions. HTTP reply faults
are not real packet-loss or rendered-client evidence. See [BACKUP.md](BACKUP.md),
[NATIVE_NETWORK_FAULTS.md](tools/NATIVE_NETWORK_FAULTS.md) and [LOAD_METRICS.md](LOAD_METRICS.md).

## M6 — Distribute and accept the real game

T4C-0215 supplies a checked, create-only **local loopback server QA package** alongside the earlier client package. Its generated operator scripts passed preview and persistent schema-10 start/health/private graceful stop on a fresh isolated database, including a copied path with spaces and PowerShell 5/7 checks. This is partial operational evidence; clean-machine, trusted remote deployment, full rendered playthrough and owner acceptance remain unchecked.

Required behavior:

- [ ] A clean-machine client package contains required runtime dependencies/assets, online native
  correction and documented prerequisites. Players need neither this checkout nor developer Maven caches.
- [ ] Document install/update, endpoint/profile configuration, protocol mismatch and safe rollback.
  Database rollback uses compatible code or a separate matching restore, not deleted receipts.
- [ ] A second tester/machine can connect through the intended trusted encrypted endpoint. Two
  windows on one developer PC are useful local QA, not evidence of remote distribution/connectivity.
- [ ] Operators can configure/start/stop/restart, inspect health and sanitized failure categories,
  provision/reissue private credentials, back up and restore using documented commands.
- [ ] Two rendered native clients complete M1–M4 together on the candidate package, with ordinary
  focus switching, understandable errors and no debugging commands required for essential play.
- [ ] Verify normal single-player launch, existing-save load/play/save/reload and preferences in
  an isolated copy. Preserve the owner's actual files and compare protected hashes separately.
- [ ] Record known limitations and owner QA results; no unresolved issue may strand a player,
  lose acknowledged progress, duplicate value or break the selected essential gameplay loop.

Done evidence: package/version manifest, clean-machine setup record, screenshots/logs without
credentials, two-player playthrough checklist and owner acceptance. Existing desktop automation
remains paused until the owner resumes it; preview feedback is a separate requirement.

## Final integrated two-player QA script

Run against the same candidate build, with fresh online test accounts and retained evidence:

1. Install/configure two clients; create/select distinct online characters.
2. Join together, move and switch focus; verify facing, shared state and independent ownership.
3. Talk to Kilhiam; reconnect and confirm exactly one letter grant per eligible character.
4. Accept the declared quest and travel to its authored encounter area.
5. Fight with the supported roles; verify pursuit, targeting, costs, damage, death and respawn.
6. Earn/pick up/equip loot; race a claim and exercise an inventory refusal.
7. Complete/turn in the quest for both players; retry without duplicate rewards.
8. Exercise fatal return/corpse recovery and a region-separated observer.
9. Disconnect/reconnect; stop/restart the server; verify exact committed state.
10. Run the fault/abuse/database/restore scenarios in M5, then the clean mixed session.
11. Verify the offline regression copy and finish owner QA with the known-limits list.

## Evidence and maintenance rules

For every completed criterion record: date, task/PR/merge SHA, exact runtime/content/schema versions,
test command/scenario, observed result, sanitized report location and remaining limits. Local ignored
logs alone are not durable handoff: keep a concise result and reproduction command in tracked docs.

Update the relevant task row, this status table, ACCEPTANCE.md and HANDOFF.md in the same delivery.
Preserve historical failures and distinguish backend tests, actual native networking adapters,
rendered clients, local play and remote play. No percentage-complete or calendar claim is inferred
from the number of merged PRs. Additional launch content and all full-Beta gates remain visible.
