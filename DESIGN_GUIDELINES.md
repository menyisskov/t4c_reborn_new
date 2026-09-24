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
- **Not yet built**: the other four class archetypes (warrior, archer, intelligence mage, hybrid
  air mage), each as its own Elder Wyrm with its own weapon (+ 1-2 armor pieces), following the
  Rootcrown Wyrm's pattern. This is an accepted open gap, not a decision to stop at one — expand
  it the same way if asked for "more Elder Wyrms" or "finish the pantheon."
- Naming convention: `"<Elder Wyrm name>'s <Adjective> <Item Type>"`, keys lowercase-underscore
  with no apostrophes (e.g. `rootcrown_wyrms_verdant_sceptre`), matching the Makrsh P'Tangh
  legendary-weapon pair from the same content initiative.

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
- **Two Java classes can silently define the same item key.** `ItemItemManaPrism.java` and
  `ItemItemCriticalHealingPotion.java` are the ones actually registered in `ItemDefinitions.java`
  and read by `ItemRegistry`; `ManaPrism.java` and `CriticalHealingPotion.java` are dead duplicate
  classes with the identical item key that are never referenced anywhere and were left as
  pre-existing dead code (out of scope to remove here). Found while trying to price-bump these two
  items - the first edit silently had no effect because it landed on the dead class. Before editing
  any legacy item's fields, grep `ItemDefinitions.java` for which class is actually registered
  under that key; don't assume the class with the "obvious" name is the live one.
