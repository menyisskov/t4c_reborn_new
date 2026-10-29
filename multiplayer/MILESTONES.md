# Multiplayer milestones and completion criteria

Updated: 2026-10-27, Asia/Jerusalem. Owner-facing roadmap and acceptance checklist.
This is the starting point for “what remains?”; implementation tasks live in
[TASKS.md](../TASKS.md), and recorded release evidence lives in [ACCEPTANCE.md](ACCEPTANCE.md).

> **Owner parity rule (2026-10-28).** The online game must look and play exactly like
> single-player; the only difference is a client and a server. Anywhere this file limits
> scope to a starter route, a class subset or a content subset, that limit is a temporary
> divergence tracked in [PARITY.md](PARITY.md), not the Beta bar. Beta now means parity.

## Scope and release stages

The next proposed playable checkpoint is a complete starter adventure with two players in
the native game client. It is an intermediate checkpoint; passing it alone does not complete
Beta or promise to port the entire single-player game within these six milestones. On
2026-10-27 the owner changed the Beta/GA concurrency gate from 20 players to two
**real rendered clients**. The owner has since removed the fixed two-hour session-duration
minimum: record the duration of mixed-play evidence, but do not use a duration threshold as a
release gate. All intended launch content, gameplay, security, anti-cheat and recovery
requirements remain. The existing 20-player server capacity is unchanged; further 20-player
qualification is follow-up scale evidence.

| Stage | Required outcome | Completion rule |
| --- | --- | --- |
| Alpha | Authoritative, persistent gameplay through the native client: accounts, required character state, combat, NPC/quest/reward loop, travel, recovery and essential operations. | Every Alpha gate in ACCEPTANCE.md has passing evidence; service tests alone are insufficient. |
| Two-player playable checkpoint | Two people can enter, play the selected starter adventure, progress and return to their saved characters. | M1–M6 below all pass on the same integrated build and declared content scope. |
| Beta | Complete intended launch gameplay and reliable concurrent operation for two real rendered clients. | Two-player checkpoint plus the full Beta gates, broader launch content and mixed gameplay/stability evidence with two real rendered clients; no fixed duration minimum. |
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
| M1 — Enter and interact | Roster/NPC backend and actual adapter probes pass, including two local HTTPS adapters with trust/hostname refusal and a separate live account-key rotation check; visible creation/selection incomplete | T4C-0192, 0195, 0196, 0216, 0219, 0232, 0233, 0234 | Native character select is merged as an opt-in lobby (`-Lobby`, owner-approved look) but has never run against a live server; in-client character creation is out of Beta scope (characters are operator-provisioned); full account lifecycle; remote deployment |
| M2 — Inventory and loot | Server ground loot (drops, owner window, first-claim-wins, expiry, carry-weight rule), per-item corpse recovery within carry limits, native pickup/equip/inventory panels; all merged with CI evidence | Existing inventory/equipment work; T4C-0201, 0202, 0204, 0218, 0228 | The two-adapter loot probe now starts legitimately (T4C-0235) but has never run (needs a database and port window); rendered loot play against a live server |
| M3 — Complete authored adventure | Quest service rules, flag allowlist, once-only turn-in rewards, native talk panel, and the Skeleton hunting ground hosted as one world-0 region with unhosted portals refused; merged | T4C-0193, 0194, 0195, 0206, 0218, 0231 | Kill credit played through end to end against a live server, the Goblin-to-Wild-Horse fight (owner accepted it as not simulated for Beta), rendered walkthrough |
| M4 — Supported combat and abilities | Restricted melee/physical retaliation, server-authoritative patrol/pursuit with guarded persistence, two-native-adapter pursuit/restart proof, brief two-client pursuit/retaliation/reconnect check, local PostgreSQL proof of authored hostile-clan spell damage (T4C-0317), durable authored monster self-healing (T4C-0319) and ranged monster attacks (T4C-0320) | T4C-0189, 0190, 0209, 0210, 0211, 0212, 0229, 0230, 0236, 0237, 0317, 0319, 0320 | Remaining class abilities and spells, rendered clan-spell combat, longer rendered pursuit, reset/death and integrated M4/Beta acceptance remain open |
| M5 — Reliability and anti-cheat | Pool/shutdown/fault probes, local HTTPS trust/hostname refusal and two-adapter reconnect, live two-account key-rotation fencing, historical two-hour native-adapter baseline with partial 20-player occupancy, isolated restart and database-crash probes, twenty-adapter forced-kill, PostgreSQL-crash and lost-acknowledgement drills (T4C-0310; database-recovery bullet ticked), brief rendered reconnect pass, and a 12-minute mixed twenty-adapter soak with eight adapters taking paced portal trips (49 region transfers, zero failures, 56.0% at twenty; T4C-0308), a two-hour twenty-adapter soak with eight portal walkers (625 observed transfers, flat CPU/heap, 2 adapter failures traced to a rejoin-lockout defect now fixed, 30-minute steady rerun clean; T4C-0315); full mixed recovery/load/abuse gates open (two-hour, two real clients, bandwidth, abuse) | T4C-0191, 0197, 0198, 0207, 0208, 0216, 0219, 0308, 0310, 0315 | Broader gameplay recovery, rendered restart, two-real-client mixed soak, bandwidth/DB latency and abuse checks |
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
- [x] Kilhiam appears using authored appearance/location. The native talk control sends identity
  only; range, line of sight, region, readiness and current revision are server decisions.
- [ ] Authored English dialogue is readable in the client. First-letter item/flag commits together;
  repeated greetings, reconnect and a lost response never grant another copy.
- [ ] Credentials are absent from logs, public snapshots and ordinary preferences. Any non-loopback
  connection uses trusted HTTPS with hostname verification; no plaintext or trust-all workaround.
  T4C-0216 passed local two-adapter HTTPS and separate standard-JSSE unknown-CA/hostname
  refusals; remote deployment and rendered-client acceptance still require verification.

T4C-0306 (2026-10-29, local rendered, not remote): two real windows each selected their own operator-provisioned
character in the lobby, drew each other, walked to Kilhiam and clicked him through the screen's own NPC click handler
(each got exactly one letter; a repeat click and a greeting after a server stop/restart gave none); an out-of-range
click was refused by the server. That ticks the Kilhiam appearance/talk bullet above. The dialogue bullet stays open:
the lost-response case was not driven rendered, and character creation was not driven. See
[CLIENT_UNIFICATION_DESIGN.md](CLIENT_UNIFICATION_DESIGN.md) 3.15 and `multiplayer/design/live2-*.png`.

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

T4C-0306 (2026-10-29, local rendered): two level-8 warriors killed Skeletons by clicking them; a drop appeared in both
windows; two clients then clicked one drop (its 60 s owner window long over) at the same moment and the database shows
exactly one claimant and one receipt, the loser refused on screen. Equip, an inventory refusal, a claim race inside the
owner window, corpse recovery and reload-from-PostgreSQL of the final items were not driven, so every M2 bullet stays
open. Also found: the public view's owner-window countdown never advances (see CLIENT_UNIFICATION_DESIGN.md 3.15).

T4C-0312 (2026-10-29, local rendered): the inventory panel (`I`) equipped and unequipped a staff and a robe through the
screen's own handlers, each commit visible in the connection's committed state, and a server restart plus rejoin kept the
robe off and the inventory identical; the Sigfried shop bought, sold and refused (too expensive, over-weight) with no
change on a refusal. The panels are drawn upside down at the bottom right, the own character's equipped appearance was not
inspected and other players have no appearance parts, so the first two M2 bullets stay open
(see CLIENT_UNIFICATION_DESIGN.md 3.16).

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
  the region has not been enabled. T4C-0224 prepared separate staged monster-v2
  and NPC appearance inventories, including NPC-only Sundial and preserved
  monster/NPC alias overlap; the runtime does not load them yet. Unhosted authored
  routes still need player-visible refusal.
  [STARTER_ENCOUNTER_AUTHORITY_DESIGN.md](STARTER_ENCOUNTER_AUTHORITY_DESIGN.md)
  records T4C-0223's read-only NPC-ownership and directional Goblin-to-Horse
  authority plan. It does not enable the region, clan combat or quest completion.
- [ ] Native map assets switch before movement resumes. Players in separate regions cannot see,
  target, damage or recover items from one another across that boundary.
- [ ] Logout/rejoin and server restart preserve objective state, rewards and destination; delayed
  intents from a prior region cannot act on a new region's local entity IDs.

T4C-0312 (2026-10-29, local rendered, one quester, not the two-player policy): the `renewed_wards` offer was accepted
through the connection (the screen has no topic control), the screen's own monster clicks credited 12 Skeleton kills, and
a greeting turned the quest in once: status 2, +500 gold, +400 experience and `spell.renew_armor` in one commit, with a
repeat greeting changing nothing. The giver and the ground are 160 tiles apart, so the operator moved the character between
two server restarts (travel was not driven), the quest has no required item, and only one player did it; no M3 bullet is
ticked (see CLIENT_UNIFICATION_DESIGN.md 3.16).

Done evidence: two rendered clients follow the declared quest from acceptance through travel,
combat and turn-in; both progress and reconnect with their own exact rewards. Include one
repeat turn-in and a lost-response/restart recovery check. See [MAP_TRANSFER_DESIGN.md](MAP_TRANSFER_DESIGN.md).

## M4 — Play the supported combat and ability set

Required behavior:

- [ ] Declare classes and required abilities above. A melee-only networking preview cannot be
  presented as a functional caster or as complete launch combat.
- [ ] Required monsters acquire valid targets, pursue where authored, attack/retaliate, lose or
  reset targets under the declared rules, die and respawn with durable life identities.
  [MONSTER_PURSUIT_DESIGN.md](MONSTER_PURSUIT_DESIGN.md) records the T4C-0209 architecture.
  T4C-0210 extracted offline-preserving scalar decisions; T4C-0211 chooses native facing/walk
  from committed position deltas. T4C-0212 delivered guarded server movement and a two-adapter
  checkpoint/restart probe. A separate brief rendered two-client session observed the authored
  guard move and retaliate after a player pressed Space, followed by that player's reconnect with saved
  HP/gold/location. It did not test a long retreat, every facing, reset, death/respawn, all roles
  or the full M4 playthrough; details are in [ACCEPTANCE.md](ACCEPTANCE.md).
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

T4C-0320 (2026-10-30, local tests only): online pursuit now stops within a monster's authored attack range instead of forcing ranged attackers to close to melee, and hostile-clan combat can resolve supported authored damage attacks at those ranges. The Goblin Bomberman regression confirms an attack at three tiles, refuses beyond its authored maximum, and the PostgreSQL persistence case confirms the attacker stays at its spawn while the ranged clan spell kills the target. Line-of-sight, attack selection and melee fallback remain enforced. No protocol, schema, client or single-player change; no rendered-client or full M4/Beta evidence.
T4C-0306 (2026-10-29, local rendered, warrior only): both windows fought Skeletons at the same time through the
monster click handler and both gained committed experience; one player died once (a corpse row exists) and respawned in
the temple. That death was incidental, not a scripted death/respawn, targeting-contention, costs/cooldown or other-role
scenario, so no M4 bullet is ticked.

T4C-0312 (2026-10-29, local rendered, Healer and Warrior): Light Heal cast through `C` (HP 5 to 21 of 28, mana -2,
exhaustion cooldowns committed) and a Light Healing Potion used through `U` (HP 21 to 28, stack three to two). The Potion
of Mana is not offered, no starter spell is a buff, creature-target casting was not driven and other players show no
appearance, so the "required skills/spells, mana, charges, buffs, cooldowns ... native controls, persisted" bullet stays
open (see CLIENT_UNIFICATION_DESIGN.md 3.16).

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
  T4C-0216 additionally checked two native adapter reconnects and committed movement over local
  trusted HTTPS, without a server restart or network fault.
  T4C-0306 added the first RENDERED restart/reconnect (local, graceful stop only, 2026-10-29): with both windows
  in the temple the server was stopped through the private shutdown and restarted on the same database; both
  clients showed the disconnect text, returned to the lobby with R, rejoined after the 33 s lease hold at the
  committed tile with identical inventory and saw each other again, and a greeting after the restart gave no
  second letter. A forced kill, a database crash, a lost acknowledgement and the mixed-session gate were not
  run rendered, so the bullet stays open.
  T4C-0310 (2026-10-29, `origin/main` c079b1c0, protocol 7/schema 13, local PG16 WSL, native adapters, not rendered)
  revalidated the forced kill on the current build with twenty paced adapters playing combat and (second run) paced portal
  trips: the server process was hard-killed mid-session and restarted on the same database. Everything the sampler had seen
  committed was still there (revisions never lower, 18 of 20 characters byte-identical at their last observed revision right
  after the kill and 2 had advanced, all receipts/events present, one opening letter each, no item id held twice), the 20
  old leases stayed live for the first 25 s and then expired on their 30 s clock, all twenty adapters were back at +40 s,
  and the final graceful stop exited 0. `NativeRestartProbe` (two adapters, letters, forced kill, real lease expiry 29.7 s)
  also passed again on the current build. Details and limits: [tools/NATIVE_FAULT_DRILL.md](tools/NATIVE_FAULT_DRILL.md).
  The bullet stays open: no rendered forced kill, and a kill during a loot claim, quest turn-in or death was not exercised
  (portal trips were mid-flight at the kill but no transfer was provably in flight).
- [x] Database unavailability/recovery fails safely and resumes from committed state. Test actual
  database recovery separately from an HTTP timeout or a mocked SQL failure.
  T4C-0208 passed a narrow two-adapter opening-letter drill against a newly created schema-9
  PostgreSQL cluster with real process crash/recovery. An unchanged probe also passed a
  separate clean committed-source schema-10 run.
  T4C-0310 (2026-10-29, `origin/main` c079b1c0, protocol 7/schema 13, local PG16 WSL, native adapters) closes the "broader
  gameplay" part with twenty paced adapters playing (combat, movement, and in the second run portal trips with 23 committed
  transfers): the cluster's own postmaster was stopped in immediate mode mid-session and started again (PostgreSQL crash
  recovery). The server stayed alive, failed closed (19 to 20 `Online persistence failed ... character disconnected` lines,
  all connection-class, no invented success), its pool recovered without a restart, everything seen committed before the
  crash was still there unchanged, leases expired on their own clock and all twenty adapters rejoined (+38.6 s and +49.2 s
  after the crash). Two runs, both PASS. Ticked on that evidence; limits: native adapters only (no rendered client), local
  PG16 only (CI's PG18 job stays the authority), no loot claim/quest turn-in in flight at the crash. The unchanged
  T4C-0208 probe could not be re-run (its fixed port 55433 is outside this work's allowed set); the new drill is a
  separate tool, see [tools/NATIVE_FAULT_DRILL.md](tools/NATIVE_FAULT_DRILL.md).
- [ ] Crash/lost acknowledgement around reward, pickup, quest turn-in, death and transfer produces
  no duplicated value or lost acknowledged progress. An unacknowledged commit is discoverable on rejoin.
  T4C-0310 (2026-10-29, local PG16 WSL, native adapters) produced both sides of a lost acknowledgement for the opening
  reward by hard-killing the server 0 to 300 ms after the greeting request was handed to the adapter: kills up to 150 ms
  left no letter (reply lost, nothing committed), kills at 200 and 300 ms left the letter committed with the reply lost
  (visible in the database right after the kill); after restart and lease expiry a retried greeting from every character
  ended with exactly one letter each and 20 reward receipts. The same fault under twenty-adapter load (server kill and
  PostgreSQL crash, each also with eight portal-walking adapters) lost no committed revision or receipt and duplicated
  no reward receipt, loot claim, letter or item id. The 322-case tamper battery (replays, reordered and duplicate commands)
  passed again, 322 of 322, on the current build. Not exercised: a crash around a pickup claim (the loot probe needs a killer
  that survives the lethal starter ground, T4C-0248), a quest turn-in, a death, or a transfer provably in flight. The bullet
  stays open. See [tools/NATIVE_FAULT_DRILL.md](tools/NATIVE_FAULT_DRILL.md).
- [ ] Account-key rotation/foreign ownership, stale/reordered/duplicate commands, speed/stat/item
  edits, oversized requests and forged targets/rewards are rejected without corrupting valid play.
  T4C-0219 checked one local two-native-session rotation: the old account key was denied,
  its active writer disconnected without a post-rotation commit, the replacement key
  reopened the same character, and the other player stayed active. Foreign ownership,
  other tampering cases, rendered clients and the integrated release build remain open.
  T4C-0222's isolated local HTTP checks kept two joined gameplay sessions responsive
  while eight slow public and eight forged-body requests were held, and separately while
  two persistent roster reads were SQL-blocked. A third roster request returned 503;
  this is bounded local resilience, not a full denial-of-service or remote abuse proof.
  T4C-0226 separately exercised two actual native network adapters against the
  merged admission code on immutable `f6175cc7` (protocol 5/schema 11): both
  moved and saw each other while eight incomplete joins and eight forged-body
  requests were open, and health stayed 200. This measured under 200 ms of
  hostile-phase native progress, not a four-second soak, rendered play, remote
  abuse or the mixed gameplay/stability gate; the session has no fixed duration minimum.
  T4C-0251 added a tamper battery, local PG16 WSL, 322 cases, not the integrated release build
  ([NATIVE_TAMPER_PROBE.md](tools/NATIVE_TAMPER_PROBE.md)): one attacker session over raw HTTP and one victim on the
  native adapter, covering foreign account-key/character joins, missing and garbled authorization, admin routes,
  stale revision and epoch on every mutating route, replays and reordered input, oversized and malformed bodies,
  forged targets and foreign items, corpses and owner-window loot, stat/gold/flag/item edit attempts, speed and
  collision. 321 passed with no committed state changed and the victim playing throughout; 1 failed (`/recover`
  reports a definite corpse-item refusal as `503 busy`, state unchanged; fixed since by PR #209). After server movement
  parity (T4C-0288) the same 322 cases all passed (322 PASS, 0 FAIL). Combat-time
  forgery, enabled-portal transfers, rendered clients and remote/TLS abuse remain open, so this bullet stays unchecked.
- [ ] Restore a backup into a separate database and finish a native gameplay/reconnect check there;
  matching table hashes alone do not satisfy restored gameplay acceptance. T4C-0213 verified
  a live schema-10 exported-snapshot restore across all eleven online tables, including
  PostgreSQL-reparsed equivalent CHECK definitions; both new loot tables were empty. Two
  actual native network adapters subsequently rejoined the isolated restore and read matching
  committed progress, balances and positions after real lease expiry, then left normally.
  This was a network read check without rendered gameplay, new loot, combat or travel on the
  restore, so the full playthrough gate remains open.
- [ ] Run a mixed session with two real rendered clients on the integrated candidate, with zero
  unexplained disconnects, crashes, invariant violations, duplicate value or lost acknowledged
  progress. Record its duration; there is no fixed minimum. Ending a session alone does not pass
  this gate: the required scenarios and evidence must be complete and satisfy the criteria.
  Include combat, loot, quests, travel and intentional reconnects rather than idle connections.
- [ ] Record actual tick duration/debt, CPU/memory, bandwidth, database/command latency, occupancy
  and every error/recovery. Investigate stalls and growing queues/resources; do not count a failed
  run as passed merely because it ended. T4C-0198 recorded a two-hour native-adapter baseline,
  but 20-player occupancy lasted only 58.213 minutes and the run lacked quest/loot/travel,
  bandwidth and database-latency measures. That historical 20-adapter run does not satisfy the
  current two-rendered-client mixed Beta/GA gate; further 20-player scale qualification is
  follow-up evidence, not a release gate. T4C-0315 (2026-10-29, `b19beb34`) ran the two-hour,
  twenty-adapter soak again on the current build with eight adapters taking paced portal trips:
  49.6% of the time at twenty (the default reconnect churn's ceiling), 98.2% at nineteen or more,
  244,928 ticks with 0 failed and 1 over budget (max 35.9 ms), flat CPU (36 to 37% of a core) and
  heap, 0 rollbacks, 0 persistence failures, 625 observed region transfers with 0 failures, but
  2 adapter failures (a rejoin-lockout class defect and a guard that disagreed with the movement
  rule, both fixed; a 30-minute rerun on the fix had 0 failures and 95.8% at twenty). It records
  application body bytes (about 2.4 MB/s) but not wire bandwidth or database operation latency, so
  this bullet stays unticked; see [tools/NATIVE_ADAPTER_SOAK.md](tools/NATIVE_ADAPTER_SOAK.md).
  T4C-0221 repaired only Windows report replacement
  under a focused file-lock fixture; the earlier 66-second schema-11 smoke remains failed and
  no new sustained result is claimed. T4C-0227 adds bounded private operator
  counters for application body bytes and handler lifetime. The native soak
  consumer's finite 1 MiB cap covers the measured dense fixed-label schema
  while retaining its two-second whole-response deadline; wire bandwidth,
  database latency and a sustained integrated candidate run remain unmeasured.

Done evidence: reproducible fault/abuse scripts, separate restored-database playthrough, and the
completed two-player report tied to exact client/server/content/schema versions. HTTP reply faults
are not real packet-loss or rendered-client evidence. See [BACKUP.md](BACKUP.md),
[NATIVE_NETWORK_FAULTS.md](tools/NATIVE_NETWORK_FAULTS.md) and [LOAD_METRICS.md](LOAD_METRICS.md).

## M6 — Distribute and accept the real game

T4C-0215 supplies a checked, create-only **local loopback server QA package** alongside the earlier client package. Its generated operator scripts passed preview and persistent schema-10 start/health/private graceful stop on a fresh isolated database, including a copied path with spaces and PowerShell 5/7 checks. This is partial operational evidence; clean-machine, trusted remote deployment, full rendered playthrough and owner acceptance remain unchecked.

**Clean-machine rehearsal on one machine (T4C-0311, 2026-10-29, source `f3d4ed62`).** Both QA packages were rebuilt from that commit
and passed their headless integrity checks and fixture negatives (tamper, private file, duplicate and traversal paths). Each was zipped,
extracted to a new empty directory with spaces outside the repository, and run from a scrubbed process environment (no repository on `PATH`
or working directory, no inherited `T4C_*`, JDK-only `PATH`, fresh profile and `TEMP`) against a disposable PostgreSQL 16 cluster. The packaged
server reached persistent health and a graceful exit 0; two private profiles were provisioned with the packaged JAR; the packaged client
launcher started the real client, which connected to the game socket and closed with exit 0; the package directories were unchanged apart
from the client's `logs/`; no credentials or developer paths were found in either package. **No second physical machine, other user
account, other Windows build, antivirus/SmartScreen, firewall prompt, GPU or remote HTTPS endpoint was involved**, so no M6 bullet is
ticked. It found and fixed four Windows PowerShell 5.1 defects (builder `GetRelativePath`, `java -version` stderr, negative-test stderr and the
client launcher dying on the client's first stderr line, which made `launch-online.bat` unable to start the client); the 5.1 shell is the only one
on a stock Windows machine. Details: [CLIENT_DISTRIBUTION.md](CLIENT_DISTRIBUTION.md) and [SERVER_DISTRIBUTION.md](SERVER_DISTRIBUTION.md).

**What a real clean-machine delivery still needs (unchecked, for the second-machine pass):**

- [ ] Java: the packages bundle no JRE. Either bundle one (jlink or a vendor runtime, with its licence and update path) or document the Java 21 x64
  requirement and a verified install step, and have the launcher print a plain message when `java` is missing or older than 21.
- [ ] Execution policy / Mark of the Web: stock Windows refuses unsigned `.ps1` files (observed: "running scripts is disabled"). Docs now give
  the `-ExecutionPolicy Bypass` and `Unblock-File` forms; a `.bat` wrapper for the server and a code-signing decision are still open.
- [ ] SmartScreen and antivirus: the client ships unsigned scripts and a large tree including `assets/origin/` (original-game `.exe`/`.dll` files,
  about 290 MB, observed unused by the online client). Decide whether to stop shipping that folder, and test on a machine with Defender real-time protection.
- [ ] Size and transfer: about 640 MB extracted client; choose a download/update channel and verify the manifest after transfer.
- [ ] Firewall and loopback: only the local-loopback launcher is rehearsed. A second machine needs the server bound to a reachable address,
  HTTP port plus the game socket (HTTP port + 1) allowed, and no Windows Firewall prompt left unanswered.
- [ ] Remote HTTPS trust: a publicly trusted certificate and matching host name per [TLS.md](TLS.md), the client reaching it from another network,
  and a clear client error for an untrusted or mismatched certificate.
- [ ] GPU and drivers: the client needs OpenGL 3.x; behavior on integrated/older GPUs and remote-desktop sessions is unknown.
- [ ] Per-launch residue: the pinned GLFW extraction directory under `TEMP` is not removed on exit.
- [ ] Repeat all of the above on a second physical machine (or a clean VM) with a different user account, record screenshots/logs without credentials,
  and obtain owner acceptance.

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
