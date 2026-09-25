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
- **Warrior:** AC, resistance to five elements (never light), **strength**, **attack**.
- **Archer:** AC, resistance to five elements (never light), **agility**, **archery**.
- **Intelligence mage:** fire/water/dark power, resistance to five elements (never light),
  **extra intelligence**, and **less AC than a wisdom item**.
- **Wisdom mage:** earth/light power, resistance to five elements (never light), **wisdom**, and
  **more AC**.
- **Hybrid mage:** air power, resistance to five elements (never light), **intelligence and
  wisdom** equally.
- Mage gear never gives strength, agility, attack or archery. Warrior and archer gear never gives
  elemental power, intelligence or wisdom.
- A theme may add a flavor extra: weapon or ring damage, shield parry, dodge, or a doubled
  resistance in the theme element (never light).

### Resistance never includes light
- **No item, of any class, may ever grant light resistance (statId 21) - not even a small
  negative one as a drawback, and not even a light-power item's own school.** This was an owner
  rule change (T4C-0030): players need broad coverage against incoming damage of every type they
  actually face, not a single deep resistance plus total exposure everywhere else.
- Concretely: every class's resistance spreads across the other **five** schools
  (air/fire/water/earth/dark) instead of concentrating in one. A mage item no longer resists only
  its own power's school - it resists all five non-light schools at the same, lower rate. A
  light-power wisdom item still deals light damage; it just never resists it, same as every
  other item.
- `ItemBalance.RESISTIBLE_ELEMENTS` is the enforced set (12/13/14/15/22 - deliberately excludes
  21); `ItemBalanceGuidelinesTest.neverGrantsLightResist()` fails the build if any item's boosts
  contain statId 21 at all, positive or negative.
- One hand-made item (`wight_bound_amulet`) used to carry a small negative light resist as a
  lore-flavored drawback (an undead-bound trinket, vulnerable to holy light). That drawback was
  dropped rather than moved to a different school - a "vulnerability" flavor extra for a
  different element is still allowed by the rule above if a future item wants one, this one
  just didn't get a replacement.

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
| Mage resistance (each of five non-light schools) | P / 25 |
| Warrior/archer resistance (each of five non-light schools) | P / 40 (a theme element may double it) |

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

### The Elder Wyrms (a new legendary content line)
- The owner wants legendary/endgame content to keep expanding beyond one-off drops, and
  specifically asked for it to draw on a "pantheon" structure (as the community `t4cfantasy.com`
  Addon reference does — different named figures gating gear for different classes/levels) —
  but reflavored into this game's own fiction, not real-world deity names.
- **The Elder Wyrms**: proto-drakes that predate the named Drake line (Ignarok, Mordrenn, Greater
  Drake, Arch Drake, Lesser Drake). Where the named Drakes each embody one *element*, the Elder
  Wyrms each embody one *class archetype* instead — a deliberately different axis from the
  Ancient Celestial/Empyrean sets (which already cover all 8 element/class flavors) and from the
  named Drakes (which already cover the 6 elements). This keeps the two mythologies distinct
  instead of overlapping.
- **Pilot shipped (T4C-0029): The Rootcrown Wyrm**, the wisdom-mage exemplar — a new boss in
  Drake's Lair dropping its own unique weapon (a staff/sceptre) and 1-2 unique earth/light-themed
  armor pieces, all balanced per the rules above and *not* reusing the existing
  `ancient_celestial_earth_*`/`empyrean_earth_*`/`*_light_*` generated sets — the Elder Wyrms need
  their own distinct item identity, separate from the armor-set generator's output.
- **All five shipped (T4C-0038)** — the pantheon is complete, one Elder Wyrm per class archetype,
  all level 700 in Drake's Lair (inside its 180-radius around 2850,2780, clear of Arch Drake, the
  Dragonguards and each other):

  | Wyrm | Class | Element / cape color | Own items |
  |---|---|---|---|
  | Rootcrown | wisdom mage | earth | sceptre + 2 armor (T4C-0029) |
  | Pyreclaw | warrior | fire | greatsword, warhelm, gauntlets |
  | Mistwing | archer | water / blue cape | longbow, mantle, boots |
  | Duskmaw | intelligence mage | dark / black cape | rod, mantle, crown |
  | Galecrest | hybrid mage | air / gold cape | wand, mantle, circlet |

  The element/color picks were Claude's call (T4C-0038), following the element-color rule above.
- **Wyrm tier numbers** (Claude's call, matching the Rootcrown pilot): level 700, 125M XP, gold
  about **25 per 1,000 HP**; gear requires **950** in the class's main stat (595 int / 595 wis for
  the hybrid) and 600 endurance on armor. Weapons carry the full armor-style bonus budget, as
  Rootcrown's sceptre does.
- **Warrior/archer wyrm gear doubles resistance in its own element; mage wyrm gear does not**
  (the balance test requires equal resists on single mage items).
- **Wyrm loot** (the four new ones): own weapon 1%, each own armor piece 1.5%, the class-matching
  Ancient Celestial set 2.5% per piece and Empyrean set 1.2% per piece, serious healing potion
  30%, mana elixir 20%. Rootcrown's older six-entry table was left as it was — **open:** it could
  be aligned by adding the earth sets. The new wyrms deliberately don't drop quest crafting
  materials (Rootcrown drops a veiled aether shard for the Godsforged chain) so no quest economy
  shifts.
- Naming convention: `"<Elder Wyrm name>'s <Adjective> <Item Type>"`, keys lowercase-underscore
  with no apostrophes (e.g. `rootcrown_wyrms_verdant_sceptre`), matching the Makrsh P'Tangh
  legendary-weapon pair from the same content initiative.

### Themed armor sets (zone and weapon-matched sets)
- Beyond the two generated tiers (Ancient Celestial, Empyrean), `tools/ArmorSetGenerator` has a
  `THEMED_SETS` list for sets tied to one zone or one weapon (T4C-0038). They are appended after
  the tiers, so re-running the generator reproduces every older set file byte for byte.
- Shipped: **Centaur Slaying** (archer, 600 agi / 500 end, 7 pieces with a quiver) to match the
  Bow of Centaur Slaying; **Drowned Inquisition** (water int mage, 180 int / 45 wis / 200 end,
  dark resist doubled) for the Sunken Chancel; **Cinderforged** (fire int mage, 240 int / 60 wis /
  280 end, fire resist doubled) for Cinderreach Hills. Zone sets sit below Ancient Celestial
  (300/75/400); Centaur Slaying is a sidegrade to Empyrean archer (more agility/archery, less
  endurance/resistance).
- Formula (Claude's call): resistance per piece is about endurance / 20, plus a flat set total of
  about endurance x 0.175 split by AC share; power = 3 x P/10; main stat and combat skill = three
  single items' worth. Archer sets also give bonus endurance, like the generated archer sets.
- A set's **quiver** has 0 AC but takes a belt-sized share of the set's bonuses, plus flat weapon
  damage as its own extra. It must use the quiver item structure so bows can fire with it.
- Drop rates: **4% per piece from the zone boss, 0.4% from the zone's regular monsters**; the
  Centaur Slaying set is 1% per piece from the Centaur King and 0.2% from Centaur Warriors.
- **Every new generated set's key prefix must be added to `ItemBalance.GENERATED_SET_PREFIXES`**,
  or the balance and loot-coverage tests treat its pieces as hand-made items.

### Godsforged: a tier above Legendary
- **A new tier above Legendary** (T4C-0033), earned only through a multi-NPC crafting chain, not
  a boss drop. Reflavored into this session's own lore rather than invented from nothing: the
  **Forgewrights of the First Pact** are the last three survivors of the order that helped bind
  Avalon's original fey pact (see "Passage to Avalon" in `## 6. Quests` below) — now working
  within the Avalon Wilds, trying to reinforce what's fraying by forging relics from materials
  torn from the world's other apex threats.
- **Mechanically it's a bonus-formula multiplier, not a higher requirement.** Legendary items
  already sit at `MAX_SINGLE_REQUIREMENT` (1000) — the level-cap main stat — so there's no
  requirement headroom left to express "stronger than Legendary." `ItemBalance.
  GODSFORGED_TIER_MULTIPLIER` (1.2) is applied on top of every bonus formula (main stat, combat
  skill, magic power, magic resistance, AC) via a tier-aware overload of each — every formula's
  no-tier overload still defaults to 1.0, so no existing item's expected value changes.
  `ItemBalanceGuidelinesTest` detects the tier by key prefix (`godsforged_`, the same convention
  `ancient_celestial_`/`empyrean_` already use) and applies the multiplier when checking that
  item's numbers.
- **The crafting chain** (see `quest/definition/ForgeTheGodcore.java`,
  `BindTheGodsigil.java`, `ForgeGodsforgedWarblade.java` and its four siblings,
  `npc/EmberSmithCorvain.java`, `npc/WardenSeressa.java`, `npc/GrandmasterTholvenn.java`):
  1. Two gathering NPCs each turn a rare material (a boss drop from existing endgame bosses, not
     a new one) into an intermediate component — an ordinary single-item `QuestDef` turn-in, using
     the new `rewardItemKey` field (T4C-0033) to hand back the component instead of just gold/XP.
  2. A third NPC combines both components into the finished item. A single `QuestDef` can only
     natively track one required item, so this final step's "extra" component is checked and
     consumed by the NPC's own `javaBehavior()` *before* it calls the new
     `QuestService.completeCraftingQuest()` — which then checks/consumes the quest's own natively-
     tracked item and grants the reward in the same step. This skips the normal accept-then-
     return-later flow entirely: once a player has both components, naming the item finishes the
     forge in one conversation.
- **Raw materials are legacy Java items, not `assets/items/*.json`.** A pure crafting
  material/component has no stat requirements and isn't meant to be worn, but every file in
  `assets/items/` is assumed to be real gear and gets the full `ItemBalanceGuidelinesTest`
  treatment (a valid class, a matching AC, etc.) — a zero-requirement item fails
  `requirementsStayReachable`'s "has no class requirement" check. Author non-equippable
  materials/components the same way `item/definition/AbyssOrb.java` does (a legacy
  `ItemDefinition` with `bodyPart: null`), and remember they're invisible to the compendium unless
  explicitly added to `CompendiumExporter`'s `NEW_UTILITY_ITEM_KEYS` (its own allowlist, separate
  from `NEW_QUEST_IDS`/`NEW_NPC_IDS`) — `exportItems()` otherwise only scans `assets/items/`.
- **One item per class archetype**, following the same "one per archetype" shape as the Elder
  Wyrms line (warrior, archer, intelligence mage, wisdom mage, hybrid mage) - see the five
  `ForgeGodsforged*.java` quests for the pattern to extend if the owner asks for more Godsforged
  items later.

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
- **Every boss also needs a "medium-rarity" tier, not just rare-or-nothing (T4C-0034).** The
  13-entry pattern above packs everything into the 0.008–0.025 band — a kill that whiffs all 13
  rolls gets nothing at all, which doesn't feel like a reward. Every boss should also drop 1-2
  items in the 0.2–0.3 range (a potion pair - `serious_healing_potion`/`mana_elixir` or
  `healing_potion`/`mana_elixir` is the established default, see Ignarok/Arch Drake/Greater
  Drake/Mordrenn/Centaur King/The Hollow King/Coastwarden Ithrak/Makrsh P'Tangh) so a kill is
  never a total whiff. A boss whose own drop theme calls for something more specific (a quest
  item, a crafting material like `item.wyrmforged_ember`/`item.veiled_aether_shard`, or another
  not-quite-rare item) can use that instead of generic potions - the point is a meaningfully
  higher-odds tier existing at all, not the specific item.
- **Scale the treatment to the boss's actual tier, don't paste max-level loot onto a low-level
  one.** A "boss" whose XP/HP puts it well below the endgame roster (e.g. Deep Ones Cave's
  `DeepOneBoss`, XP in the tens of thousands vs. tens of millions for real endgame bosses) should
  get the medium-rarity tier above, not a full 1000-requirement Ancient Celestial/Empyrean set -
  that would be wildly overpowered gear for the level range it drops at.
- A boss that spawns as multiple simultaneous instances (e.g. `BastionWarden`, 4 concurrent
  spawns) still gets the full pattern if its own tier (XP/HP) otherwise warrants it - just be
  aware the multi-spawn count effectively multiplies the farm rate versus a solo unique boss, and
  weigh that when picking drop chances for a new multi-spawn boss.

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
- The Items page has Weapons/Armor/Accessories category tabs above the table (generic `tabs`
  support in `listPage`/`wireListPage`, keyed by `bodyPart`), on top of the existing search/slot/
  class/rarity filters, so a large item catalog stays browsable as it grows (T4C-0028).
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

## 6. Quests
- **Don't change the original game's quests** (owner, T4C-0038). That covers the quests and quest
  hooks carried over from the original T4C (NPC dialogue that mentions or promises a quest, such
  as the Dragon's Crypt tomb raider, Mirak's goblin bounty, Tristan's caravan or Rhodar's hammer),
  as well as their rewards. Only quests this project authored itself may be extended.
  Content-pass backlog ideas that would "finish" an original quest hook are off the table unless
  the owner asks for one by name.
- Every zone-unlock quest added by the T4C-0019 pass follows the same mechanical shape: kill N
  of a monster in one area, turn in one boss-drop item, unlock fast travel to a zone. That's a
  fine default for a minor zone gate, but it undersells a **major** new location - see below for
  when to go beyond it.
- **Ordinary zone-gate quest: add flavor, don't touch the mechanics.** Give the giver NPC a
  personal stake (why do *they* care?) and a hook forward (what's rumored to be waiting past the
  gate?) as new, purely-informational `DialogueTopic` entries (no `actions`, so they can't affect
  quest state) and richer offer/completion/completed text on the existing `QuestDef`. Never
  change `requiredKills`/`rewardGold`/`rewardXp`/the item objective to do this - those are what's
  actually saved per character, and a values change there is a balance/compat decision, not a
  narrative one. This remains the right level of effort for a secondary gate (Kraanhold's
  provinces, Deep Ones Cave, Sunken Chancel, Cinderreach Hills, etc.).
- **Major zone gate: give it real multi-stage structure, not just text.** A quest that's the
  sole gate to a flagship new location (e.g. Avalon Sanctuary) should feel like the story beat it
  is, mechanically as well as narratively. `QuestDef` supports only one target-monster/one-area/
  one-item objective, so a genuine multi-stage feel means a **chain of `QuestDef`s**: split the
  story into sequential quests (e.g. `tideworn_shore_scouts` proving stage, then
  `passage_to_avalon` as the real assault and the actual zone unlock), each with its own id,
  reward tier, and offer/completion/completed text, gated on the prior stage's completion. See
  `TidewornShoreScouts`/`PassageToAvalon`/`HarbormasterRangor` (T4C-0032) for the pattern:
  - The giver NPC needs a `javaBehavior()` override (not a plain declarative `GIVE_QUEST`
    action) to dispatch the shared keyword to whichever stage the player is actually on, via
    `QuestService.statusFor()` checks against each stage's `QuestDef` - the declarative system
    has no conditional/prerequisite dialogue support.
  - **Always check the final stage's status first.** A player who already completed the
    original single-stage version of the quest (pre-chain) must never be re-offered an earlier
    stage - route straight to (or past) the last stage if it's anything but `STATUS_NOT_STARTED`.
    This is a save-compatibility requirement, not a nice-to-have.
  - Keep the original declarative `DialogueTopic` for the entry keyword too (retargeted to the
    new first stage), even though `javaBehavior()` intercepts it before it ever fires - it's
    otherwise unreachable, but it's what the compendium's static NPC-topic export reads, so
    removing it would make the quest chain's entry point disappear from the reference site.
- Ground new dialogue in what the zone's own `zones.json` summary and existing NPCs already
  establish (e.g. Avalon's "fey pact" and its fraying, from the Fading Veil/Avalon Wilds
  summaries) rather than inventing new factions or events - see `quest-creator`'s own lore
  guidance section for why, and its noted inability to verify against `t4cfantasy.com/Addon`
  from this sandbox.
- A new quest stage must be added to `CompendiumExporter`'s `NEW_QUEST_IDS` allowlist (and a new
  NPC, if any, to `NEW_NPC_IDS`) or it silently never appears on the reference website - the
  exporter only emits quests/NPCs it's been told are new-since-fork.

### The "Two Masters" pattern (T4C-0046)
- A reusable end-of-quest **choice**: two different NPCs can both complete the same already-ready
  quest, but only one of them - whichever the player talks to first. One master ("the immediate
  one") pays the quest's own `rewardGold`/`rewardXp`/`rewardItemKey` right now, through the normal
  `giveOrReport`/`turnInReadyQuests` path, completely unchanged. The other ("the alternate one")
  swaps that payout for something smaller but **permanent-until-rebirth** - a passive perk instead
  of a lump sum. This is a reward-*shape* choice, not a new quest: it never adds a second
  `QuestDef`, never changes the original's `requiredKills`/area/objective, and both paths still
  apply the quest's own `unlockZoneId` if it has one.
- Built on `QuestService.completeWithAlternateReward(questId, player, Runnable)`: same readiness
  checks as `complete()` (`STATUS_ACTIVE`, kills, required item, `minLevel`), but takes a
  caller-supplied `Runnable` instead of granting gold/XP/item, and - critically - it doesn't check
  `giverNpc`, so any NPC can call it. Marking the quest `STATUS_COMPLETED` either way is what
  makes the two masters mutually exclusive: whichever one fires first, the other one's own
  readiness check (`statusFor(...) == STATUS_ACTIVE`) now fails.
- The alternate master needs a `javaBehavior()` override (not a declarative `GIVE_QUEST` action,
  which only knows how to call the normal path) - see `npc/OldCorrin.java` for the full pattern:
  check `QuestService.statusFor`/`killsFlag` directly to word three distinct lines (not ready yet,
  ready and offering the trade, already settled by either master), `askYesNo` to confirm giving up
  the normal payout, then `completeWithAlternateReward` on yes.
- **Proof of concept**: `passage_to_kraanhold` (T4C-0024). Dockmaster Thessaly is the immediate
  master (unchanged). Old Corrin is the alternate: instead of the gold/XP, she grants a permanent,
  per-life flag (`OldCorrin.FAVOR_FLAG`, cleared on rebirth like every quest flag) that a small
  hook in `MainGameScreen.awardKraanholdFavorBonus` reads to pay a flat XP trickle on every
  Kraanian-clan kill. **Design call (mine, flag for the owner to overrule):** a flat per-kill
  bonus, checked where kills are already centrally handled, rather than a true "+X% XP in this
  zone" multiplier threaded through the main combat XP path - that would touch code well outside
  this quest's own scope for a cosmetically similar result. Use the same flat-bonus shape for the
  next zone this pattern is applied to, unless the owner asks for the real multiplier.
- Use this pattern when a quest's completion is a natural narrative fork ("who do you report to,
  and what do you actually want from this") - not for every zone gate. Most zone gates should stay
  single-master per the "ordinary zone-gate quest" rule above.

### Quest-completion level gate (`minLevel`)
- `QuestDef.minLevel` (T4C-0035, default 0/no gate) blocks a quest's **turn-in** on a character
  level floor - `QuestService.meetsMinLevel()`, checked in `turnInReadyQuests()` and
  `completeCraftingQuest()`. Kills/items can still be gathered below the floor; only the final
  completion is blocked, and `giveOrReport`'s progress dialog and `recordKill`'s "ready"
  notification both report the level requirement instead of falsely claiming the quest is ready.
- Built for `quest/definition/TheWakingRite.java`: a short, one-time Avalon quest (owner's call:
  locked to level 125) that permanently unlocks a rebirth shortcut at `npc/AnchoriteRowan.java` -
  a proven character no longer has to re-trek to the Oracle's dungeon (and its guardian gauntlet)
  for every subsequent rebirth. Deliberately independent of the Oracle's own
  `__FLAG_USER_HAS_DEFEATED_ASSISTANT` gate, which currently has no reachable spawn point in the
  live game (`GabrielArchonis`/`GaenenElthorn` both sit at their placeholder `(0,0,0)` with no
  spawn-group wiring anywhere) - a separate, pre-existing bug, not something this pass touched or
  depends on. Use `minLevel` the same way for any future "quality-of-life unlock" quest that
  should only be reachable once a character is already well past the early game.

## 7. Economy

- **A named boss's gold drop should out-earn nearby regular monsters per unit of difficulty, not
  match or trail them.** Gold-per-1000-HP (a proxy for gold-per-time-to-kill, since HP is the
  dominant factor in how long a fight takes) should land at roughly 1.5-2x the ratio of regular,
  repeatable-spawn monsters within about 15 levels of the boss. This isn't just flavor: nearly
  every monster in the game (boss or trash) shares the same 30-second respawn timer, but trash
  mobs typically have 15-250+ concurrent spawn points on the map versus a boss's 1 (occasionally a
  handful) - so even a 2x boss premium only partly offsets that a boss is one contested world spot
  competing with everyone else, versus dozens of open trash spawns. T4C-0037 (owner's call) found
  and fixed two bosses that violated this - Mordrenn the Drowned Inquisitor (was ~26% *below* the
  ratio of level-50 trash) and the Centaur King (was *half* the ratio of level-140 trash, the
  worst-proportioned boss found) - raising Mordrenn to 200-600 gold and the Centaur King to
  1,100-2,900 gold. When authoring or auditing a boss's `goldMin`/`goldMax`, sanity-check this
  ratio against a couple of regular monsters near its level before shipping.
- **Endgame quest gold should have a ceiling well under "instantly buys everything."** T4C-0036
  (owner's call) trimmed the five `forge_godsforged_*` final-craft quests and the
  `forge_the_godcore`/`bind_the_godsigil` component quests from 10,000,000/2,000,000 gold down to
  1,500,000/800,000, and `drakes_lair_vigil`/`fading_veil_reckoning` from 5,000,000/4,000,000 down
  to 1,200,000/1,000,000 - these were an isolated top-tier cluster 5-12x above the next tier down
  (`avalon_wilds_vigil` at 800,000), which made every gold sink in the game trivial to a character
  who'd done even one of them. Any future endgame quest's `rewardGold` should land at or below this
  new ~1,500,000 ceiling unless the owner explicitly asks for a new high-water mark; don't silently
  reintroduce a 10x outlier. (`rewardXp` on these quests was left untouched - a separate, known
  `PlayerProgression.addXp` overflow risk for very large XP rewards, not this pass's concern.)
- **Vendor prices are the other half of the gold sink, not a substitute for the cap above.** Every
  town's general-goods vendor (`Fali`/Lighthaven, `Boreas`/Silversky, `Yolak`/Windhowl,
  `ChryseidaYolangda`/Stonecrest, `WayfarerBryndis`/Avalon Sanctuary) now also stocks
  `item.mana_prism` (10,000 gold) and `item.critical_healing_potion` (25,000 gold) - both
  pre-existing items that had sat unsold at trivial legacy prices (0 and 333) until this pass. When
  adding a new consumable meant as a real gold sink (as opposed to an early-game convenience item),
  price it in the thousands-to-tens-of-thousands range, not the legacy 0-500 range those two items
  had, and add it to all five town vendors' lists so it's a sink everywhere, not just one town.
  When the item is also in `CompendiumExporter`'s `NEW_UTILITY_ITEM_KEYS` (see below), reference it
  in `ShopCatalog`/the NPC's own item list with its `item.`-prefixed key (e.g. `item.mana_prism`),
  not the bare key other JSON-authored shop items use - `ItemRegistry.findByKey` normalizes either
  form at runtime so this doesn't change what the game does, but the compendium's static JSON/JS
  layer does an exact-string match with no such normalization, so a bare key here silently breaks
  that item's "sold by" listing on both the item page and the vendor's own page (Codex caught this
  on the T4C-0036 PR).
- **Two Java classes can silently define the same item key.** `ItemItemManaPrism.java` and
  `ItemItemCriticalHealingPotion.java` are the ones actually registered in `ItemDefinitions.java`
  and read by `ItemRegistry`; `ManaPrism.java` and `CriticalHealingPotion.java` are dead duplicate
  classes with the identical item key that are never referenced anywhere and were left as
  pre-existing dead code (out of scope to remove here). Found while trying to price-bump these two
  items - the first edit silently had no effect because it landed on the dead class. Before editing
  any legacy item's fields, grep `ItemDefinitions.java` for which class is actually registered
  under that key; don't assume the class with the "obvious" name is the live one.
- **A dangling `ItemDefinition.ItemSpell` id makes an item silently do nothing.**
  `ItemUseService.useOnSelf` resolves an item's use-effect via `SpellRegistry.findById(spellId)`
  and quietly returns `Failure.NO_EFFECT` on a miss - no error, no log, the player just sees
  nothing happen. `item.critical_healing_potion` had exactly this bug (pointed at 10208, a legacy
  macro id from the original game's script system that nothing in `SpellRegistry` registers,
  instead of `HealCritical`'s real id 10034) until T4C-0036 fixed it and added
  `ItemSpellIntegrityTest` to lock that one item down. **Known gap, not fixed here:** the same
  pattern turned out to affect roughly 80 other pre-existing legacy items when checked
  registry-wide (`mana_elixir`, `serious_healing_potion`, `scroll_of_recall`, several rings and
  weapons, etc.) - each would need someone to work out what spell it was actually meant to trigger
  before it could be corrected, which is real research work per item, not a mechanical fix. Treat
  this as an open backlog item, not something to silently batch-fix; if you're touching one of
  these items anyway for an unrelated reason, it's reasonable to fix its id too and note it, but
  don't scope-creep a content pass into fixing the whole list.

## 8. Interface and controls
- **Windows should look like the original game's windows.** The owner called the old storage
  screen "gruesome": text spilling outside the window, the same items listed twice, and art that
  didn't match what was drawn on it. Build screens from the original GUI art (`GUI_BackTrade`,
  `GUI_BackQuest`, `GUI_PopupBack`, `GUI_Button*`) and **tile** textured pieces (stone, parchment,
  grid cells) rather than stretching them. Keep all text inside its panel. The storage screen
  (T4C-0039) is the reference: see its `tile`/`drawThreeSlice` helpers.
- **Check a UI change by looking at it.** Render the screen off-screen (under Xvfb with an
  LWJGL3 harness) and look at the screenshot before calling it done; a compiling screen can
  still be unreadable.
- **Moving an item must never repair or recharge it.** Anything that moves items (storage,
  trade, a future bank or mail) carries each item's durability and remaining charges along with
  it (`StorageService` keeps per-item lists parallel to the storage list).
- **Everything a player owns must be saved.** Storage items and banked gold were never written
  to the save file until T4C-0039. When you add player-owned state, add it to `PlayerStateDto`
  and `PlayerStateMapper` in the same change, with a round-trip test. Old saves without the new
  field must still load.
- **A window with a text box or number prompt must override `capturesKeyboard()`**, so movement
  keys, macros and game hotkeys don't fire while the player types.
- **Every shortcut is listed in the Controls window** (Ctrl+H, or the Controls button in
  Options). When you add or change a key binding, add or update its `controls.<topic>.<n>`
  lines in `lang.json`. `ControlsScreenTest` checks that each line is complete.
- **Debug keys need a modifier.** A bare letter key must never trigger a developer action; the
  old bare-R "reload map graphics" is now Ctrl+Shift+R. Holding Ctrl suppresses walking, so
  Ctrl+letter window shortcuts never also move the character.
- **Hide unused decorations in the original art at the window's own transparency.** A screen
  that doesn't use some part of the original art (the quest journal's three reward squares, the
  macro window's six circles) must hide it with `GuiDraw.drawOverlayWithPatch`. That helper draws
  the background with that rectangle filled from plain stone elsewhere in the same art, all at
  the overlay alpha. An opaque patch drawn on top stands out whenever "Transparent Interface" is
  on, which it is by default (75%).
- **Selecting and acting are separate steps in list windows** (T4C-0040). A single click selects
  and shows details. The action (Travel, Bind) happens on a button, a double-click or Enter, so a
  stray click never teleports the player or rebinds a key.
- **Show numbers the way players say them.** Durations are converted to "45 s" or "5 min" for the
  current character (`SpellEffectManager.resolveDurationSeconds`), never shown as raw
  milliseconds or formulas.
- **Keep F2 free for player macros** (owner, T4C-0041). Built-in shortcuts must not take F2; the
  coordinates readout moved to F12. Before binding a new built-in function key, check it against
  the Controls list. Currently taken: F1, F3, F8, F9 (debug tools), F11 (fullscreen) and F12
  (coordinates). Prefer F12 or a modifier combination over another free F-key, because players
  use those for macros.
- **Radar colors (owner, T4C-0043):** other players **blue**, NPCs **green**, monsters
  **yellow**, bosses **red**. The local player is the white dot in the centre. Keep these colors
  if the radar or any other entity marker (world map, compendium pins) is extended.
  - *My calls, open to overrule:* an NPC that has turned hostile shows as a monster (yellow); a
    **boss** is any monster type with at most 2 spawn points in the whole world
    (`MonsterRank`, the same rule the compendium zone maps use), because monster definitions have
    no boss flag. Summoned or scripted monsters with no spawn point (the Mirror Echo) count as
    ordinary monsters. Radar range is 40 tiles, with an inner ring at 20 tiles (about the screen
    edge). There are no other players yet, so the blue category is wired but empty until
    multiplayer lands.
- **The character selection screen remembers the last character played** (`lastCharacterId`
  in the game preferences) and preselects it on startup and after Switch Character. Double-click
  enters, following the list-window rule above. Levels shown there are clamped to the level cap,
  like the game does on load.


## 9. The Mirror of Echoes (T4C-0042)

A boss built from the player instead of a stat table. Every number below was Claude's call when
the owner asked for an unprompted "surprise"; the owner can overrule any of them.

- **Where:** Ysmera the Mirrorwarden (`npc/MirrorwardenYsmera`) stands in the Colosseum
  (1736, 1840, worldZ 0; fast travel "Colosseum"). "echo" summons the Echo, "trials" reports
  progress, "call" brings the bound Echo as a companion. Rules live in `mirror/MirrorTrials`;
  the monster is `monster/EchoOfSelf`.
- **The Echo copies its maker when it first updates:** paperdoll (the player's own part map,
  tinted pale blue), name ("Echo of <name>"), level, attack or archery skill, agility, dodge.
  Casters (by dominant stat) throw their highest-level known attack spell at range; bow users
  shoot at range; everyone else melees. It has no AC and no kill XP.
- **Scale to the player, not to a tier table.** A hit lands for 6–10% of the maker's max HP
  times the trial multiplier `1 + 0.12 × (trial − 1)` (2.08 at trial 10). Physical hits add the
  maker's AC to the raw roll so armor can't erase them; light spells are pre-scaled by the
  5000 light-resist baseline. Its health is the strongest of the maker's first five blows times
  `14 + 2 × trial` (provisional 4 × maker max HP before the first blow). Keep any future
  "fight yourself" content on this rule: difficulty must not depend on how extreme a character's
  numbers are.
- **Rewards (first win of each trial only):** XP = `(0.25 + 0.05 × (trial − 1))` × the XP the
  current level still needs (threshold minus current XP; none at the cap). It is fed into
  progression pre-divided by `SERVER_XP_RATE`, so the amount shown is the amount credited -
  any reward that is already "a share of real XP" must do the same; gold = `level × 250 × trial`, capped at the
  1,500,000 endgame quest ceiling (section 7). Rematches after all ten pay `level × 100` gold.
  Winning trial 10 binds the Echo companion (id `mirror_echo`, rebuilt from the player's current
  look on every call and on load, since the companion save only stores the id).
- **Falling to an Echo is free:** no XP/item loss or corpse; the player wakes in place at half
  HP and the Echo fades. This also covers 3 seconds after the Echo dies, since a spell it cast
  may still be in flight. It also fades (paying nothing) past 22 tiles or after 8 minutes.
  Instant-kill effects only wound it (at most 10% of its health). **Open gap:** the free fall
  applies to any non-PvP death while an Echo is alive, so another monster landing the killing
  blow during a trial is also free - at most once per summon, since the Echo fades on the fall.
- **Progress is plain quest flags** (`mirror.tier`, `mirror.challenge`, `mirror.victories`,
  `mirror.falls`, `mirror.whisper`), so no save-format change. `mirror.whisper` gates a one-time
  login message pointing new and returning players to the Colosseum.
- **Reference website:** Ysmera is listed with the new NPCs. The Echo is deliberately *not*
  added to the monster list: its stats only exist relative to a player, so any static row would
  be misleading.
