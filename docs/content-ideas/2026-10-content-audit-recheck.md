# Original-game content audit: recheck against current code (T4C-0407)

Scope: single-player content only (monsters, NPC scripts, items, quest flags, spawns). The earlier notes
([quest audit](2026-09-original-quest-audit.md), [Bible audit progress](2026-10-original-bible-audit-progress.md)) were treated
as leads. Every lead below was re-derived from the code at `origin/main` 43501f16, then compared with the original-game
reference (t4cbible.com Add-on page) where a repair was made. Multiplayer, shared engine classes, animation, SpriteLoader,
BaseNPC, ScriptedNpc and build files were not touched (another agent owns them; see `multiplayer/PEER_WORK.md`).

## Method (so the next pass can repeat it)

All four checks are text/registry scans over `src/main/java/com/perso/T4C` plus the live registries. None of the scan code is
committed; the rules are short enough to restate.

1. **NPCs spawned at (0,0,0).** Every `@Spawn(... x = 0, y = 0, z = 0)` NPC was matched, case-insensitively, against any other
   `@Spawn` of the same type with real coordinates and against references from other classes.
2. **Required items with no source.** Every literal key passed to `hasItem` / `takeItem` / `itemCount` was compared with every
   possible supplier: monster loot (display names normalised to keys), `ShopCatalog`, literal `giveItem`, `InventoryService.add`.
3. **Quest flags read but never written.** Every `flag(...)` / `globalFlag(...)` read was compared with every write, after
   stripping the `__`, `GLOBAL_` and `FLAG_` prefixes (the same flag appears under several spellings).
4. **Monster definitions no spawn can place.** Each `MonsterDef` name was compared with every `@Spawn` type and every spawn-group
   entry, then with references from other classes (summons).

## What the recheck found

### Confirmed broken and repaired in this pass

| Path | Evidence | Repair |
| --- | --- | --- |
| Raven's Dust demon crypt: the three Chaos **centre portals** (`ChaosWest/East/SouthToCenterPortal`) open only when `ADDON_TERROR_DEMON_KILLED`, `ADDON_CHAOS_DEMON_KILLED` and `ADDON_DARKNESS_DEMON_KILLED` are all 1. Nothing set them. Lothar and the Caledbolg stand in the centre room behind those portals. | Flags read in three portals, written nowhere; the three demons are spawned (`Terror Demon` 1477,1793; `Chaos Demon` 1755,1794; `Darkness Demon` 1475,1514, all z=1) but had no death handling. The Bible says to kill all three, then use any of the three portals to reach Lothar. | `r241TerrorDemon`, `ChaosDemon` and `DarknessDemon` now set their own per-player flag when the player kills them. |
| Olin Haad (Arakas): his greeting changes once the player has cleared "about 20" of his guards; `OlinHaad1` reads `ADDON_NUMBER_OF_OLIN_HAAD_GUARDS_KILLED > 19`. Nothing counted. | Flag read in one NPC, written nowhere; guards 3, 4 and 5 are spawned (spawn groups 230/231 and the class spawns). The Bible says to kill about 20 unnamed guards. | `r163OlinHaadGuard3`, `r164OlinHaadGuard4` and `r165OlinHaadGuard5` deaths add to the counter (capped at 99). Guards 3, 4 and 5 all count: the Bible names no type. The conversation never hard-blocked (a player at progress 2 is advanced to 3 regardless), so this only fixes which line Olin Haad speaks. |

End-to-end proof: `AddonDemonAndGuardCountersTest` (5 tests) kills each demon with the real monster classes, talks to the real
portal NPC and checks the player lands in the centre room; the portals stay shut with two of three kills, one player's kills do
not open the portal for another, and 21 guard kills across the three guard types satisfy Olin Haad's gate while the counter
stays capped. The npc, monster, quest and spawn test packages pass.

### Lead disproved (do not re-investigate)

- **"55 dialogue NPCs sit at (0,0,0)."** Of the 53 origin spawns now in code, 25 NPCs already have a second spawn with real
  coordinates (the dual NPC/MONSTER spawn pattern: Bane Blackblood, Jarko, Haden, Edgar, the Oracle bosses and doors, the Kahp
  Leth and Moon Tug guards, Mhorgwloth, Greyleaf and others), 11 are summoned or referenced by another class (the six spell
  portals, Carman, ExitGate, the Daran trainers via `TrainingCatalog`, Dorken Rotsmell), and 17 are never placed (below).
- **Required-item gaps.** After normalising keys, 151 required item keys have only nine apparent gaps and all nine have a source:
  `apple`, `mandrake`, `necromantic_scroll` and `fake_blade_of_ruin` are sold by shops; `assassin_blade` and `robe_of_hell` come
  from Malachai Fatebringer's fate roll (a ternary the scan could not see); the Fenrir and Jormungand soulstones are given by
  the god's death handler through `InventoryService.add`. The remaining `finely_cut_`, `rough_` and `royal_key_` hits are keys
  built by concatenation. No quest item was found that nothing can supply, except as noted under "Still open".

### Still open (confirmed by the scan, not repaired here)

- **Never-placed NPC classes**: Aydan Goldstar, High Priest Gunthar 2, Olin Haad Guard 3/4 (NPC kind), PM Theisser, Sweeper,
  World Wild Horse, Portal Cave Destin, Portal Forteresse Noire, Portal Spell Wizard Vale, Blight Portal and the Nomad furniture
  (bed, desks, bookshelves, painting, table). Each has no second spawn and no reference. Sweeper gives the Arcane Scroll on death and
  is only ever read by two flag macros, so its spawn is the likely missing piece. Placement needs canonical coordinates that the
  Bible pages do not give; nothing was invented.
- **Quest flags that nothing sets** (the branch they gate is unreachable): `QUEST_WAX_APPLE` (Samuel's Wax Apple trade is locked
  forever; the code's Elysana uses an `apple` plus herb pouches instead of the Bible's wax-apple route, so this is a design
  divergence rather than a missing line), `QUEST_KALASTOR_MISSION` (Liurn Clar, Renegade Orc Leader), `QUEST_KRAANIAN_EYES` (Bane
  Blackblood), `QUEST_URANOS` (Marsac Cred), `QUEST_BRIGAND_GOT_THE_REPORT` (Lord Sunrock), `QUEST_USER_IS_A_TRAITOR`,
  `RATS_KILLED` (Sabrina's counter), `GUARDIANS_KILLED` (Celestina), `WHO_HAS_THE_STONE_FLAG` (Mhorgwloth), `TITLE_MORDRICK`,
  `LIGHTBRINGER_OF_ARTHERK`, `FOLLOWER_OF_OGRIMAR`, `FACTION_WITH_THIEVES`, `EVIL_QUEST_COMPLETED_ON_RD`, `ADDON_GLURIURL_PRESENT`,
  `ADDON_NEXUS_STONEHEIM_ACTIVATED`, `USER_HAS_ARCANE_SPELLBOOK`, `USER_HAS_CROWN_OF_CORRUPTION`, `USER_HAS_ROBBED_THE_SAFE_BEFORE`,
  and the Colosseum flags. The Bible pages fetched this pass (Add-on, Raven's Dust) do not mention Uranos, Sabrina, Sunrock's
  report or the level-25 mission, so there was no reference to repair against. Several look like flavour reads rather than gates;
  each needs its own reading of the original script before changing it.
- **Monster definitions with no placement** (120 by the crude name match; most are aliases of placed monsters). Real candidates to
  check against the Bible's monster chart: the seven Psykowasp monsters, Centaur Archer, Centaur Shaman, Cobra, Dungeon Bat,
  Large Rat, Hoofcrusher, Psi Monk, Puppet Master, Robin Hood, Acid Slime, Battle Bard, Battle Priest, Faith Warrior and Heretic
  Warrior. Their drop-chart entries exist (earlier passes added loot), so only world placement is missing.
- **Lead "Letter from Damien to Xanth"**: Monsignor Damien's side plot checks for this item but nothing gives it; the main Shadeen
  quest advances the same flag at its own step, so nothing is blocked.

## Decisions recorded

- The Olin Haad counter counts guards 3, 4 and 5 because the Bible names no guard type; if a later source ties it to the
  "Flagged" spawn group only, restrict it to guard 5.
- No coordinates were invented for the never-placed NPCs and monsters; they stay open until a canonical source is found.
