# Design Guidelines

Balance and design rules for T4C Reborn, collected from the project owner's instructions. This
file is written **for Claude (and any other agent) working on this repo**: read it before
adding or changing content that touches progression, spells, items, monsters or the reference
website. When the owner states a new rule or changes an old one, update this file in the same
pass, so the rules live here instead of only in a chat log.

Where a rule is enforced in code, the enforcing class and test are named next to it. Change the
code and this file together.

## 1. Character progression

| Rule | Value | Where |
|---|---|---|
| Level cap | **400**. XP stops counting at 400; saves above it are clamped to 400 when loaded. | `GameConstants.MAX_PLAYER_LEVEL`, `XpCurve`, `PlayerProgression`, `PlayerStateMapper` |
| Points per level | 5 stat points, 15 skill points | `PlayerProgression` |
| Max rebirths | **50**. The Oracle refuses after that; saves above 50 count as 50 for the Seraph aura. | `GameConstants.REBIRTH_MAX_REMORTS`, `RebirthBehavior.canRebirth` |
| Starting attributes after rebirth *n* | 20 + 5n in all five attributes (270 at 50) | `RebirthBehavior.startingAttributeFor` |
| Level needed for rebirth *n* | 75 + 5(n − 1) (320 for the 50th) | `RebirthBehavior.requiredLevelFor` |
| Energy points from rebirth *n* | 10 + 5(n − 1) (255 at 50) | `RebirthBehavior.energyPointsFor` |

**Stat budget.** Size every requirement against what a character can actually have.
- A never-reborn character at level L has 100 base attribute points (20 in each of the five)
  plus 5(L − 1) more.
- A 50-rebirth character has 1350 (270 in each) plus 5(L − 1).
- At the level-400 cap, a well-built caster has about **1000** in their main casting stat.
- Nothing may ask for more than a character at that level can reach. Tests enforce this for
  spells and items.

The XP curve definitions still run to level 1000, so the cap can be raised later by changing one
constant. Monsters above level 400 (up to Arch Drake at 1000) currently remain as above-cap
challenge content. Rescaling them is an open follow-up, not a decision.

## 2. Spells

**Schools and their casting stat:**

| School | Stat |
|---|---|
| Fire, water, dark | Intelligence |
| Earth, light | Wisdom |
| Air | Intelligence and wisdom equally (the hybrid school) |

- **The ladder is even.** Every school has exactly one attack spell at each of levels
  **150, 200, 250, 300, 350 and 400**.
- At a given level, every school's spell has the same requirements, mana, price and damage.
  If you add a spell at a new level, add it for every school.
- Enforced by `spell/HighTierSpellCurve.java` (the single source for the numbers) and
  `HighTierSpellLadderTest`.
- **Requirements at level L:**
  - Main stat 2.5 × L; other casting stat 0.6 × L.
  - Air needs 1.55 × L in each of intelligence and wisdom.
  - So 375 at level 150, and 1000 at level 400.
- **Damage** is `(1d(L/5) + L/2 + stat/4) × power/resist`.
  - Single-target bolts multiply by 6; area spells by 5.
  - Air uses `(int + wis)/5` as its stat term.
- **Shapes per level:** 150 bolt, 200 area, 250 bolt, 300 bolt, 350 area, 400 area.
- **Support spells** above level 150 (wards, heals) take their stat gate from the same curve.
- **Hard limits:**
  - No player spell may require a level above the cap.
  - Never reuse a low-level spell's dice scale for a high-level spell.
- **Where it's taught:** Archmage Thalindra (Avalon). Naming a school opens that school's ladder;
  "train" opens the wards and heals.

## 3. Items

Enforced by `item/ItemBalance.java` and `ItemBalanceGuidelinesTest`, which checks every file in
`assets/items/`. `tools/ArmorSetGenerator` builds the Ancient Celestial and Empyrean armor sets
from the same rules; re-run it after changing them.

### Requirements
- **No item may require more than 600 endurance.**
- No single requirement may exceed 1000 (the level-cap main stat).
- Every item must have a class requirement (strength, agility, intelligence or wisdom).
  Its class comes from its requirements:
  - **Warrior:** strength is the main requirement.
  - **Archer:** agility is the main requirement, or the item is a bow.
  - **Intelligence mage:** intelligence is higher than wisdom.
  - **Wisdom mage:** wisdom is higher than intelligence.
  - **Hybrid (air) mage:** intelligence and wisdom are within 20% of each other.

### What each class gives
- **Warrior:** AC, resistance to all six elements, **strength**, **attack**.
- **Archer:** AC, resistance to all six elements, **agility**, **archery**.
- **Intelligence mage:** fire/water/dark power, resistance in that school, **extra
  intelligence**, and **less AC than a wisdom item**.
- **Wisdom mage:** earth/light power, resistance in that school, **wisdom**, and **more AC**.
- **Hybrid mage:** air power and resistance, **intelligence and wisdom** equally.
- Mage gear never gives strength, agility, attack or archery. Warrior and archer gear never gives
  elemental power, intelligence or wisdom.
- A theme may add a flavor extra: weapon or ring damage, shield parry, dodge, a doubled
  resistance in the theme element, or a small negative resistance as a drawback.

### Armor Class follows the endurance requirement
`AC = slot AC × endurance requirement / 100 × class multiplier`.

AC per 100 endurance requirement, by slot:

| Slot | AC | Slot | AC |
|---|---|---|---|
| Body | 31 | Legs | 10.32 |
| Feet | 9.28 | Hands (gauntlets count once) | 9.28 |
| Head | 8.94 | Belt | 6.88 |
| Cape (BACK) | 9 | Shield | 16 |
| Neck, ring, bracer | 5 | Weapon | 0 |

Class multipliers:

| Class | Multiplier |
|---|---|
| Warrior | 1.10 |
| Wisdom mage | 1.00 |
| Hybrid mage | 0.90 |
| Archer | 0.85 |
| Intelligence mage | 0.80 |

The `armorClass` field counts from every slot, jewelry included. A high-requirement ring should
carry real AC. For example, Aerie's Drakeheart Signet (600 strength, 600 endurance) gives 33 AC,
+50 strength and +120 attack.

### Bonus budget
The budget for a single, hand-made item scales with P, its main requirement. For a hybrid,
P = 0.8 × (intelligence + wisdom), so 375/375 counts as 600.

| Bonus | Budget |
|---|---|
| Strength / agility / wisdom | P / 12 |
| Intelligence | P / 10 (the "extra intelligence") |
| Hybrid intelligence and wisdom | P / 24 each |
| Attack / archery | P / 5 |
| Mage power (own school) | P / 10 |
| Mage resistance (own school) | P / 15 |
| Warrior/archer resistance (each of six) | P / 40 (a theme element may double it) |

- A full 6-piece armor set carries three single items' worth, split across pieces by AC share.
- Weapons carry their class stat (P / 12) and their skill (attack or archery). The damage
  formula and any legacy enchant-line percentage boost are kept.

### Slots, colors and sprites
- Capes and mantles use the **BACK** slot. The `CAPE` enum value has no inventory slot, so an
  item there can't be seen or taken off.
- Recolored cape art: `NMS_NewCape01` (red) and `__pal2` blue, `__pal3` purple, `__pal4`
  pink, `__pal5` orange, `__pal6` gold, `__pal7` green, `__pal8` black, `__pal9` white. The
  inventory icon has the same name prefixed with `Inv_`.
- **Element colors:** fire red, water blue, earth green, air gold, dark black, light white.
  The six archmage mantles (sold by Archmage Thalindra with "mantle") use them. High-level
  mage gear should require about **600** in its main casting stat.

## 4. Reference website (compendium)
- Generated from the live game data by `tools/CompendiumExporter`. CI regenerates it on every
  push to `main`, and Vercel deploys `main`.
- When adding a genuinely new spell, item, NPC, monster or quest class, add it to the
  exporter's "new content" lists.
- Show numbers the way the game really uses them. Publish computed values from the game's own
  helpers, not hand-typed copies. For example, the Seraph aura rolls 0–100 inclusive, so the
  Rebirths page shows (c + 1)/101, not c%.
- Pages that exist for rules: Systems (XP curve), Rebirths (per-rebirth requirements and
  rewards), Spells, Items.

## 5. Process
- Every player-visible change gets a `T4C-XXXX` ID in `TASKS.md` and a player-facing
  `CHANGELOG.md` entry (see `CLAUDE.md`).
- Open a PR, then wait for **Build and test** to go green and the **Codex** review to finish.
  Fix real findings, reply on each thread, and resolve it. Merge only after all of that.
  Never merge while Codex is still running.
- Once a PR is merged, start follow-up work on the same branch name, freshly from `main`.
