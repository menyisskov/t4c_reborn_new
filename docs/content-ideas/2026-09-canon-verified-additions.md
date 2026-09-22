# Content pass: canon-verified additions (Goblin Slayer, Bow of Centaur Slaying, Drake's Lair, Deep Ones Cave loot/quest)

## Update: the "Ancient tier" spells were renamed (T4C-0018)

This doc's "Ancient tier" spells (Sentinel, Divine Veil, Clemancy, Undead Annihilation, Omega
Planetoids) were originally named after the real t4cfantasy.com/Addon spell list documented
below — on later reflection, a fork adding *new* content shouldn't reuse the *real* game's own
spell names, since that collides with the genuine article rather than inventing alongside it.
T4C-0018 renamed all five to invented names with identical mechanics/`spellId`s: Sentinel →
Leyward Bastion, Divine Veil → Veilstone Aegis, Clemancy → Wellspring Mercy, Undead Annihilation
→ Sunscour, Omega Planetoids → Gravebreaker. The research below (what the real Addon tier
actually contains) is left as-is as a historical record — just don't take the old spell names as
what's currently shipped.

The two previous content-idea passes (`2026-09-sunken-chancel-and-cinderreach-hills.md`,
`2026-09-leveling-overhaul-and-endgame-zones.md`) were both written with `t4cfantasy.com` and
`t4cbible.com` blocked by this environment's network egress proxy. This pass had real access to
both sites. It cross-checks the two earlier docs' claims (see the "Update" notes now at the top
of each) and builds new content directly grounded in what the sites actually contain, rather than
web-search fragments.

## Process note: this codebase has more live legacy content than it looks like

Before trusting anything below, read this — it changed the shape of the whole pass. The first
draft of this pass wrote fresh `DeepOne.java`, `DeepOneBoss.java`, `ArchDrake.java`, and
`RhodarHeatforge.java` from scratch, on the assumption that grepping for the *string* "Deep One"
/ "Arch Drake" / "Rhodar Heatforge" elsewhere in the source (which came back clean) meant nothing
used those names yet. That assumption was wrong, and the `Write` tool's own "file ... has been
**updated**" responses (rather than "created") were the tell that got missed the first time:
**all four files already existed**, as real, already-wired, already-spawned content imported from
the original game:

- `DeepOne.java` already had **41 real `@Spawn` points** (24 on `WORLDMAP`, clustered around
  (330, 2246) in Raven's Dust; 17 more across two `CAVERN` sub-areas) and real loot (`Light
  healing potion`, `Potion of fury`).
- `DeepOneBoss.java` already had a real `@Spawn` at (932, 424) on the same `CAVERN` map, with
  real stats (health 1,812 — an exact match for what this pass's own site research derived
  independently) and `xpOnDeath` already set (23,307), just no loot.
- `ArchDrake.java` already existed **fully stat-authored** (health 206,623, a real dodge value,
  and an attack list that already casts exactly one spell — already matching the "Fireball only,
  unlike its lesser kin" distinction the site confirmed) but had **zero `@Spawn` points** — the
  exact same "written but never placed" state `LesserDrake`/`GreaterDrake` were in before the
  previous pass activated them, just not previously noticed.
- `RhodarHeatforge.java` already existed as a **fully-wired shopkeeper NPC** — a real weapon shop
  (18 items), day/night "closed" hours, and a working "Rhodar's Hammer" quest interaction
  (`hasItem`/`takeItem`/`giveGold`/`giveXp`) — using all 16 of his dialogue topics from
  `lang.json`, not the handful this pass first assumed were unused.

All four were restored to their real, original content and then given **minimal, additive**
patches instead — new loot-table entries, one new `@Spawn` (Arch Drake only), and one new
dialogue topic + keyword handler (Rhodar only) — the same discipline already used on
`CentaurKing`/`CentaurWarrior`/`SkywatchIlvara` elsewhere in this pass. The lesson, stated
plainly for whoever reads this next: **before writing any monster/NPC file by a name that seems
new, check whether the file already exists** (`ls src/main/java/com/perso/T4C/monster/<Name>.java`
or equivalent) — a clean grep for the *string* elsewhere is not the same check, and this
codebase's legacy import is bigger and more complete in places than it first appears.

## Research summary

Full crawl notes (16 pages, both domains) live in this session's working notes, not committed —
the load-bearing facts are captured here and in the corrections added to the two earlier docs.
Headlines:

- **t4cbible.com is an empty forum/CMS**, despite the name — it is not a game database. The real
  structured data (monster/item/spell/NPC/quest tables) all lives on **t4cfantasy.com**, split
  across `/Bible/*` (the "Bible" proper) and `/Addon` (which, despite its name, turns out to be a
  much bigger item/spell/boss stat database, not narrative expansion lore).
- The world is **Althea**, three base islands **Arakas / Raven's Dust / Stoneheim**, plus six
  later add-on islands (Avalon, Atlantis, Paradise, Egypt, Oblivion, Arena) — see the correction
  now in the Sunken Chancel doc for what was wrong in the original web-search-sourced claim.
- `/Bible/Monster`'s color-coded categories are AR/RD/SH/Seraph/Add-On/**GM**/Colosseum (not
  "Beta"), with Mini-Boss as a cross-cutting modifier, not its own category.
- `LesserDrake`/`GreaterDrake` in this codebase (health 15,385 @ level 250; health 54,943 @ level
  500) are **exact matches** for the real GM-category Drake ladder — confirming the previous
  pass's web-search-derived numbers were accurate. The real ladder has one more rung: **Arch
  Drake, level 1000, health 206,623** — already sitting in this codebase, unplaced (see the
  process note above) — a natural capstone since this repo's XP curve caps at level 1000 too
  (`XpCurveHardener.NEW_MAX_LEVEL`).
- Real, concrete hooks with genuine implementation gaps: a **Goblin Slayer** weapon (kill 500
  goblins → bought from **Rhodar Heatforge**, whose dialogue already described this but had no
  logic behind it), and a **Bow of Centaur Slaying** (+1 to +4, drops off Centaur/Skraug officer
  tiers) — neither existed as an item before this pass.
- A whole "Ancient tier" of level-200+ group-support spells confirmed on `/Addon` (Divine Veil,
  already in this codebase, sits alongside Army's Paeon, Mage's Ballad, Wanderer's Minuet,
  Clemancy, and **Sentinel** — the last of which this pass adds).

## What shipped in this pass

### Engine fix: JSON-authored weapons can now deal real, scaled damage

Both earlier passes flagged the same gap (`item-creator` skill, and the "Honest limitations"
section of the Sunken Chancel doc): `ItemJsonDef.toItemDefinition()` always passed `dmgFormula =
null` / `atkDelay = "0"`, so any JSON weapon fell back to a flat, stat-independent 1–4 damage
roll regardless of its stats. `ItemJsonDef` now exposes optional `dmgFormula`/`atkDelay` fields
(defaulting to the old `null`/`"0"` behavior when omitted, so all 111 pre-existing JSON items are
unaffected) that pass straight through to `ItemDefinition`, mirroring how legacy Java-authored
weapons already express it. `ignaroks_emberfang_claw.json` (from the first pass) picked up a real
`dmgFormula` as a drive-by fix now that the gap is closed.

### Bow of Centaur Slaying (new item)

Canon-named weapon (`bow_of_centaur_slaying`, isBow, real scaled bow damage via the fix above),
dropped by the **already-existing** `CentaurKing`/`CentaurWarrior` (Windhowl Marches, previous
endgame pass) — no new monster needed, just two new loot-table entries at rarity-appropriate
chances (0.015 boss-tier, 0.008 trash-tier).

### Goblin Slayer (new item) + Rhodar Heatforge (real pre-existing NPC, minimally extended)

`goblin_slayer.json` is a new item, granted by the real, pre-existing `RhodarHeatforge` NPC (see
the process note above — this class was restored to its original shop/hammer-quest content, not
rewritten) once a hero's global `__GOBLINS_KILLED_BY_HERO` count — the same counter Mirak Nira's
"100 goblins for Windhowl trust" quest already tracks — reaches 500, matching the real in-game
threshold his own (already-imported) dialogue at `npc.topic.rhodarheatforge.5` describes. Canon
sells the weapon for 25,000 gold; this recreation grants it directly instead (same `giveItem` +
flag pattern as Mirak Nira's `ring_of_trust`) because `NpcBehaviorContext` has no "take gold"
action to pair with `giveItem` — flagged rather than faked. The only new code in
`RhodarHeatforge.java` is one new dialogue topic and one new `onKeyword` branch for
GOBLIN/SLAYER/BOUNTY; his shop, hours, and Hammer-quest logic are untouched.

### Arch Drake (real pre-existing monster, activated) — completes the Drake ladder

`ArchDrake.java` already existed fully stat-authored (see process note) but unplaced. This pass
adds its `@Spawn` (WORLDMAP, (2200, 2500) — a genuinely empty pocket, zero existing `@Spawn`
points within 150 tiles, ~280 tiles past the previous pass's Greater Drake's Bastion at
(2000, 2300), continuing that pass's "the Drakes get more remote and dangerous" geography), a
real `archdrakes_molten_heart` loot entry (new item, BELT, unique, fire power/AC — calibrated
against the first pass's `heartfire_of_the_greater_drake` for scale), and an `xpOnDeath` (was
`0`, meaningless while unplaced — see the `int`-overflow limitation below for why the new value
is capped rather than curve-exact). Every other field — health, dodge, resists, the attack list
that already casts only Fireball — is the original authored data, untouched.

### Kraanian Dragonguard (new monster) — Arch Drake's guard, "Drake's Lair"

The one genuinely new monster in this pass: `Kraanian Dragonguard` (trash, level 750, new —
reuses `KraanianWyrmling`'s `KraanianFlying` sprite family, an elite evolution of the same stock)
spawns four points around Arch Drake's position, giving it a real approach to fight through
rather than a bare, undefended boss spawn. Drops `dragonguards_scale_bracer` (new item). A new
quest, `drakes_lair_vigil` (kill 15, offered by new NPC `Outrider Kaelis`), ties the two
together.

### Deep Ones Cave — real pre-existing monsters, new loot + new quest (no new zone)

`DeepOne`/`DeepOneBoss` already existed, fully spawned (see process note) — there was no zone to
build. This pass adds:

- **Loot**: `barnacled_gauntlets` on `DeepOne` (new item, alongside its existing `Light healing
  potion`/`Potion of fury` drops), `depths_wardens_talisman` on `DeepOneBoss` (new item, NECK,
  water power/resist — it had no loot before).
- **Quest**: `deep_ones_cave_purge` — kill 18 Deep Ones, offered by new NPC `Keeper Tamsin`.
  The quest's area (WORLDMAP, center (330, 2246), radius 90) is the real `DeepOne` `WORLDMAP`
  spawn cluster's own centroid, sized to cover every one of its 24 existing spawn points, not an
  invented location — a quest whose area doesn't overlap where the target actually spawns would
  silently never register a kill (see `quest-creator`'s `QuestService.recordKill` note). Keeper
  Tamsin is placed just outside that cluster. Flavor text says "this stretch of the Raven's Dust
  coast" rather than naming a specific neighboring settlement — the nearest confirmed neighbor is
  an RD trash monster (`Taunting Horror`), not a named town, so claiming proximity to Silversky
  (which sits ~1,180 tiles away) would have been invented geography.

### Spell: Sentinel (new)

A real, level-200 "Ancient tier" group-support spell (per `/Addon`'s spell tables, sitting
alongside Army's Paeon, Mage's Ballad, Wanderer's Minuet, Clemancy, and the already-shipped
Divine Veil). Modeled directly on Divine Veil's own "full elemental resist + AC" party-ward
shape, scaled to level 200's actual cost/level floor rather than reusing Divine Veil's lower
numbers outright. Taught by `SkywatchIlvara` (Lesser Drake's Aerie's existing trainer NPC, level
200–260 zone) via a new `OPEN_SPELL_LEARNING` dialogue topic.

## Honest limitations, flagged rather than papered over

- **`MonsterDef.xpOnDeath`/`QuestDef.rewardXp` are still `int`, and the hardened level-1000 curve
  can overflow them.** `XpCurveHardener`'s own formula wants a level-800 trash mob's "9 kills per
  level" `xpOnDeath` around 2.9 billion, and a level-1000 boss's "3–4 kills per level" figure
  around 280 billion — both blow past `int`'s ~2.1 billion ceiling. `Kraanian Dragonguard` was
  deliberately placed at level 750 instead of 800+ so its natural calibrated value (≈1.19B) fits
  safely; `Arch Drake`'s `xpOnDeath` and `drakes_lair_vigil`'s `rewardXp` are both capped just
  under `Integer.MAX_VALUE` rather than truncated or left to silently overflow — still the
  biggest single-kill/quest reward in the game, just not literally curve-accurate at the very
  top. This is defensible rather than urgent: level 1000 is the curve's own hard cap
  (`XpCurveHardener.NEW_MAX_LEVEL`), so a kill at that level is a capstone/prestige reward, not a
  leveling tool. The real fix — widening `MonsterDef.xpOnDeath`/`xpPerHit`, `QuestDef.rewardXp`/
  `rewardGold`, and `PlayerProgression.addXp`'s `int amount` parameter to `long` — is the same
  class of fix the leveling-overhaul pass already made for `Stats`/`XpCurve`/`PlayerStateDto`,
  and is flagged here as the natural follow-up rather than attempted unattended as a drive-by on
  top of an already-large pass.
- **Goblin Slayer is granted, not sold.** See the Rhodar Heatforge section above — canon sells it
  for 25,000 gold; this recreation has no NPC "take gold" action to pair with `giveItem`, so it's
  handed over free once the kill threshold is met, matching the `ring_of_trust` precedent
  already in the codebase (Mirak Nira's trust quest) rather than inventing a new mechanic.
- **No new sprite art**, same as both earlier passes — `Kraanian Dragonguard` reuses
  `KraanianWyrmling`'s `KraanianFlying` sprite family, and every item reuses an existing,
  already-referenced-elsewhere sprite name (verified by grepping `assets/items/*.json` and
  `tools/ArmorSetGenerator.java`, not guessed).
- **Spawn-point collision checking was grep-based, not visual**, same caveat as both earlier
  passes, for the same reason (no display in this environment) — someone with map-editor access
  should confirm the terrain at (2200, 2500) (Arch Drake / Kraanian Dragonguard) is sane before
  treating it as final. Deep Ones Cave and Rhodar Heatforge needed no new placement — see above.
- **Registry snapshot tests updated.** `SpellRegistryParityTest` (291→292, +Sentinel) and
  `LighthavenSamaritanTest` (468→470, +Keeper Tamsin +Outrider Kaelis — Rhodar Heatforge was
  already counted in the original 468) had hardcoded totals this pass changed; both were bumped
  to the new real counts, not loosened.

Full test suite (`mvn test`, 259 tests) and `mvn compile` both pass after this change.

## Backlog: further canon-grounded ideas not built this pass

Real hooks surfaced by the site crawl that a future pass could build on with real confidence,
roughly in order of how directly they extend what's already shipped:

- **Bane Blackblood's castle** (RD mini-boss, level 80, "Bane's Castle") already has a real
  quest chain in canon (Audience to Bane Blackblood → Cape of Fire Resistance) with a named
  lieutenant (Dwarthon Stoneface) and dungeon boss (Delwobble the Mad Summoner) — a strong
  candidate for the next full zone-plus-questline pass. **Check first whether any of these
  already exist as unplaced/unwired legacy classes**, per the process note above, before
  authoring anything new.
- **A matching "Centaur Slaying" armor/quiver set** alongside this pass's Bow of Centaur
  Slaying, following `tools/ArmorSetGenerator.java`'s proportional-split pattern — canon's own
  Bow already comes in +1 to +4 tiers, which this pass simplified to a single base version;
  enchant tiers are a natural follow-up once/if a "+N" mechanic exists for JSON items.
- **Deep Ones Cave's `CAVERN` spawns** (17 more `DeepOne` points, plus `DeepOneBoss` itself, all
  on `z=2` around (666–1083, 109–434)) have no quest pointed at them yet — this pass's
  `deep_ones_cave_purge` only covers the `WORLDMAP` cluster, since `QuestDef` supports one
  `targetWorldZ` per quest. A second, `CAVERN`-scoped quest (or a boss-kill objective once
  `QuestDef` supports something other than a plain kill-count) is a natural, low-risk follow-up.
- **Rhodar's Hammer** — his existing `HAMMER` keyword handler already checks for `rhodar_hammer`
  and pays out 3,500 gold + 8,000 XP, but nothing in this codebase drops `rhodar_hammer` yet.
  Canon's source is Eye-Patched Qardos (RD mini-boss, level 30) — another "does this already
  exist unplaced?" check before writing a new monster.
- **Silversky royal court** — King Theodore VIII, Princess Delilah, and Bishop Crowbanner Rikken
  are all real, named canon NPCs with only a couple of documented quests (Audience to the King,
  the Royal Keys chain through RD Crypt Level 3) — a good candidate for a proper multi-quest
  "political intrigue" pass, per `quest-creator`'s multi-`QuestDef`-chain guidance.
- **Crimsonscale vs. Dark Fang** — two named, opposed dragons already in canon (a "good Dragon"
  at the Mausoleum in RD, "the dragon of Lighthaven" in Arakas) with no plot connecting them yet.
- **A "cult of Makrsh" mini-raid** — Gluriurl and Harvester of Life (both level-150 Add-On
  mini-bosses) cast the exact same spell kit as the level-200 Seraph raid boss Makrsh P'Tangh,
  strongly implying they're meant to be his lieutenants/avatars.
- **Skeleton Cave** — confirmed to exist in Raven's Dust via quest text, but has no documented
  monster roster of its own (the "Undead Cave" with that theme is actually in Stoneheim) — a
  genuine gap, not a rename of anything already built.
