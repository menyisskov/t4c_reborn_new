# Design Guidelines

Balance and design rules for T4C Reborn, collected from the project owner's instructions. This
file is written **for Claude (and any other agent) working on this repo**: read it before
adding or changing content that touches progression, spells, items, monsters or the reference
website. When the owner states a new rule or changes an old one, update this file in the same
pass, so the rules live here instead of only in a chat log.

Where a rule is enforced in code, the enforcing class and test are named next to it. Change the
code and this file together.

## 0. Keeping this file current (standing instruction)

The owner does not need to ask for an entry here. **Keep this file current yourself:** after
every conversation or task, work out which decisions should outlive the chat and write them
down in the same pass (and the same PR) as the work. Add something when it is any of these:

- **A rule or number the owner states or approves.** Caps, formulas, ratios, thresholds,
  naming or color schemes, "X should always/never Y".
- **A correction.** When the owner says something "doesn't make sense" and explains what they
  expected, the expectation is the rule. Record the general rule, not just the one fixed item.
- **A design choice you had to make** to carry out an instruction, and would otherwise make
  differently next time: the formula you picked, a tie-breaker, what counts as "balanced". Say
  it was your call so the owner can overrule it.
- **A workflow preference.** How to deliver work, when to merge, what to double-check, what the
  owner wants reported.
- **An accepted trade-off or known gap** that the owner saw and didn't reject (for example,
  "monsters above the level cap stay for now"). Mark it as open, not decided.

Don't add one-off task details, anything already obvious from the code, or anything the owner
rejected. When a new instruction contradicts an entry, replace the entry; don't keep both. If
you can't tell whether something is a lasting rule, add it and say so in your summary, so the
owner can strike it.

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

### Boost IDs
- Every boost needs a unique `boostId`. A shared ID silently drops one item's bonus.
- `20000`–`29999` is reserved for the armor-set generator; hand-made items use `30000`+.
- The test enforces both.

### Slots, colors and sprites
- Capes and mantles use the **BACK** slot. The `CAPE` enum value has no inventory slot, so an
  item there can't be seen or taken off.
- Recolored cape art: `NMS_NewCape01` (red) and `__pal2` blue, `__pal3` purple, `__pal4`
  pink, `__pal5` orange, `__pal6` gold, `__pal7` green, `__pal8` black, `__pal9` white. The
  inventory icon has the same name prefixed with `Inv_`.
- **Element colors:** fire red, water blue, earth green, air gold, dark black, light white.
  The six archmage mantles (sold by Archmage Thalindra with "mantle") use them. High-level
  mage gear should require about **600** in its main casting stat.

### Legendary weapon coverage
- "Legendary" has no separate tier field — an item becomes legendary purely by setting
  `"unique": true` (see `compendium/app.js`'s `rarityOf()`). There's no cap on how many
  legendary items may exist, and no fixed ratio between weapon types.
- Every weapon archetype should have at least one legendary/unique option for endgame players:
  a strength-classed melee weapon, an archer weapon (bow), and an intelligence- or
  wisdom/hybrid-classed caster weapon. Before T4C-0028 there were three unique melee weapons and
  zero unique bows or caster weapons. Check this coverage whenever "not enough legendary
  weapons" comes up again — add the missing weapon type, don't just add another melee option.
- A caster weapon (`bodyPart: WEAPON`, intelligence/wisdom/hybrid archetype) follows the same
  class rules as armor (own-school power plus matching resistance, main stat via the P/10 or
  P/12 budget), but like every weapon it is exempt from the AC and skill/power/resistance
  *budget* checks in `ItemBalanceGuidelinesTest` — only its main-stat bonus is checked exactly.
  Keep the power/resistance amounts in line with the budget anyway (as if they were checked) for
  internal consistency with the rest of the item's numbers.

### Boss loot tables
- A boss should drop **multiple different items**, not one signature item plus a couple of
  potions. The established pattern (Ignarok, Mordrenn, Arch Drake, Greater Drake, Centaur King,
  The Verdant Warden, Ysolde the Veiled Matriarch) is: the boss's own unique/legendary item at a
  low chance (0.008–0.02), plus a full themed 6-piece Ancient Celestial set (~0.025 each) and its
  matching 6-piece Empyrean set (~0.012 each) — 13 drop entries in total.
- All eight `ArmorSetGenerator` flavors (fire, water, air, earth, light, dark, warrior, archer)
  are already claimed by a boss. Reusing a flavor for another thematically-fitting boss is fine
  — there's no rule against two bosses sharing a set flavor.
- When adding a new boss, or noticing an existing one with a thin (1–3 entry) loot table, bring
  it in line with this pattern instead of leaving it as a near-single-item drop (T4C-0028).
- A single named/event boss can drop more than one unique/legendary item of its own (e.g. Makrsh
  P'Tangh drops both a legendary bow and a legendary staff) when its established loot theme
  plausibly supports more than one signature weapon type.

## 4. Reference website (compendium)
- Generated from the live game data by `tools/CompendiumExporter`. CI regenerates it on every
  push to `main`, and Vercel deploys `main`.
- When adding a genuinely new spell, item, NPC, monster or quest class, add it to the
  exporter's "new content" lists.
- Show numbers the way the game really uses them. Publish computed values from the game's own
  helpers, not hand-typed copies. For example, the Seraph aura rolls 0–100 inclusive, so the
  Rebirths page shows (c + 1)/101, not c%.
- "Is the website up to date?" means the live Vercel production deployment is the current `main`
  commit, and re-running the exporter on `main` changes nothing. Check both before answering.
- Pages that exist for rules: Systems (XP curve), Rebirths (per-rebirth requirements and
  rewards), Spells, Items.
- List pages show every important stat as its own column in a flat table — for Items that's
  slot, class, requirements, AC, damage, boosts, price, rarity and source — so a player can
  compare items without clicking through. A row can still open the detail page for anything not
  worth a column (full element-colored boost breakdown, lore text, etc.), but the table itself
  must answer the basic "what does this do" questions on its own (T4C-0028).
- Never show a generic "monster drop" label for where an item comes from. Name the actual
  monster/boss (every one of them, if more than one drops it) via `lootSources.json`'s
  `monsterDisplayName`, the same way an item's own detail page already does.

## 5. Process
- Every player-visible change gets a `T4C-XXXX` ID in `TASKS.md` and a player-facing
  `CHANGELOG.md` entry (see `CLAUDE.md`).
- Open a PR, then wait for **Build and test** to go green and the **Codex** review to finish.
  Fix real findings, reply on each thread, and resolve it. Merge only after all of that.
  Never merge while Codex is still running.
- Once a PR is merged, start follow-up work on the same branch name, freshly from `main`.
- When asked to "double check and merge", re-audit your own diff first (anything that could
  still break the rules above or old saves). Then follow the merge rule above; you may merge
  yourself once it holds.
- Tell the owner about pre-existing problems you notice along the way (unwearable items,
  soft-locks, misleading displays). Fix them when they're small and in scope; otherwise list
  them as follow-ups.
