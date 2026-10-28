# Online parity with single-player

**Owner rule (2026-10-28):** the online game must look and play exactly like the single-player
game: layout, capability, characters, monsters, XP, inventory, everything. The only difference is
that there is now a client and a server. Single-player is the reference for every decision about
balance, rules and looks. This file is the acceptance checklist for that rule and supersedes the
narrower Beta scope in [SCOPE_PROPOSAL.md](SCOPE_PROPOSAL.md) and [MILESTONES.md](MILESTONES.md)
wherever they conflict. Source: read-only parity audit of origin/main 4bcb72b4 (2026-10-28);
re-measure before relying on a number.

## Where online stands (hosted versus total)

| Area | Single-player | Online today |
|---|---|---|
| Hosted map | all worlds | two crops (world 0 249x117, Lighthaven interior 80x60) |
| Monster spawns | 14,393 across worlds 0/1/2 | about 50 |
| Portals | 1,118 | 2 (16/17); all others refused |
| NPCs | about 2,574 placed spawns | 3 (Kilhiam, Warden Aelric, Sigfried) |
| Shops | 21 | 1 hosted (Sigfried: buy, sell, server-priced, one atomic transaction; plain text panel) |
| Quests | 35 | 1 (renewed_wards) |
| Player spells | 132 | Stage 1 and 2 (81 spells) plus Light Heal; starting spell kits are now offered to six online classes |
| Classes | 7 | 6 (Warrior, Paladin, Cleric, Healer, Mage, Battle Mage); Archer awaits online ranged combat |
| Combat | melee, bows, two weapons, stun blow, stealth, spells | melee and supported stage-1/2 spells; bows, dual wield, stun and stealth still unavailable |
| Interface | full GUI (inventory, spell book, minimap, macros, options, journal) | plain text panels |
| Audio and effects | on | audio off, no spell effects |

## Parity checklist (tick only with evidence; the evidence must be a test or a live run)

- [ ] **Client unification.** The online client runs the real single-player screens against a
  remote backend (intents out, server snapshots in) instead of growing a second GUI.
- [ ] **Characters:** all 7 classes, creation screen and preview identical to single-player,
  stat and skill allocation, training, rebirth.
- [ ] **Combat:** melee, archery, two weapons, stun blow, stealth, armor class and auras.
- [ ] **Spell engine:** all 132 player spells (damage, heal, buffs and debuffs with expiry,
  damage over time, area, summons, projectiles) cast on the server, buffs persisted.
- [ ] **Monsters:** all 484 definitions including spell attackers, scripted events, bosses,
  unique and one-of loot, durable clan combat.
- [ ] **NPCs and economy:** dialogue for every NPC, all 21 shops (buy and sell), storage and
  bank, trainers, companions.
- [ ] **Quests:** all 35 quests, journal and kill log.
- [ ] **World:** all worlds hosted, all portals and teleports, performance with about 14,000
  spawns.
- [ ] **Items:** player ground drop, multi-charge items, harvest and crafting, quantity prompts.
- [ ] **World systems and rules:** world events, trials, divine systems, karma, the full death
  penalty, return scrolls.
- [ ] **Audio and effects** from server events.
- [ ] **Chat** (the minimum multiplayer addition for a two-client game).

## Temporary divergences (debt, not design)

Each is a gap to close, never a decision to keep:

- Archer remains excluded from online character creation until authoritative ranged combat is implemented; the other six classes now have online starter kits and the server-supported spell set.
- Stage-1 and stage-2 spells (40 attack with splash, 4 drain, 4 heal; 25 timed buffs, the curse, invisibility and detection, 2 dispels, Renew Armor: 81 in all) are castable server-side (`spells-v2` stage gate, protocol 6); everything else stays refused. Casters cannot be created online yet, so only a character that already knows them can cast. Buffs are laid on the caster only; laying one on another player is resolved by the engine but not yet reachable (needs a two-character atomic commit).
- Melee only; bows, stun blow and companions refuse as unsupported combat.
- Combat fails closed while a world-event flag is active, or while a buff the server does not model (an older record without stored amounts, or an attribute outside `BuffAttributes.MODELLED`) is running. The 25 stage-2 buffs are modelled and fold into melee, retaliation, carry weight and spell numbers.
- Other quests' flags block combat (quest-flag allowlist).
- Goblins do not attack the Wild Horse (clan combat not simulated).
- Monsters with spell attacks, scripts or unique loot are excluded from combat.
- Multi-charge items are refused; no player ground drop.
- Hosted crops only; every portal except 16/17 refused; NPC-owned spawns (Sundial) filtered out.
- Ground-loot owner window (60 s) and expiry (15 min) have no single-player equivalent;
  they are a multiplayer rule, kept unless the owner removes them.
- Movement: the server uses a swept 0.2-tile substep rule at 34 Hz with no sliding, no speed buffs, no dynamic blockers and tile-centre positions; single-player reserves whole tiles, slides along walls and scales by speed buffs. The shared `MovementRules` holds both (`Mode`); the server does not use it yet. Difference table: `CLIENT_UNIFICATION_DESIGN.md` 3.10 (T4C-0269).
- Text HUD and panels; Space attacks the nearest target; audio disabled.
- Characters are operator-provisioned accounts; no import of desktop saves.

## Build order

1. Rescope documents and this checklist (done with this file).
2. Client unification design, then build. This is the largest and riskiest change
   (`MainGameScreen` is about 5,000 lines coupled to the local player), so it starts with a design
   written by the top-tier model and reviewed before code.
3. Generic spell engine in the shared rules module, exported content, server casting.
4. All classes plus archery, two weapons, stun blow and stealth; stat and skill allocation.
5. Monster parity (spell attacks, durable clan combat, unique and one-of loot, scripted bosses).
6. NPC engine (decide: extract dialogue to data or run the NPC classes on the server), then
   shops, storage, trainers, companions.
7. All quests generalised over the shared quest definitions, plus journal and kill log.
8. Whole-world hosting with every portal; load and performance check.
9. Remaining item and world systems.
10. Audio, effects, chat.

Steps 3 and 6 are server and shared-rules work and can run in parallel with step 2.

## Rules that follow from the owner rule

- When online and single-player disagree, single-player wins unless it is impossible with a
  server (then record the reason here).
- Visual work copies the single-player screens; do not invent a separate online look.
- Balance questions are answered by the existing single-player numbers, not by online-only
  changes. The starter Skeleton ground stays exactly as single-player has it.
