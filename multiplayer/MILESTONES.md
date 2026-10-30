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
| M1 — Enter and interact | Roster/NPC backend and actual adapter probes pass, including two local HTTPS adapters with trust/hostname refusal and a separate live account-key rotation check; selection and refusal messages verified rendered; creation bullet and remote HTTPS open | T4C-0192, 0195, 0196, 0216, 0219, 0232, 0233, 0234 | Native character select and its Create form are merged as an opt-in lobby (`-Lobby`, owner-approved look) and ran rendered against a live local server (T4C-0306 to T4C-0325; four of seven bullets ticked); in-client character creation is recorded here as out of Beta scope (characters are operator-provisioned) while the creation bullet still requires it: owner decision; remote deployment |
| M2 — Inventory and loot | Server ground loot (drops, owner window, first-claim-wins, expiry, carry-weight rule), per-item corpse recovery within carry limits, native pickup/equip/inventory panels; all merged with CI evidence | Existing inventory/equipment work; T4C-0201, 0202, 0204, 0218, 0228, 0341 | Every bullet ticked (T4C-0306, 0312, 0318, 0341); the milestone's single integrated Done run, the corpse control and single-stack recovery from the screen, and remote or clean-machine evidence are open |
| M3 — Complete authored adventure | Quest service rules, flag allowlist, once-only turn-in rewards, native talk panel, and the Skeleton hunting ground hosted as one world-0 region with unhosted portals refused; merged | T4C-0193, 0194, 0195, 0206, 0218, 0231, 0341 | Three of eight bullets ticked by T4C-0342 (kill credit, both players completing, region boundary); open: scope freeze, required-item quests, the unhosted-route refusal (no walkable unhosted route exists), restart with delayed intents; the Goblin-to-Wild-Horse fight (owner accepted it as not simulated for Beta) |
| M4 — Supported combat and abilities | Restricted melee/physical retaliation, server-authoritative patrol/pursuit with guarded persistence, two-native-adapter pursuit/restart proof, brief two-client pursuit/retaliation/reconnect check, local PostgreSQL proof of authored hostile-clan spell damage (T4C-0317), durable authored monster self-healing (T4C-0319) and ranged monster attacks (T4C-0320) | T4C-0189, 0190, 0209, 0210, 0211, 0212, 0229, 0230, 0236, 0237, 0317, 0319, 0320, 0330 | T4C-0331 adds a rendered two-window run of different roles (contention, spells, buff and rejoin, refusals, death seen from the other window) and ticks three of the seven M4 bullets; pursuit of a fleeing player, sanctuary/interruption validation evidence, buff/cooldown HUD, tile-aimed area spells, stat allocation, rendered clan-spell combat and integrated M4/Beta acceptance remain open |
| M5 — Reliability and anti-cheat | Pool/shutdown/fault probes, local HTTPS trust/hostname refusal and two-adapter reconnect, live two-account key-rotation fencing, historical two-hour native-adapter baseline with partial 20-player occupancy, isolated restart and database-crash probes, twenty-adapter forced-kill, PostgreSQL-crash and lost-acknowledgement drills (T4C-0310; database-recovery bullet ticked), brief rendered reconnect pass, and a 12-minute mixed twenty-adapter soak with eight adapters taking paced portal trips (49 region transfers, zero failures, 56.0% at twenty; T4C-0308), a two-hour twenty-adapter soak with eight portal walkers (625 observed transfers, flat CPU/heap, 2 adapter failures traced to a rejoin-lockout defect now fixed, 30-minute steady rerun clean; T4C-0315); shutdown-ownership, restored-database play, full-metric 30-minute record (socket bandwidth and database operation latency) and tamper/key-rotation bullets ticked on native-adapter evidence (T4C-0350); lost-acknowledgement for quest turn-in/death/transfer, rendered restart and the two-real-client mixed session open | T4C-0191, 0197, 0198, 0207, 0208, 0216, 0219, 0308, 0310, 0315, 0345 | Broader gameplay recovery (quest turn-in, death, transfer kills), rendered restart, two-real-client mixed soak on the release candidate |
| M6 — Package and native acceptance | Create-only client and local-loopback server QA packages, headless integrity checks, isolated persistent server stop and brief two-window local client smoke pass; clean-machine, remote and full rendered playthrough open | T4C-0205, 0215 and cross-cutting work | Clean-machine delivery and complete two-rendered-client playthrough |

Task IDs not yet claimed are deliberately not represented as completed or assigned work.
Independent implementation can proceed in parallel; acceptance must use an integrated build.

## M1 — Enter the online game and interact

Required behavior:

- [x] Each player uses a separate online account, with no desktop character/save import.
- [x] The native client discovers only that account's characters, displays availability and lets
  the player select one. Pagination, an empty roster and unavailable characters remain usable.
- [ ] Native character creation uses shared name/slot/class rules and server-generated initial
  stats, items and gold. A retried or interrupted request cannot create duplicates or regrant a kit.
- [x] Invalid keys, foreign characters, a character already in use and incompatible versions give
  usable errors. Selection alone acquires no gameplay lease or player slot.
- [x] Kilhiam appears using authored appearance/location. The native talk control sends identity
  only; range, line of sight, region, readiness and current revision are server decisions.
- [x] Authored English dialogue is readable in the client. First-letter item/flag commits together;
  repeated greetings, reconnect and a lost response never grant another copy.
- [ ] Credentials are absent from logs, public snapshots and ordinary preferences. Any non-loopback
  connection uses trusted HTTPS with hostname verification; no plaintext or trust-all workaround.
  T4C-0216 passed local two-adapter HTTPS and separate standard-JSSE unknown-CA/hostname
  refusals; remote deployment and rendered-client acceptance still require verification.

T4C-0306 (2026-10-29, local rendered, not remote): two real windows each selected their own operator-provisioned
character in the lobby, drew each other, walked to Kilhiam and clicked him through the screen's own NPC click handler
(each got exactly one letter; a repeat click and a greeting after a server stop/restart gave none); an out-of-range
click was refused by the server. That ticks the Kilhiam appearance/talk bullet above. The dialogue bullet stayed open at
that point because the lost-response case had not been driven; T4C-0310 and T4C-0325 below close it. See
[CLIENT_UNIFICATION_DESIGN.md](CLIENT_UNIFICATION_DESIGN.md) 3.15 and `multiplayer/design/live2-*.png`.

T4C-0325 (2026-10-30, local rendered, one disposable PG16 cluster per run, not remote; details and per-case results in
[CLIENT_UNIFICATION_DESIGN.md](CLIENT_UNIFICATION_DESIGN.md) 3.18 and [ACCEPTANCE.md](ACCEPTANCE.md)) maps each bullet to its
evidence and ticks four of the seven:

- **Separate account, no desktop import (ticked).** Every rendered window in T4C-0306/0312/0318/0323 used its own
  operator-provisioned account (this pass: six accounts, one process each); a character moved to another account vanished from
  the first account's roster on the next refresh; `RemoteSessionWritesNothingTest` (5 tests) shows the remote session writes no
  desktop save and cannot reach the local account vault; PARITY.md records operator-provisioned characters.
- **Roster, availability, pagination, empty, unavailable (ticked).** One account with 25 characters (20 per page, so two pages)
  listed all 25 in order with every status label (Ready, In use, New, Region offline, Respawning) and scrolled to the last row;
  an account with none showed the empty-roster text and Enter opened no connection; selecting a Region offline and a
  Respawning character and pressing Enter gave a message naming the state and acquired no lease. `OnlineLobbyModelTest`
  (14) covers paging, order, cursor and truncation.
- **Usable errors, no lease from selection (ticked, after a fix).** Rendered: an invalid key ("The server did not accept this
  account..."), a character in use in another window ("already in use ... wait about 30 seconds"), a character moved to another
  account under an open lobby (generic "could not be entered ... not on this account"), a client built with protocol 8 against a
  protocol-7 server ("different versions ... update together"); the lobby holds no lease from selection alone (database lease
  count 0 after 8 s of selection, and no connection object exists until Play). Before this pass a refused join dropped the player
  back to the lobby with no message and a version mismatch read "could not reach the server" (fixed, `OnlineJoinFailureTest`,
  `OnlineLobbyScreenTextTest`). The server deliberately answers an unknown key, another account's character, a missing character
  and one in use with the same refusal (no account oracle), so the foreign case can only be reached through a stale roster and
  its text lists the possibilities. "No player slot" is shown by the lobby opening no join request; the server's capacity
  counter was not sampled.
- **Dialogue, first-letter commit, repeats, reconnect, lost response (ticked).** Readable: the parchment greeting and the talk
  panel in T4C-0306/0312 screenshots. Letter and flag commit together and a receipt retry cannot duplicate:
  `OnlineNpcInteractionsTest.questFlagAndLetterCommitTogetherAndReceiptRetryCannotDuplicateReward` and `OnlineNpcHttpTest`
  (PostgreSQL). Repeats and reconnect rendered in T4C-0306 (repeat click and a greeting after a server stop/restart gave no
  second letter). Lost response: T4C-0310 `LostAck` produced both outcomes (reply lost before the commit, and reply lost after it)
  and every character ended with exactly one letter and one receipt. That drill used native adapters, not a rendered window,
  and the client never replays a gameplay request; this is the one judgement call in the four ticks.
- **Native creation (left open, owner decision).** The lobby now has a working Create form (T4C-0272 and the lobby creation
  work). This pass drove it rendered once as extra evidence (the form, one Archer typed and created, the roster listing it,
  entering the game with the server-generated kit: 46 inventory stacks and 204 gold) and the server-side idempotency is covered
  by `OnlineCharacterCreationTest` (PostgreSQL) and `OnlineCreateFormTest` (7). It is not ticked because the status table above
  records in-client creation as outside the Beta scope while this bullet still requires it; the owner decides whether to keep
  the bullet (then this evidence, plus one interrupted-request drill rendered, would support ticking it) or to rewrite it as
  operator-provisioned. The "Done evidence" line below (both clients create) is blocked by the same decision.
- **Credentials and HTTPS (left open).** Credentials: after both runs a scan of every driver and server log, the preference file
  and the chat log for the six exact account keys (and for any 64-hex string in the run logs) found none; existing tests cover the
  profile, credentials and lobby `toString`, the roster, creation, join and metrics responses (`OnlineAccountProfileTest`,
  `OnlineLobbyModelTest.toStringNeverContainsTheAccountKey`, `AccountRosterTest`, `GameSocketServerTest`, `PreviewServerTest`,
  `HttpTrafficMetricsTest`). HTTPS: a source search finds no trust manager or host-name verifier override and the client
  sets HTTPS endpoint identification on every game socket; `ServerTransportTest` (2) and `OnlineEndpointTest` (2) passed again
  here; T4C-0216's two-adapter probe is unchanged (not re-run: it needs its own `t4c_tls_0216_*` database). New: the rendered
  lobby over a local HTTPS server with a one-day fixture certificate listed 25 characters and entered the game with the
  certificate trusted, and showed "Could not reach the online server" with the certificate untrusted and with `127.0.0.1` against
  a `localhost` certificate. Still unproved: a genuinely non-loopback deployment with a certificate chain a client trusts.
  Left unticked until that deployment check exists.
- **Operator-profile compatibility path.** The profile file format is unchanged and every run above used operator-provisioned
  profiles; `OnlineLobbyLaunchTest.defaultLaunchIsDirectAndArgumentsAreUntouched` and `OnlineAccountProfileTest` (a
  character profile keeps direct launch and can prefer lobby selection) cover the retained direct path; T4C-0212's rendered
  check and T4C-0311's packaged-client rehearsal used it live.

T4C-0343 implementation evidence (local only): the existing six online HUD buttons now route clicks to their current actions; completed Renewed Wards greetings use the authored completed line in offline and server paths. Focused tests and both packages pass. No rendered-client check has been run for this delivery, so M1 remains open.

Done evidence: both rendered clients create/select their own characters, connect, converse, close
and reconnect. Retain operator profiles as a compatibility path. See [ACCOUNT_LIFECYCLE.md](ACCOUNT_LIFECYCLE.md)
and [NPC_INTERACTION_DESIGN.md](NPC_INTERACTION_DESIGN.md) for current contracts and limits.

## M2 — Use inventory and receive loot

Required behavior:

- [x] Native inventory shows committed item identities, quantities, charges and equipped slots.
- [x] Equip/unequip updates authoritative bonuses and the required appearance; shared attribute
  requirements, occupancy, weight, uniqueness and other applicable item rules remain intact.
- [x] Supported monsters produce their authored drops through a server-owned persistent loot path.
- [x] Pickup uses server position/range, current region and the declared ownership/claim policy.
  Two simultaneous claimants cannot both receive the same item or currency.
- [x] Capacity or eligibility refusal preserves the loot and original inventory; partial stack
  operations conserve quantity and preserve appropriate identities/charges.
- [x] Death/corpse recovery conserves protected equipment and dropped contents under shared rules.
  No alternate pickup path bypasses corpse ownership or duplicates recovered contents.
- [x] Reconnect, restart and a lost pickup acknowledgement retain exactly the committed outcome.

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

T4C-0318 (2026-10-29, local rendered, two level-30 warriors, `d4eff350` plus this branch, details in
CLIENT_UNIFICATION_DESIGN.md 3.17): Skeleton drops (Light Healing Potion, Leather Gloves, Healing Potion) appeared in both
windows with the owner window counting down in both committed views; a claim race inside the window (owner took it, the other
window got "Another player has the first claim"), a second after the window (one took the gloves, the other was refused "You cannot
carry that much weight" with loot and inventory unchanged), a third that lost to a committed claim ("Another player picked that up
first"); the database holds one receipt per claimed drop and none twice. A death left a corpse and a recovery sent through the
connection returned its three stacks exactly (counts conserved, worn gear unchanged, corpse row emptied). The one window took its
legs armour off and on and the other window's snapshot and drawn model followed, name and outline shown on hover. **Ticked from
this and the existing server tests:** supported monsters' authored drops through the persistent loot path; pickup position, range
and ownership with a simultaneous claim; capacity refusal preserving loot and inventory (partial-stack conservation stays the
server tests'); corpse recovery conserving contents and gear. **Still open, precisely:** the native inventory bullet (the new inventory
window was driven but no screenshot of it was kept), equip bonuses and the equipped appearance on the *own* screen were not
inspected, the screen has no corpse control (the recovery went through the connection) and no owner-window display, a restart or lost
acknowledgement during a claim or recovery was not run, so the reconnect/restart bullet and the Done evidence's "reload from PostgreSQL"
step remain unmet.

T4C-0342 (2026-10-30, local rendered plus native adapters, PostgreSQL 16 disposable clusters, details in
CLIENT_UNIFICATION_DESIGN.md 3.19): **Ticked, and what proves each.** *Native inventory:* the inventory window (`I`) was driven on the real
screen and kept as two pictures (`multiplayer/design/live6-inventory-window-page1.png`, `-page2.png`): every row names the item
(Leather Pants, Recruit's Mace), the equipped slot in brackets (LEGS, WEAPON), the quantity ("Healing Potion x20") and the charge count
("(1)"). *Equip bonuses and appearance on the own screen:* with body armour off the character sheet's Armor Class fell from 3.10 to 1.65 (the
sheet shows 1) and with the weapon off Strength fell from 43 to 40 (the Recruit's Mace boost); both returned to 43 and 3.10 when the items went
back on; the own figure lost and regained the armour (`live6-own-model-*.png`, `live6-character-sheet-body-armour-off.png`). **This found a
defect, fixed here with a test:** before the fix the sheet read 43 and 3.10 throughout, because the own screen's player kept the join-time gear
(only the vitals were refreshed); `RemoteOwnLook` now follows every committed equipment change (`RemoteOwnLookTest`). Requirements, occupancy, weight
and uniqueness stay the shared rules' and the server tests' (a level-30 Warrior with Strength 43 got no change from the Strength-100 ring; equipping a mace
into the taken weapon slot swapped it). The other window's view of an equip change is T4C-0318's. *Restart and lost acknowledgement:* the new
`NativeLootFaultDrill` hard-killed a real server 18 times against one ground-drop claim or one two-stack corpse recovery each, 9 delays from
0 to 600 ms, through the real adapter: after every kill the database held exactly the applied or exactly the unapplied outcome (claim: unapplied for
0 to 200 ms with the reply lost, applied for 350 ms with the reply lost and for 600 ms acknowledged; recovery: unapplied 0 to 120 ms, applied for 200 and 350 ms
with the reply lost, and for 600 ms acknowledged); after the restart and lease expiry the same request was accepted once for every unapplied case and refused
("unavailable") for every applied one; the final state held the potion once (or the two stacks and 50 gold once), one receipt per claimed drop, the drop claimed by the
claimant and the corpse recovered, no duplicate item ids, and the last server stopped gracefully with exit 0. Reload-from-restart of the
inventory and quest items was also seen in the rendered run. **Not done, still true:** the drill is native adapters, not rendered clients; the corpse
control on the screen and an individual single-stack recovery from the screen were not re-run (T4C-0321, T4C-0339 are merged separately); no
remote or clean-machine run. With these three boxes every M2 bullet is ticked; the milestone's single Done run (earn, pick up and equip in one
playthrough while the other window watches) is still spread over T4C-0306, 0312, 0318 and this task, so the owner decides whether to call M2 done.

Done evidence: player A earns/picks up/equips loot while player B observes the shared world;
repeat with a simultaneous claim and an inventory refusal. Reload from PostgreSQL and verify
the final items/currency exist in exactly one permitted place. General loot is distinct from
the already implemented owner-corpse service.

## M3 — Complete one authored adventure and travel safely

Required behavior:

- [ ] Freeze the actual quest/content scope above; export/validate its references from existing
  definitions instead of inventing a shorter online version.
- [x] Native dialogue can accept the quest, show its objectives/progress and turn it in.
- [x] Server combat supplies authoritative kill credit under the declared two-player policy.
  Replayed kills, old creature lives and unrelated players do not corrupt progress.
- [ ] Required quest items and turn-in consumption resolve by real item identity/key. Item,
  currency, XP, ability rewards and completion state commit together, exactly once.
- [x] Both players can independently complete the selected adventure without blocking each other
  through a stale flag, consumed shared objective, duplicated claim or unsupported-state guard.
- [ ] Every required map/encounter is hosted. (Owner decision 2026-10-30: reach the rest of the world by raising the
  region bound with area-of-interest streaming; plan and stage gates in [WORLD_STREAMING_DESIGN.md](WORLD_STREAMING_DESIGN.md),
  T4C-0349, design only so far.) Authored entrances/returns and valid respawns work;
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
- [x] Native map assets switch before movement resumes. Players in separate regions cannot see,
  target, damage or recover items from one another across that boundary.
- [ ] Logout/rejoin and server restart preserve objective state, rewards and destination; delayed
  intents from a prior region cannot act on a new region's local entity IDs.

T4C-0312 (2026-10-29, local rendered, one quester, not the two-player policy): the `renewed_wards` offer was accepted
through the connection (the screen has no topic control), the screen's own monster clicks credited 12 Skeleton kills, and
a greeting turned the quest in once: status 2, +500 gold, +400 experience and `spell.renew_armor` in one commit, with a
repeat greeting changing nothing. The giver and the ground are 160 tiles apart, so the operator moved the character between
two server restarts (travel was not driven), the quest has no required item, and only one player did it; no M3 bullet is
ticked (see CLIENT_UNIFICATION_DESIGN.md 3.16).

T4C-0318 (2026-10-29, local rendered, two windows): both windows accepted `renewed_wards` through the screen's own NPC topic window
(click the Warden, digit 1; status 0 to 1 committed, the objective text "Put down twelve of the skeletons ..." shown), both walked
165 tiles to the Skeleton ground on foot (no operator relocation this time), and each character took Lighthaven door 16 and 17 and
the cave mouth and exit with the other window watching: the map switched, the walker vanished from the other's view and reappeared in
the new region with the right look; 8 hops, 0 timeouts. **Ticked:** "Native dialogue can accept the quest, show its objectives/progress
and turn it in" (accept and progress now through the screen, the turn-in through the screen's greeting click in T4C-0312). **Still open,
precisely:** kill credit and turn-in for the second window, a repeat turn-in and a restart/lost-response check in the same run, the
unhosted-route refusal, and targeting, damage or recovery across a region boundary (only mutual invisibility was observed), so the
travel, map-switch, both-players and restart bullets stay unticked.

T4C-0342 (2026-10-30, local rendered, two level-30 Warriors, details in CLIENT_UNIFICATION_DESIGN.md 3.19): both windows accepted `renewed_wards` through the topic
window, took the Lighthaven door pair (the map switched on screen, the other window watched each walker leave and arrive), walked to the Skeleton ground and
fought until each character's own counter read 12 (credit goes to the lethal blow as declared; the counters were 12 and 12 although both attacked the same
creatures); each then walked back to Warden Aelric and turned in once: the database holds one reward row per character (500 gold, 400 experience), status 2,
`spell.renew_armor` known, and every later greeting (two in the same session, one after a graceful server restart) changed nothing. After the restart both characters
came back with the same status, kills, gold, experience, inventory and position. One window then entered the cave while the other stayed out; neither saw the other
(drawn players 0), the cave window's attack on the outside creature's id was answered "no target" (that id does not exist in the cave) and, in a second run, "busy", the
outside window's attack on the cave window's id was refused "out of reach" (ids are region-local: the same number names another creature in each region), the
target's hit points did not change in either region, and the cave window's pickup of the outside window's ground drop was refused with the drop still on the ground.
**Ticked:** kill credit, both players completing, and the boundary bullet (map switch plus no seeing, targeting, damage or pickup across it; a foreign
corpse cannot be recovered at all because recovery is owner-only, `NativeTamperProbe`/T4C-0251). **Not ticked and exactly why:** *scope freeze* (a design decision, not
run evidence); *required quest items* (`renewed_wards` has no required item, so item identity and consumption stay with the server tests; its currency, experience, spell and
completion commit together exactly once, as seen); *hosting and the unhosted-route refusal* (the only authored portal in the starter region whose route is not enabled,
portal 947 at 2964,1038 toward world 1, sits in a pocket no hosted tile reaches, found by flooding the hosted map, so no player can walk into an unhosted route and
there is nothing to refuse in play; the explicit refusals that do exist are the join and respawn ones, shown in the lobby as "Region offline" in the T4C-0325 run; whether that satisfies the
bullet or an in-world refusal text is wanted is the owner's call); *restart and delayed intents* (restart preserved objective state, rewards and position for both
characters, but the delayed-intent half rests on the stale-epoch battery of T4C-0251, not re-run on this build, so it stays open). Also not driven: a lost response during the
turn-in itself (T4C-0310 produced both outcomes for the opening-NPC reward through the same reward path).

T4C-0343 adds the completed-state dialogue response to the online quest greeting without changing reward or progress state. The local server regression passes; this is not rendered quest-loop evidence, so M3 remains open.

Done evidence: two rendered clients follow the declared quest from acceptance through travel,
combat and turn-in; both progress and reconnect with their own exact rewards. Include one
repeat turn-in and a lost-response/restart recovery check. See [MAP_TRANSFER_DESIGN.md](MAP_TRANSFER_DESIGN.md).

## M4 — Play the supported combat and ability set

Current evidence (T4C-0323, PR #285 merged): learned single-target heal effects route to another live nearby player through the game socket; atomic caster/recipient persistence and the PostgreSQL-backed socket route pass focused tests. No rendered-client evidence yet; this does not complete M4.

T4C-0344 implementation evidence: learned online Sneak now commits concealment and its cooldown; movement upkeep uses the authored skill/stat/witness checks, and melee, ranged attacks and spell casts reveal it. Focused server tests passed 19 total (0 failures/errors, 6 skipped, including database-gated cases), focused client tests passed 49/49, root and server package builds passed, and `git diff --check` passed. The PostgreSQL persistence/observer case and rendered-client verification were not run. M4 and Beta remain open.

Required behavior:

- [x] Declare classes and required abilities above. A melee-only networking preview cannot be
  presented as a functional caster or as complete launch combat. (T4C-0331: the approved
  [SCOPE_PROPOSAL.md](SCOPE_PROPOSAL.md) section 1 declares Warrior and Paladin with Light Heal, and
  [PARITY.md](PARITY.md) widens it to six online classes; casting is shown live by a Healer, a Mage and the Paladin's list, see the T4C-0331 note below. The per-role native scenario is the Done evidence, still open.)
- [x] Required monsters acquire valid targets, pursue where authored, attack/retaliate, lose or
  reset targets under the declared rules, die and respawn with durable life identities.
  (T4C-0334: the whole server world step is tested against single-player's rules: acquisition, a long
  retreat among many patrollers, reset beyond the leash without healing, and every hosted Skeleton on
  the real maps. The live walk-away finding was a parity defect and is fixed; see the T4C-0334 note below.
  A rendered re-run of the Skeleton walk-away is recommended follow-up evidence, not a gate.)
  [MONSTER_PURSUIT_DESIGN.md](MONSTER_PURSUIT_DESIGN.md) records the T4C-0209 architecture.
  T4C-0210 extracted offline-preserving scalar decisions; T4C-0211 chooses native facing/walk
  from committed position deltas. T4C-0212 delivered guarded server movement and a two-adapter
  checkpoint/restart probe. A separate brief rendered two-client session observed the authored
  guard move and retaliate after a player pressed Space, followed by that player's reconnect with saved
  HP/gold/location. It did not test a long retreat, every facing, reset, death/respawn, all roles
  or the full M4 playthrough; details are in [ACCEPTANCE.md](ACCEPTANCE.md).
- [x] Server validates movement speed/collision, attack/cast timing, range, sight, sanctuary,
  resource costs, attribute eligibility, effects and any applicable interruption rules.
  (T4C-0334 adds the last two missing pins, sanctuary and the stun interruption, through the real
  melee and spell paths; the rest was already covered, see the T4C-0331 and T4C-0334 notes below.)
- [ ] Required skills/spells, mana, charges, buffs, cooldowns and advancement are usable through
  native controls, persisted appropriately and reflected by committed HUD/appearance state.
- [x] Both clients observe consistent combat, deaths and respawns. One player's action does not
  damage or award another player incorrectly, and disconnect cannot leave an immortal target.
  (T4C-0331 live, two windows: identical creature health sequences, the lethal blow's owner alone gets the death experience and the database agrees, a death seen from the other window, a leaving player's wounded target killed by the other; details below and in CLIENT_UNIFICATION_DESIGN.md 3.18.)
- [x] Quest/story flags supported by M3 continue to work with combat; arbitrary unsupported
  scripts remain explicitly refused rather than bypassing authority checks.
  (Quest kills and the turn-in with a level-30 warrior: T4C-0312 live; unlisted flags, scripted monsters and companions refused: `OnlineQuestFlagsTest`, `OnlineMeleeTest`; a character with an unlisted quest flag refused every attack live in T4C-0331.)
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

T4C-0318 (2026-10-29, local rendered, warriors only): two windows fought Skeletons for 19 minutes (about 750 attack clicks each)
with idle stretches, one player stood among them until it died and respawned in the temple, and the other window's player list
stayed consistent; the Healing Potion (not only the Mana Potion) is not offered by `U` online, so the potion part of the bullet is
further from met. No M4 bullet is ticked: no other role, no contention for one target, no leaving reach/sight scenario, and the
death was not shown from the other window.

T4C-0331 (2026-10-30, local rendered, three role pairs: Warrior + Healer, Archer + Healer, Paladin + Mage; `main` 155c03a5 plus this branch,
detail and exact limits in CLIENT_UNIFICATION_DESIGN.md 3.18): both windows contended for one creature life at a time (identical
health sequences in both; the lethal blow's owner got the death experience, the other kept per-hit experience, the database's
reward rows agree); the Healer cast Stone Shard at a selected creature, Light Heal on itself and a timed Protection buff through `C`; the
buff kept its remaining time across a graceful server restart and rejoin (logged-out time did not run it down); the Mage cast Fireball
(accepted after a fix); a Warrior was refused a bow it cannot wear, an Archer without its quiver and a character with an unlisted quest flag were
refused "You cannot attack that yet."; a retaliating Skeleton hit a Warrior 28 times (103 points) and the figure was outlined beside it; a wounded Skeleton kept its
hit points when the player left through a portal and across a restart; a Warrior who left the world left no immortal target (the Healer killed it);
a Warrior's death was watched from the other window (public hit points falling, then the figure at the temple with half health), its one corpse
was recovered by clicking the drawn corpse. Four defects were fixed with tests (taking off a quiver dropped the connection, raw message keys, casts and potions lost to a
"busy" refusal, Fireball not castable from the list).
Ticked: the declaration, the two-client consistency and the quest-flag bullets. **Not ticked, with the exact missing evidence:**
- *Required monsters acquire, pursue, reset...*: the retaliating Skeleton did not follow a player walking away, so "pursue where authored" for the
  Skeleton ground is unexplained (authored hunting-ground leash or a defect: the pursuit lane's RCA), and natural acquisition of an unprovoked player was
  not shown on the screen (`OnlineMonsterPursuitTest` and the T4C-0212 probe cover both).
- *Server validates movement, timing, range, sight, sanctuary, costs, eligibility, effects, interruption*: movement speed/collision (tamper battery, T4C-0288, 322 PASS), swing and
  cast timing, range, sight, mana costs, equip requirements and effects have tests or live evidence, but no test pins a sanctuary refusal through the server's melee and spell path and no test
  pins an online interruption (stun) of a player's attack or cast.
- *Skills/spells, mana, charges, buffs, cooldowns and advancement*: spells, mana, cooldowns, a buff and its persistence are shown live, but there is no buff or cooldown display on the HUD, multi-charge items are refused, area spells aimed at a tile (20 spells) and area heals cannot be
  cast from the spell list, and stat allocation has no online control.
- *Level progression/stat allocation and class behavior*: progression is covered by `OnlineProgressionTest` and live experience gains, but unspent stat points cannot be spent online and no old-save/single-player-unchanged check was part of this pass.

T4C-0334 (2026-10-30, local tests on a disposable PG16 cluster, no rendered client; on `main` c009f9ed): **the live finding "a retaliating Skeleton did not follow a player walking away" was a parity defect, not an authored rule.**
Root cause: single-player judges creatures by the static foot collision of a tile (`CollisionManager.hasStaticCollisionAtGrid`: the A* in `Pathfinding` and `BaseMonster.canMove`), and the Skeleton has no hunting-ground rule (`HuntingGrounds.at("Skeleton", ...)` is null; the hunting leash applies only to the authored hunting-ground types, and the retaliation leash is 60 tiles). The server judged creatures, their A* and the chased player by the player's stricter one-tile-above fold (`PreviewMap.canStand`), while players themselves have rested by the foot rule since T4C-0315 (`canRestOnGrid`). A player standing directly under a wall (any tile whose upper neighbour is blocked) was therefore "not eligible" to the Skeleton: no natural acquisition, an immediate reset of a retaliation chase, and no path through gaps single-player lets a Skeleton use. Creatures, saved creature positions, patrol goals and the chased player now use single-player's rule; spawn homes keep the stricter check so the unhostable-spawn list is unchanged.
Proved by tests: `OnlineMonsterPursuitRealMapTest` (no database; every hosted Skeleton on the real starter and cave maps, provoked, follows a player standing 6, 12 or 20 tiles away in 16 directions on every tile single-player's A* reaches: 158 placements, 5 of which failed before the fix); `OnlineMonsterPursuitFootRuleTest` (a player under a wall is followed and, unprovoked, noticed; a creature may stand and be saved there but never on a wall or off the map); `OnlineMonsterPursuitWorldStepTest` (PostgreSQL, through `HostedWorlds.step` with 120 other patrolling Skeletons competing for the single path permit per tick: a provoked Skeleton closes on a player who walked 11 tiles away; an unprovoked player inside 8 tiles is acquired and persisted as the target, the chase is dropped beyond the 30-tile natural leash, the creature walks home, resumes its patrol, clears the persisted target and keeps its hit points, as `BaseMonster.loseAggroAndReturnHome` does). Not proved: that this defect is the only cause of the exact rendered observation (no rendered re-run was made), the 60-tile retaliation leash through the world step (unit-tested only), and Skeletons behind a portal (a player who leaves the region is outside the creature's region by design).
Server validation, `OnlineCombatValidationTest` (PostgreSQL, real session, melee and spell paths; every refusal leaves the character and the creature lives unchanged): a safe-haven tile (terrain code 6, `SanctuaryCombatRules`) protects both sides: melee from a sanctuary and at a creature standing in one is refused `out_of_range_or_blocked`, a Fire Dart cast from or at a sanctuary is refused alike with no mana spent and no exhaustion started, and the same fight outside succeeds and costs one mana; authored stationary (peaceful) actors refuse melee and spells with `protected_target` and are never provoked; a player with an active `stun` is refused melee (`cooldown`) and spells (`exhausted`) and may act once it ends. Single-player has no source that stuns the player (`Player.stunFor` has no caller; monsters carry no stun blow) and no rule that damage interrupts a cast; its interruption is that casting reveals a hidden caster and resets the auto-attack target (`Player.disturb`), which the server mirrors (a successful cast reveals) or leaves to the client. Server-side movement gating by stun is not modeled because nothing creates the state.
Ticked: the monsters acquire/pursue/reset bullet and the server-validation bullet. Still open in M4: the skills/spells/charges/HUD bullet (no buff or cooldown display, multi-charge items refused, tile-aimed area spells and area heals landed in #298 and are not re-verified here, no stat allocation online) and the level progression/stat allocation bullet, plus the Done evidence (a native scenario for every declared role).

Done evidence: a native combat scenario for each declared starter role, including two players
contending for a target, leaving reach/sight, death/recovery and reconnect with costs/cooldowns intact.

## M5 — Preserve progress and resist cheating

Required behavior:

- [x] Finish bounded connection reuse and shutdown ownership. A normal stop succeeds only after
  accepted work is drained and workers have terminated. A forced/incomplete flush is a visible
  failure; it must not be reported as a safe save.
  Ticked by T4C-0350 (2026-10-30, `origin/main` 3e147d8b plus the T4C-0350 branch, protocol 8/schema 13, local PG16 WSL, native adapters). Code and tests already in
  place from T4C-0196/0197: one owned pool of at most 8 connections with a 1 s acquire bound, real-PostgreSQL tests for backend reuse, state reset between borrowers,
  bounded saturation, replacement of a terminated backend and closing the physical sessions with the pool; a 12 s accepted-write drain plus a separate termination wait
  in the persistence workers; and tests for a blocked write that ignores interruption (the stop waits for the worker and fails with "flush incomplete") and for two
  concurrent closers. T4C-0350 added the missing server-level and live halves. (1) `PreviewServerTest.shutdownWhileTheDatabaseIsUnreachableFailsVisiblyAndNeverReleasesTheLease`:
  with every connection failing, `close()` throws, the writer lease is not released and a second close reports the same failure. (2) The live drill
  `run-native-fault-drill.ps1 -Mode ShutdownOutage` crashed PostgreSQL (immediate mode) under twenty playing adapters and requested the private operator stop 1.5 s later while
  the sessions were still bound: the server answered HTTP 200 ("stopping"), then 30 s later **exited with code 1** with `Server shutdown failed; closing owned database pool
  after incomplete teardown. Recover from committed state.`, `Persistence flush incomplete; recover from last committed state after lease expiry` and 26 persistence-failure
  lines on stderr, no success claim, no replay; after PostgreSQL's crash recovery the committed record verified PASS, every unreleased lease expired on its own clock (all 20
  at 0 live by +36 s), a new server answered healthy 1.6 s after launch and all twenty adapters were back at +60.7 s, and that server's final private stop exited 0. (3) Every normal stop in this
  pass (three in the restore check, one after the 30-minute load record, one after the tamper battery, one after the rotation probe, one at the end of the outage drill) returned
  HTTP 200 and exited 0, with empty stderr wherever it was captured, and the 30-minute run kept the pool within its limits (never above 8, 157,169 operations, mean pool wait 0.014 ms, 0 acquire timeouts). Limits: local PG16, native
  adapters, no rendered client; the exit code, not the HTTP 200, is the success signal (documented in [DATABASE_RUNTIME.md](DATABASE_RUNTIME.md)).
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
  T4C-0342 later covered the pickup claim and the corpse recovery (18 hard-killed rounds, each applied exactly once after the retry), and T4C-0350 reran that drill
  as step one of the restore check with the same result (see [tools/NATIVE_RESTORE_CHECK.md](tools/NATIVE_RESTORE_CHECK.md)); a quest turn-in, a death and a transfer provably in flight
  are still not exercised, so the bullet stays open.
- [x] Account-key rotation/foreign ownership, stale/reordered/duplicate commands, speed/stat/item
  edits, oversized requests and forged targets/rewards are rejected without corrupting valid play.
  Ticked by T4C-0350 (2026-10-30, `origin/main` 3e147d8b plus the T4C-0350 metrics change, protocol 8/schema 13, local PG16 WSL, native adapters):
  on the current build the 322-case tamper battery passed 322 of 322 (foreign account-key/character joins, rosters and admin routes;
  stale revision and epoch on every mutating route; replay, reorder, duplicate and sequence-jump input; speed and collision; stat, gold,
  flag and item edit fields; oversized, chunked and lying bodies; forged targets, pickups, recoveries and item ids) with the victim playing
  throughout and its record unchanged, and `NativeKeyRotationProbe` passed again (old key 401/409, old writer fenced without a post-rotation
  commit, new key rejoined the same character, observer unaffected). What remains open beyond this bullet's wording, and is listed in the
  notes below: combat-time forgery during live fights, enabled-portal transfers under attack, rendered clients and remote/TLS abuse.
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
  forgery, enabled-portal transfers, rendered clients and remote/TLS abuse remain open; they are outside this bullet's wording, which T4C-0350 ticked (see its note above).
- [x] Restore a backup into a separate database and finish a native gameplay/reconnect check there;
  matching table hashes alone do not satisfy restored gameplay acceptance.
  Ticked by T4C-0350 (2026-10-30, `origin/main` 3e147d8b plus the T4C-0350 branch, protocol 8/schema 13, local PG16 WSL, native adapters, not rendered):
  `run-native-restore-check.ps1` populated a disposable database by real native play (18 hard-killed loot-claim and corpse-recovery rounds, then
  twenty characters with opening letters and quest flags played by a 120 s soak whose server was hard-killed so the dump held 20 live copied leases:
  38 characters, 78 reward receipts, 77 combat events, 9 loot claims, 10 corpses), ran `verify_backup.py` into a separate `t4c_restore_*` database
  (all 12 tables identical), found the restore identical to the source on a 24-measure gameplay summary, then started a server on the restore only:
  the same twenty adapters (original account keys) played 90 s, the server was stopped gracefully (exit 0), restarted on the same restore, the adapters
  reconnected and played 45 s more (0 failures), second graceful stop exit 0. The restore was then judged against everything the source sampler had
  seen committed (no lowered revision, no lost receipt, claim, corpse or drop, one opening letter per character, no shared item id) and had
  committed new play on top (139 receipts against 78, 160 combat events against 77). Details and limits (no rendered client, no portal travel or
  loot claim performed on the restore, local only): [tools/NATIVE_RESTORE_CHECK.md](tools/NATIVE_RESTORE_CHECK.md). Earlier evidence: T4C-0213 verified
  a live schema-10 exported-snapshot restore across all eleven online tables, including
  PostgreSQL-reparsed equivalent CHECK definitions; both new loot tables were empty. Two
  actual native network adapters subsequently rejoined the isolated restore and read matching
  committed progress, balances and positions after real lease expiry, then left normally.
  That was a network read check without gameplay on the restore; T4C-0350 above supplies the gameplay and reconnect part.
  A hand-driven rendered playthrough on a restore remains the stronger, optional check (PARITY.md keeps its own line).
- [ ] Run a mixed session with two real rendered clients on the integrated candidate, with zero
  unexplained disconnects, crashes, invariant violations, duplicate value or lost acknowledged
  progress. Record its duration; there is no fixed minimum. Ending a session alone does not pass
  this gate: the required scenarios and evidence must be complete and satisfy the criteria.
  Include combat, loot, quests, travel and intentional reconnects rather than idle connections.
  T4C-0318 (2026-10-29, local, `d4eff350` plus this branch, development build not the package) ran two real rendered windows for
  39.4 minutes with the topic window and quest acceptance, equipment seen by the other window, a walk to the Skeleton ground, portal
  trips, 19.0 minutes of timed fight, idle and loot segments with three claim races, and (a later, separate run) a death with a corpse
  recovery: 0 disconnects, 0 client `ERROR`, `WARN` or exceptions, 0 server stderr lines and 0 "persistence failed" lines, clean
  graceful stop, heap in a 2.4 to 3.9 GB sawtooth without drift. The gate stays **unticked**: the candidate package was not
  used, no intentional reconnect or restart was part of the session, the quest kills and turn-in were not done by both windows, the
  death and recovery ran in a separate shorter session after a driver fix, the recovery bypassed the screen, and two
  Claude-lane refusals (busy equipment change, busy attack) were found and fixed in it (see CLIENT_UNIFICATION_DESIGN.md 3.17).
- [x] Record actual tick duration/debt, CPU/memory, bandwidth, database/command latency, occupancy
  and every error/recovery. Investigate stalls and growing queues/resources; do not count a failed
  run as passed merely because it ended.
  Ticked by T4C-0350 (2026-10-30, `origin/main` 3e147d8b plus the T4C-0350 branch, protocol 8/schema 13, local PG16 WSL, native adapters, development build): the
  server's operator metrics now also report game-socket bytes and pooled database operation time, pool wait and timeouts, and one 30-minute
  twenty-adapter run (eight paced portal walkers, default reconnect churn) recorded every listed measure in one place: 61,154 ticks, 0 failed, 0 over
  budget, 34.000 ticks/s, mean callback 1.258 ms, max 22.57 ms, max start lateness 21.32 ms, 0 deadline debt and 0 resets; CPU 16.8% of one core;
  heap max 607.6 MB and Windows working set flat at 850 to 863 MB after warm-up (threads and handles flat); game-socket traffic 175 KiB/s per adapter
  out and 25.3 KiB/s total in (application bytes, no TCP/IP headers); 157,169 database operations, mean 0.941 ms, max 148 ms, pool wait mean 0.014 ms,
  0 acquire timeouts, 0 rollbacks, 0 deadlocks; command latency 880 of 988 results within 500 ms and none above 1 s; occupancy 51.7% at twenty and 97.8% at
  nineteen or more (the reconnect churn's ceiling); 248 region transfers; 0 adapter failures, 0 health failures, empty server stderr, graceful stop exit 0;
  the 272 reconnects were the scheduled churn. Full table and definitions: [LOAD_METRICS.md](LOAD_METRICS.md). Together with the two-hour run below
  (flat CPU and heap, 625 transfers) this covers duration and the metric set; it is not the integrated release candidate, not rendered, not remote and not TLS.
  Earlier history: T4C-0198 recorded a two-hour native-adapter baseline,
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
  application body bytes (about 2.4 MB/s) but not socket bandwidth or database operation latency at the time,
  which T4C-0350 now supplies; see [tools/NATIVE_ADAPTER_SOAK.md](tools/NATIVE_ADAPTER_SOAK.md).
  T4C-0221 repaired only Windows report replacement
  under a focused file-lock fixture; the earlier 66-second schema-11 smoke remains failed and
  no new sustained result is claimed. T4C-0227 adds bounded private operator
  counters for application body bytes and handler lifetime. The native soak
  consumer's finite 1 MiB cap covers the measured dense fixed-label schema
  while retaining its two-second whole-response deadline (at that point socket
  bandwidth and database latency were still unmeasured; T4C-0350 added them). A sustained run on the
  integrated release candidate remains outstanding.

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

**Closed by T4C-0345 (2026-10-30, same-machine evidence only; details in [CLIENT_DISTRIBUTION.md](CLIENT_DISTRIBUTION.md) and [SERVER_DISTRIBUTION.md](SERVER_DISTRIBUTION.md)):**

- [x] Java requirement documented and enforced by the launchers: every launcher (client, `check-server`, `start-server`, `provision-online`, `verify-package`)
  now prints one plain message and exits 1 when `java` is missing, older than 21, not 64-bit, or not a Java at all. Proved by `test-package-java-check.ps1`
  (fake `java.cmd` for Java 8, 17, 32-bit 21, junk, 21 x64 and 26-ea, plus a stock PATH with no Java, through the real `.bat` wrappers of a built client and
  server package). No JRE is bundled; the jlink alternative is documented as an option only, with the measured size (about 49 MB, not built into any package).
- [x] Execution policy: every operator script of the server package and the client's verify script now has a `.bat` wrapper that starts it with
  `-ExecutionPolicy Bypass` for that one process (proved with the shell's default policy forced to Restricted: the bare `.ps1` is refused, the `.bat` runs).
  A provisioning wrapper (`provision-online`) was added so the packaged JAR needs no hand-typed `java -jar` line.
- [x] Per-launch residue: the pinned GLFW library now extracts to one checksum-named folder that every launch reuses, instead of a new `TEMP` folder per launch.
  Proved with five packaged launches (three package builds, lobby and in-game) from a scrubbed environment and a fresh `TEMP`: exactly one
  `t4c-online-glfw-*` folder after the first launch and after every later one. `OnlineNativeLibraryTest` covers reuse and replacement of a damaged file.
- [x] Manifest verification after transfer: each package has `verify-package.ps1`/`.bat` that re-hashes every file against its own `SHA256SUMS.tsv`, rejects
  unlisted/changed/missing files and prints the manifest's own SHA-256; passing `-ManifestSha256 <value from the publisher>` also checks the manifest itself.
  The builders print that value. Proved on a copied package (match, mismatch, missing Java). A download/update channel is still an owner choice (below).

**Still open (unchecked) after T4C-0345:**

- [ ] Java on a machine that really has none: the message is proved only with a fake `java.cmd` and a PATH without Java on this machine. Missing evidence: the
  same launchers on a second machine or clean VM with no JDK, plus the owner's choice between "document Java 21" and bundling a runtime (jlink or vendor).
- [ ] Code signing and Mark of the Web: the `.bat` wrappers avoid the policy prompt, but unsigned scripts/JARs are unchanged. Missing evidence: the owner's
  signing decision and a browser-downloaded zip on a second machine (Mark of the Web and `Unblock-File` not exercised).
- [ ] SmartScreen and antivirus: not exercised (this machine's own Defender/SmartScreen state was not varied). Missing evidence: a second machine with
  Defender real-time protection and SmartScreen enabled. Fact for the decision: `assets/origin/` is 287 MB of the 731 MB asset tree, 82 files including four
  `.exe` and twelve `.dll` files, and a package built without it (`-ExcludeOrigin`) connected and rendered both the lobby and a game. The default builder still ships it.
- [ ] Size and transfer channel: measured about 759 MB extracted client by default (472 MB with `-ExcludeOrigin`), 4.3 MB server. Missing: the owner's
  download/update channel and whether to drop `assets/origin/` by default (the verify script covers integrity after transfer, not distribution).
- [ ] Firewall and loopback: only the local-loopback launcher is rehearsed. A second machine needs the server bound to a reachable address,
  HTTP port plus the game socket (HTTP port + 1) allowed, and no Windows Firewall prompt left unanswered.
- [ ] Remote HTTPS trust: a publicly trusted certificate and matching host name per [TLS.md](TLS.md), the client reaching it from another network,
  and a clear client error for an untrusted or mismatched certificate.
- [ ] GPU and drivers: the client needs OpenGL 3.x; behavior on integrated/older GPUs and remote-desktop sessions is unknown.
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
