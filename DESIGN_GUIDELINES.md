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
| Starting attributes at creation | **20 in all five**, plus a fixed **30-point class spread** (130 total). No dice: every class spends the same 30 points, only in different places. | `CharacterCreationRules.BASE_ATTRIBUTE`, `CharacterCreationRules.CLASS_BONUS_POINTS`, `CharacterClass` |
| Starting attributes after rebirth *n* | 20 + 5n in all five attributes (270 at 50) | `RebirthBehavior.startingAttributeFor` |
| Level needed for rebirth *n* | 75 + 5(n − 1) (320 for the 50th) | `RebirthBehavior.requiredLevelFor` |
| Energy points from rebirth *n* | 10 + 5(n − 1) (255 at 50) | `RebirthBehavior.energyPointsFor` |

**Stat budget.** Size every requirement against what a character can actually have.
- A never-reborn character at level L has 130 starting attribute points (20 in each of the five
  plus their class's 30-point spread) plus 5(L − 1) more.
- A 50-rebirth character has 1350 (270 in each) plus 5(L − 1).
- At the level-400 cap, a well-built caster has about **1000** in their main casting stat.
- Nothing may ask for more than a character at that level can reach. Tests enforce this for
  spells and items.

The XP curve definitions still run to level 1000, so the cap can be raised later by changing one
constant. Monsters above level 400 (up to Arch Drake at 1000) currently remain as above-cap
challenge content. Rescaling them is an open follow-up, not a decision.

**Carry capacity keeps scaling with strength well past 1000 (T4C-0054).**
`InventoryService.maximumWeight` is `2000 × str / (550 + str)`, not a substitute for a real
inventory-slot limit - just a soft weight gate. It was `500 × str / (100 + str)`, which had
already reached ~91% of its 500 ceiling by str 1000 and was effectively flat above that; gear
alone hands out up to +600 strength per item (section 3), so a heavily-equipped or high-rebirth
character routinely clears 1000-3000+ strength and the old curve made that entirely invisible.
The new asymptote (2000) is reached the same gradual way, just stretched much further out - str 50
still gives 166 (unchanged), str 100 gives 307 (was 250), str 1000 gives 1290, str 3000 gives 1690.
Guarded by `InventoryServiceTest`.

## 2. Spells

### `self.true_*` formula variables (T4C-0060)

Formulas like `self.true_r_fire`, `self.true_dodge`, `self.true_str` read the caster's own
**true** stat — base + gear + permanent quest bonuses, *excluding* any of the caster's own
currently-active spell buffs — via `DiceFormula.Context.selfTrue` (populated in
`SpellCastingService.selfTrue`). Reading the true value rather than the live/buffed one means
re-casting a buff before it expires refreshes it to the same size instead of compounding forever.
`DiceFormula` answers 0 for anything it can't parse rather than throwing, so a variable with no
substitution rule silently becomes 0 — this is why Resist Fire, Resist Ice, Nimbleness's dodge
half, Tranquility and Clear Thought did nothing for a while; check this file's mistakes log and
`SelfReferentialBuffFormulaTest` before assuming a new formula variable "just works" the same way.

- **Owner's ruling on Resist Fire / Resist Ice:** ship as written — `self.true_r_fire`/
  `self.true_r_water` add 100% of your own resistance for 60 seconds. Elemental damage is
  `raw * 100 / resistance`, so doubling a base-100 resistance is exactly a 50% damage cut,
  regardless of how much gear has already raised that resistance — the formula is self-limiting,
  not an uncapped multiplier. Light (base 5000) is never boosted by a spell, so it isn't a
  consideration here. The existing "greater" resistance potions use the same `self.true_r_*`
  formula and are intentionally left at parity with these spells; "lesser" (`/4`) and "partial"
  (`/2`) potions are weaker versions of the same rule, not separate values to rebalance.

### High-tier spell VFX: one animation per ladder spell (T4C-0069, T4C-0077, T4C-0082, T4C-0085)

**Rule (owner, T4C-0085): every ladder spell has its own impact animation.** No two of the 30
`HighTierSpellCurve` spells (6 schools x levels 150/200/250/300/350) share one, and none reuses the
impact of any other spell in the game. The single source is `HighTierSpellCurve.IMPACTS` (element ->
tier -> sprite base name); `HighTierSpellLadderTest.everyLadderSpellPlaysItsOwnDistinctImpact`
enforces it. A new ladder spell needs a new animation, not a borrowed one.

| Level | Shape | Impact | Where it comes from |
|---|---|---|---|
| 150 | bolt | `Strike<Element>` | New SpriteCook strike, painted, styled on the element's original (below) |
| 200 | area | `Eruption<Element>` | The SpriteCook pixel-art eruptions first made for T4C-0082 |
| 250 | bolt | `<legacy bolt impact>-Ascended` | Duotone recolor of the legacy impact (`tools.AscendedVfxGenerator`, T4C-0077) |
| 300 | bolt | `Grand*`, `BoulderFire`, `iceTree` | The original game's own top-tier animation per element |
| 350 | area | `Mythic<Element>` | SpriteCook upgrade drawn *from* that original (edited from its peak frame) |

**Originals per element (owner's picks, T4C-0085):** fire = the nightmare fire nuke (`NM_Fire000`),
earth = `BoulderFire`, air = `thunderstorm`, water = `iceTree`, light = `NMS_2SupraHeal`, dark =
`NM_Poison000`. They sat unused in the sprite bins. The "NM" ones were painted for *additive*
blending (opaque, on black); `SpellRenderer` alpha-blends, so drawn as-is they show a black halo.
`tools.GrandVfxGenerator` packs `Grand*` copies with `MythicVfxPacker.lumaAlpha` (alpha =
brightest channel, color divided back up), which reproduces the additive look exactly.
`BoulderFire` has its own transparency mask (`BoulderFireA`, applied by the spell mask shader) and
`iceTree` is not additive, so those two are used directly. `GrandVfxAssetTest` checks the copies
match the originals frame for frame and have no opaque black left.

**Area spells also get the shower (`GrandImpactShower`, T4C-0069):** scattered, staggered bursts
across the radius, 2 at 150 up to 6 at 350. Bolts are unaffected.

**Packing generated art (`tools.MythicVfxPacker`).** Offsets are never hand-typed.
- **Frames:** 8-64, named `<Base>-a` .. `-z`, then `-2a` ... The base name must not be a prefix of
  another sprite family (the renderer prefix-matches); `MythicVfxAssetTest` enforces this, plus
  contiguous frames and mirrored offsets (`off2X = 32 - width - off1X`).
- **Pixel art** (the Eruptions): alpha snapped on/off, `--downscale k` nearest only.
  `--ground 0` puts the lowest pixel on the ground line (the legacy fire-circle ground ring).
- **Painted art** (Strikes, Mythics): `--soft-alpha` keeps partial alpha, `--scale f` resizes
  with area averaging. Mythics use `--match <original>` so the upgrade sits exactly where the
  original does; reference boxes ignore tiny placeholder frames (NM_Fire000 opens with two 32x16
  frames parked at x=-305).
- **Openings and endings:** generated animations tend to start at full size and end on visible
  smoke. `--grow-in 3` builds an eruption from the first frame; `--fade-out 3` fades the last one.
- **Matte:** SpriteCook composites frames over #808080 before removing the background and a
  sequence can end on pure matte; always pack with `--strip-matte`.
- **Size budget:** painted frames are ~100 KB each. Do not `--hold` painted art (it stores every
  frame twice); only the tiny pixel-art Eruptions use `--hold 2`.
- **Look before packing:** `--dry-run --preview <dir>` draws every frame at its real offset over
  the tile outline.

**Spending generation credits (owner's call):** pilot one element, inspect every still before
paying to animate it, one variation per call, and reuse the asset IDs recorded in
`docs/content-ideas/mythic-spell-vfx/spritecook-assets.json` instead of regenerating. Prompts,
settings and the full run log are in `docs/content-ideas/mythic-spell-vfx.md`.

The legacy spells below level 150 keep their plain animations on purpose; only the ladder gets
new art, since attaching it to routine low-level spells would make them look inconsistent.

### Character creation

Creation is a **class picker**, not a questionnaire (T4C-0059). The eight-question personality
quiz is gone; its i18n strings were deleted with it.

- **Seven classes:** Warrior, Archer, Paladin, Cleric, Healer, Mage, Battle Mage. Adding one
  means adding a `CharacterClass` enum constant, its two i18n strings, its kit and its spells.
- **Every class spends exactly `CLASS_BONUS_POINTS` (30).** `CharacterCreationRulesTest` fails if
  one spends more or fewer, so no class can be quietly stronger than another. No class may drop
  an attribute below the 20 base.
- **Attributes are deterministic; only health and mana roll.** "Reroll" calls
  `rerollVitals`, which never touches the five attributes - the player chose those by choosing a
  class.
- **A class's starting kit must be wearable by that class's own spread, and its starting spells
  learnable by it.** `CharacterClassTest` checks every kit item's five stat requirements and every
  spell's minInt/minWis against the class's rolled stats. Raising a kit item's requirements, or
  lowering a class's stats, breaks that test rather than shipping gear a new character can't use.
- **A class that leaves an attribute at the 20 base goes without gear gated on it.** The Healer
  and the Mage spend everything on one stat, so the leather line (endurance 25) is out of reach
  for both and their kits
  simply omits boots and gloves rather than bending the requirements to fit. Caster weapons in the
  legacy catalogue nearly all gate on intelligence, so wisdom classes get purpose-built
  wisdom-gated ones (Acolyte's Earthen Mace, Acolyte's Dawnlit Staff).
- **Every kit item still needs a shop or a drop.** Being granted at creation is not a source the
  reference site can show, so starter-kit gear is also stocked by the starting town's weaponsmith
  and armourer. In `ShopCatalog`, JSON-authored items must be listed with their `item.` prefix -
  a bare key silently matches nothing.
- **Macros belong to the character, not the install.** They bind spell names, and spells are
  per-character, so they live in the save (`PlayerStateDto.macros`), not `game_preferences.json`.
  Anything else that is per-character in the same way belongs there too.
- **Kits are worn, not carried.** `LocalCharacterStore` writes them straight into the save's
  equipment map. Only shared supplies (torches, potions) and stacked ammunition go in the bag.
- **Starting spells are referenced by key and resolved through the registry**, because saves store
  spell *names*. Never write a display name into a class definition.
- Characters created before T4C-0059 have no class recorded; anything that reads it must tolerate
  `null` rather than defaulting them into one.
- **Classes must be distinguishable in the preview.** The picture is drawn from the kit's own
  sprites, so two classes wearing the same body armor with no weapon, helm, cape or shield to tell
  them apart look identical. Give each kit at least one distinct visible piece.
- **The preview must call `PlayerAnimations.refresh()` after changing the part map.** Frames are
  cached and only reload on a texture-generation change, so without it a newly dressed class keeps
  the previous one's sprites - or draws nothing at all if its own were never loaded.
- **A roster entry must have a complete character save.** Creation writes the character state
  before registering the slot. A failed write must leave the roster unchanged. Replacement saves
  use a temporary file so a failed write does not damage existing progress.
- **New characters choose Good or Evil after gender and before class.** The chosen path starts
  with +100 or -100 karma in the character save. Existing characters keep their recorded karma;
  this choice applies only when creating a new one.

**Schools and their casting stat:**

| School | Stat |
|---|---|
| Fire, water, dark | Intelligence |
| Earth, light | Wisdom |
| Air | Intelligence and wisdom equally (the hybrid school) |

- **The ladder is even.** Every school has exactly one attack spell at each of levels
  **150, 200, 250, 300 and 350**.
- **The ladder stops at 350, below the level-400 cap (T4C-0084, owner's call).** The level-400
  rung (Ashfall, Tectonic Ruin, Heavenfall, Cataclysm's Herald, Solar Apotheosis, Eclipse of
  Ruin) and the level-400 light ward Sanctum Ward were removed outright. Characters that had
  learned them simply lose them on load - spellbook, quick slots, macros and active buffs - with
  **no gold refund**. Anything else removed later goes into `spell/RemovedSpells.java` the same
  way, so old saves never keep a spell that no longer exists.
- At a given level, every school's spell has the same requirements, mana, price and damage.
  If you add a spell at a new level, add it for every school.
- Enforced by `spell/HighTierSpellCurve.java` (the single source for the numbers) and
  `HighTierSpellLadderTest`.
- **Requirements at level L:**
  - Main stat 2.5 × L; other casting stat 0.6 × L.
  - Air needs 1.55 × L in each of intelligence and wisdom.
  - So 375 at level 150, and 875 at level 350 (the top rung).
- **Damage** is `(1d(L/5) + L/2 + stat/4) × power/resist`.
  - Single-target bolts multiply by 6; area spells by 5.
  - Air uses `(int + wis)/5` as its stat term.
- **Shapes per level:** 150 bolt, 200 area, 250 bolt, 300 bolt, 350 area.
- **Support spells** above level 150 (wards, heals) take their stat gate from the same curve.
- **Hard limits:**
  - No player spell may require a level above the cap.
  - Never reuse a low-level spell's dice scale for a high-level spell.
- **Where it's taught:** Archmage Thalindra (Avalon). Naming a school opens that school's ladder;
  "train" opens the wards and heals.
- **Cast speed decays with level, floor shared with every other spell (T4C-0054).** Ladder spells
  used to have a flat, non-decaying exhaustion per shape (1600/1200/1200ms bolt, 1900/1400/1400ms
  area) at every tier and every caster level. They now decay from that same starting value down to
  the game's universal floor (1000/750/750ms) linearly between level 150 and the level cap (400),
  via `(400-self.level)` rather than `(self.level-tier)` - one formula shared by every tier, so
  every rung reaches the floor exactly at the level cap. See `HighTierSpellCurve.attack`.
- **Every element's offensive spells form one prerequisite chain (T4C-0054).** You must already
  know the previous rung (by ascending `minLevel`, ties broken by `spellId`) to learn the next one
  of the same element - e.g. Earth's `stone_shard → shatter → earthquake → boulders → ...`.
  Built dynamically in `SpellRegistry.offensiveChainPredecessor` from `playerCastableSpells()`, so
  a newly added attack spell slots into its element's chain automatically; don't hardcode a chain
  list elsewhere. Non-elemental attacks (element 0, e.g. `tame_beast`/`mana_burst`) aren't chained.
- **Skill-point cost to learn a spell scales with its `minLevel`, capped at 100 (T4C-0054).**
  Previously every spell cost a flat 5 points. See `SpellPurchaseCost.skillPointsForLevel`
  (roughly calibrated against t4cbible.com/Spells' skill-point column, stretched so the 100 cap is
  reached only at the level cap) - this is the one place that number lives; the Lighthaven spell
  seller (`SpellMerchant`) and any other trainer read it, never re-typed.
- **Internal/system spells never reach players.** `wrath_of_the_ancients` (boss aura),
  `remort_aura` (rebirth aura), `level_up` (automatic on-level-up stat tick) and `wrath_of_drake`
  (dead GM-only content) are cast via direct name/spellId lookup or aren't cast at all - never
  learned. They're explicitly denylisted in `SpellRegistry.isPlayerCastable` (T4C-0054) since none
  of them follow the `item_`/`mob_`/`test_`/`npc_` naming convention the rest of that filter relies
  on. Any future non-player spell that also doesn't fit that naming convention needs the same
  treatment, or it will silently show up in the spellbook and at the spell seller.
- **`minLevel` 0 means "not a player spell" (T4C-0061).** `SpellRegistry.isPlayerCastable` drops
  every spell with no level requirement, because nothing in the game sells or grants one - the nine
  that existed were GM tools, item-triggered gateways and cut content. A real spell that should be
  learnable from the very start gets `minLevel` **1**, not 0; `Light` was the one such spell and now
  carries 1. Guarded by `UnlearnableSpellsTest`. Note the compendium exporter's
  `NEW_SPELL_CLASSES` allow-list deliberately re-adds curated entries that fail this filter, so
  `avalon_gateway` still has a website page (the Scroll of Avalon casts it) while being absent from
  the spellbook and the seller.
- **Casting is instant; the pacing lives entirely in exhaustion (T4C-0061).** A successful cast
  launches the spell in the same frame - there is no pre-cast wait and no cast bar. The gap before
  the next action comes only from `applyExhaustion` inside `SpellCastingService.begin`, i.e. the
  spell's own mental/physical/attack exhaustion formulas and the 1000/750/750ms floor above. The old
  pre-cast wait re-used those same formulas, so it charged every spell its exhaustion twice; removing
  it roughly halved real cast cycle time without touching a single balance number. Don't reintroduce a
  pre-cast delay to "slow casting down" - change the exhaustion formulas instead. The progress bar is
  now only for harvesting and taming.
- **Self-centered area attacks (T4C-0113).** Earthquake and Flame Wave cast immediately from the
  caster when activated; they do not require a clicked target. They affect every attackable monster
  within their spell radius measured in map tiles on both axes. Earthquake reaches seven tiles and
  Flame Wave five, including monsters on the radius boundary. Safe havens block these attacks before
  mana is spent.
- **The five protection spells (Barrier, Protection, Stone Skin, Mana Shield, Mana Surge) cast in
  0ms - an explicit exception to the universal floor above (T4C-0067, owner's call).** Their
  mental/physical/attack exhaustion fields are literal `"0"`, not the level-scaled-decay formula
  every other spell uses. The floor exists to pace repeated *casting*; these five exist to be
  re-applied the instant you need them (before a fight, after a death, on a fresh double-click), and
  the owner asked for that friction removed rather than just reduced. This is these five spells'
  own field, not a change to `SpellCastingService`/`Player.applyExhaustion` - a different spell cast
  in between still exhausts normally, and casting one of these five still respects *that* exhaustion
  window if one is already running. Give any future "protection spell" in this same family (a ward
  meant to be topped up on demand, not a combat-paced buff) the same 0/0/0 treatment.

### The Ultra tier (T4C-0067)

- **Naming/shape convention for a "stronger version of an existing spell":** prefix the name with
  `Ultra ` (`Ultra Barrier`, `Ultra Protection`, ...), keep every field identical to the base spell
  (icon, projectile/impact, element, targetType, duration, mana cost, exhaustion) except the ones
  that make it a genuinely separate, stackable spell: a new `spellId`, `minLevel` **150** (the
  level-150+ tier the owner asked for), and the effect formula wrapped in `2*(...)` around the base
  spell's own formula - literally double, not a hand-tuned different number. Don't invent a new
  formula shape; wrapping the existing one is what keeps "double" verifiably true and keeps the two
  tiers' relationship obvious to the next person reading the pair.
- **Price ~1,000,000 gold to learn (owner's call, "not cheap").** Applied flat across all five Ultra
  spells rather than varying per-spell - there's no basis in the base spells' own (very different)
  prices to derive a ratio from, so a flat round number for the whole tier is simpler and just as
  defensible.
- **Stacking is the point, not a side effect.** An Ultra spell is a different spell name from its
  base version, so `Player.applyBuff`'s per-`spellName` dedup means both can be active
  simultaneously and their stat contributions add - a caster who learns both Barrier and Ultra
  Barrier gets the sum of both AC bonuses from one cast of each. This falls out of the existing buff
  system for free; no new stacking logic was needed or added.
- **minInt/minWis for the Ultra tier (Claude's call, flag for the owner to overrule):** the five base
  protection spells don't follow the level-150+ curve (`HighTierSpellCurve`, main stat 2.5×L / other
  stat 0.6×L) themselves - they're much lower-level and don't share one consistent int/wis ratio.
  Since minLevel 150 puts the Ultra tier inside that curve's range, and section 2's "support spells
  above level 150 take their stat gate from the same curve" rule, each Ultra spell uses 375/90
  (2.5×150 / 0.6×150), split main-vs-secondary by whichever stat its own *base* spell already leans
  on more heavily: Ultra Barrier and Ultra Mana Shield/Mana Surge (int-leaning bases) get
  `minInt=375, minWis=90`; Ultra Protection and Ultra Stone Skin (wis-leaning bases) get
  `minInt=90, minWis=375`. Note `SpellCastingService.begin()` doesn't actually gate casting on
  int/wis at all (only `hasLearnedSpell` + mana/exhaustion/cooldown) - these numbers only gate
  *learning* the spell in `LearnScreen.blockReason()`, same as every other spell.
- **Renew Armor is sellable, not quest-only (Claude's call, flag for the owner to overrule).** There
  is no existing "quest-only spell, hidden from the regular seller" mechanism in this codebase, and
  building one just for this spell would be new plumbing for a fairly small distinction. Renew Armor
  is priced and sold at the Spell Merchant like any other spell; a quest that also grants it for
  free/early remains a legitimate separate reward on top of that, the same way other content in
  this game is reachable by more than one path.
- **Renew Armor's mana cost and price, corrected (T4C-0079, owner's call).** It was priced as if it
  were a small convenience (40 mana, 500,000 gold) despite casting up to ten other spells' worth of
  effect in one go. It now costs **the sum of all ten recast targets' own mana costs**
  (`(10+9+45+14+39)*2` - base and Ultra tiers alike, `RenewArmorCostTest` pins this against the
  components' own live `manaCost` fields rather than a copied number) and **20,000,000 gold** to
  learn. The mana charge is flat and unconditional - it's charged whether or not the caster actually
  knows every recast target, since the price reflects what the spell is capable of casting, not
  what one caster happens to know yet.
- **A spell whose real effect isn't a dice-formula `T4cEffect` needs the `tame_beast`-style exemption
  in `SpellRegistry.isPlayerCastable`, not a fake placeholder effect.** Renew Armor's effect is
  custom code in `MainGameScreen.castDefensiveSpell` (re-casting whichever of ten other spells the
  caster knows), so it was added to that exemption list by name rather than given an inert formula
  just to pass the filter. Do the same for any future spell whose effect is pure custom logic.
### Temple blessing chests (T4C-0061)

- An **offering chest** stands outside each town's temple and lays nine wards on whoever clicks it:
  Bless, Barrier, Protection, Mana Shield, Mana Surge, Earthen Strength, Stone Skin, Tranquility,
  Clear Thought. Free, no mana, no level or learning requirement - the caster is an unseen priest,
  not the player. Re-clicking refreshes rather than stacks; there's deliberately no cooldown, since a
  second click buys nothing a first click plus a short wait wouldn't.
- **Owner's call on strength: two levels, not a per-town ladder.** The five ordinary towns
  (Lighthaven, Silversky, Windhowl, Stonecrest, the Oracle) are all served by a **200
  intelligence / 200 wisdom** journeyman and give an identical blessing; **Avalon Sanctuary** alone
  has an archmage at **500 intelligence / 1500 wisdom**. An earlier 3-tier version
  (500 int, 500 × tier wis) was rejected as far too strong for the ordinary towns - it handed out
  +121 and +189 AC for a free click. Current numbers, measured not estimated:

  | | Ordinary town | Avalon Sanctuary |
  |---|---:|---:|
  | Armour class | +59 | +258 |
  | Wisdom | +100 | +750 |
  | Strength | +33 | +135 |
  | Max HP | ~+230 | ~+1635 |
  | Attack / archery | +100 | +750 |

  Those figures are pinned by `TempleBlessingChestTest.theBlessingIsWorthWhatTheGuidelinesSay`, so
  retuning the caster stats fails that test rather than quietly leaving this table wrong. Max HP is
  approximate because Bless rolls `1d(wis/4)` into it.

  The gap is the point: a town blessing is a convenience, Avalon's is worth the journey. Avalon's
  numbers are still large against the level-cap main stat of 1000 and are deliberate - if monster
  difficulty is ever tuned against "a buffed player", Avalon is the buffed baseline.
- Caster stats live per `Shrine` rather than behind a town-rank multiplier, because the stats are the
  whole of what varies between chests, and a multiplier stopped fitting once intelligence differed
  too. Adding a middle tier means adding one more `Shrine` with its own pair of numbers.
- **A buff cast by someone other than the player must carry that caster's stats (T4C-0061).** A save
  keeps only a buff's name and remaining time; `PlayerStateMapper.applyActiveBuffs` rebuilds its
  effects on load. That was right while every buff was self-cast, but it rescaled an Avalon blessing
  from +258 to +18 armour the first time a character reloaded. `ActiveBuff` and
  `PlayerStateDto.ActiveBuffState` now carry `casterIntelligence`/`casterWisdom`, and **0 means "the
  player cast it"** - which is how saves written before this read back, so they behave exactly as
  before. Any future buff applied by an NPC, item or world object needs the same treatment or it will
  silently weaken on reload. Guarded by
  `TempleBlessingChestTest.aBlessingSurvivesSavingAndLoadingAtFullStrength` and
  `anOlderSaveWithNoRecordedCasterStillLoads`.
- The two lists that define a chest - `TempleBlessingService.SHRINES` (tile + tier) and the
  `TEMPLE BLESSING CHEST` entries in `ObjectPositionDefinitions` - must agree exactly, or a chest
  either does nothing when clicked or is unreachable. `TempleBlessingChestTest` holds them in step.
- Because the chest isn't backed by a container item, its `ObjectMapping` needs `clickAnimate = true`
  to be clickable at all: `ObjectRenderer.handleClick` only considers objects that animate on click
  or are containers.
- **Accepted gap:** Windhowl, Stonecrest and the Oracle have no temple modelled in the world, so
  their chests sit at the town's own gathering point (gate sentry, trade row, cavern arrival) rather
  than outside a temple door. Move them when those temples exist.

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

### The Wyrm Scales and The Convergent Wyrm (T4C-0047, a capstone above the five)
- Each of the five Elder Wyrms now also has a small chance (12%) to drop its own named **Wyrm
  Scale** (`item/definition/WyrmScales.java`) - a non-equippable, unique-flagged turn-in token,
  same shape as `BoundGodsigil`. Bring one of each to the **Keeper of the Sixth Seal**
  (`npc/KeeperOfTheSixthSeal.java`), standing in the Colosseum near the Mirror of Echoes and
  ColosseumClerk, and she consumes all five and summons **The Convergent Wyrm** on the spot -
  level 750 (above the five's level 700), hybrid-mage class, all-element resistance (a flat 130
  on every one of the six schools, including light - a *monster*'s resists have no "never light"
  rule, that's an item-only rule), 3 signature legendary items (a weapon, a neck piece and a
  head piece, all `HYBRID_MAGE` per the item balance formulas) plus one piece from each of the
  six Ancient Celestial elemental sets, so its own loot literally "converges" every element.
- **Repeatable, not a one-time unlock.** The seal isn't a quest flag - the Keeper just checks
  `context.hasItem()` for all five scale keys every time she's asked, so a player can farm scales
  and summon the Convergent Wyrm again on a later life. This matches how the five Elder Wyrms
  themselves are ordinary respawning world bosses, not a single-completion set piece.
- Mechanically this is an on-demand summon via `NpcBehaviorContext.summon()` (wired to
  `MonsterManager.spawnMonster` through `NpcScriptRuntime`'s summon callback in
  `MainGameScreen`), the same plumbing declarative NPC "summon" scripts already use - no new
  spawn/quest infrastructure needed. The Convergent Wyrm has no `@Spawn` of its own (pure JSON,
  `assets/monsters/convergent_wyrm.json`), same reasoning as the `arenamobxp*` family: it only
  ever needs to exist when summoned.

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
- Quest item keys may use the game's `item.` prefix while exported item keys are bare. Resolve
  both forms to the exported item before displaying a delivery requirement or making its link.
- When adding a genuinely new spell, item, NPC or quest class, add it to the exporter's "new
  content" lists. Monsters are the exception (T4C-0089): every monster is exported and gets its
  own page, original-game creatures included, tagged with an `origin` of `new`/`activated`/
  `legacy` rather than being filtered by an allow-list — a player looking up an original
  monster's drops or gold needs a page too.
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
- Every quest page must show a location — a map wherever one exists (T4C-0053). A quest with no
  zone match still gets a schematic worldmap dot from its own `areaCenterX/Y`/`areaRadiusTiles`
  rather than showing nothing. Since T4C-0055 the map carries the location and the prose does
  not: walkthroughs no longer recite coordinates or tile radii, they say "the marked area".
- **Naming a person is not telling the reader where they are (T4C-0062).** Every quest step that
  says "talk to X", and every NPC page, must also answer "and where is X?". The site answers it
  from data, never from prose: each NPC exports its own `@Spawn` position and the fast-travel
  landmarks export from `NamedLocations`, and the page derives a landmark, a bearing and a
  distance from those two. Three rules keep that honest:
  - **Only name a landmark the reader can actually have.** A landmark gated behind a zone unlock
    is offered only when some quest *other than the one being read, and given by somebody else*,
    opens it at or before this point on the road. Elder Ophira stands in the Avalon Wilds, but
    the Wilds entry is her own quest's reward, so her page routes via Avalon Sanctuary instead.
  - **Only name a landmark you could walk from.** Past ~800 tiles the claim stops being a
    shortcut and becomes a lie — two dungeon-level containers sit that far from the only landmark
    on their level, with no route between. Those get their coordinates and no directions.
  - **Only link a map that really pins them.** Being listed under a zone is not the same as being
    inside that zone map's cropped box; four quest-givers are stationed back in town, outside it.
    Link the map that carries the pin, or no map at all.

  **Known gap (T4C-0062):** Tide Warden Bryn, Sentinel Corwin, Outrider Halvard and Dockmaster
  Thessaly are listed under a zone whose map does not reach them, so they show no map link. The
  fix is on the map side (widen the crop, or pin the giver separately), not the prose side.

  **Deferred (T4C-0062):** showing a picture of each NPC alongside the directions. NPCs are
  paper-doll composites of body-part sprites, so this needs a new headless exporter compositing
  them out of the sprite bins. **Partially superseded by T4C-0089**, which added monster
  portraits (`MonsterSpriteExporter`, single-sprite-sheet monsters only, no compositing needed)
  — the same approach does not extend to NPCs' multi-layer paper-doll rendering, so this NPC
  half of the gap is still open.

- **Monster portraits (T4C-0089):** `MonsterSpriteExporter` writes one PNG per monster (its
  walk-cycle frame 0, facing angle "000") straight from the packed sprite database
  (`SpriteBinIO`, already PNG-encoded per entry), into `compendium/data/sprites/monsters/`, with
  a `monsterSprites.json` sidecar `CompendiumExporter` merges in as each monster's `sprite`
  field. Run it before `CompendiumExporter`, same ordering as `MinimapExporter` for zone maps.
  Only covers monsters with a named walk-cycle sprite sheet; monsters drawn through the
  appearance/item-layered puppet system (no `walkPattern` of their own) get no portrait — this
  is most named creatures but not humanoid mobs wearing generic equipment layers.
- **Inline row expansion (T4C-0089):** the Monsters/Items/Spells/NPCs tables expand a row's
  detail panel directly beneath it on click (accordion, one row open at a time) instead of
  navigating to that thing's own page. `listPage`'s `detailRender` config + `wireListPage`'s
  `toggleInlineDetail` own this; the standalone `#/monsters/:name` etc. routes still exist for
  direct links (search results, cross-references from other pages) and reuse the exact same
  `renderXDetail()` function so the two views can't drift apart.

### 4a. Editorial rules for the site (T4C-0055)

The compendium is read by **someone who plays the game and has never opened this repo**. The
`technical-writer` skill is the full standard and the pre-PR review gate; it is required reading
before changing any player-facing string, and its leak scan must be clean. The rules that bind:

- **Nothing internal reaches the reader.** No `T4C-XXXX` task IDs, class or file names,
  registry keys (`item.*`, `spell.*`), engine expressions (`self.maxmana`, dice formulas), raw
  effect rows, `@Spawn`/"spawn points", "geofence"/"world Z"/"tile radius", or dev vocabulary
  ("authored", "activated", "fully-stat'd", "placeholder stub"). Repo history is not player
  history: drop "pre-existing", "since the fork", "this pass". Data-only fields may keep ids
  the site resolves to display names; prose may not.
- **Zones, maps and quests read as one progressing journey.** The chapters live in
  `meta.json`; each zone carries `chapter`, `chapterOrder` and `nextZoneId`, and every list
  page groups by chapter and orders by level, never alphabetically or by build date. A zone
  page states what the place is, who holds it, why they turned, what it demands, and where you
  go next. A new zone without a chapter still renders, in a trailing "Elsewhere" group.
- **Technical detail is demoted, not deleted.** Formulas, raw effect parameters and similar go
  inside a `<details class="curious">` block; the default view shows the plain version (a
  damage range, a named mana cost, a percentage).
- **Prose never retypes a number the page can render.** Gold, XP and kill counts come from the
  quest data; a sentence that quotes one will drift. This is the same rule as "publish computed
  values" above, applied to prose.
- **What you hand over and what you receive are never conflated.** `requiredItemKey` and
  `alsoRequiresItemKey` are consumed on turn-in; `rewardItemKey` is granted. They render in
  separate, verb-labelled blocks. Ten walkthroughs got this backwards before T4C-0055 — several
  also told the player a boss was optional when that boss was the only source of the item they
  had to bring — so check new quest prose against the definition, not against the other
  walkthroughs.

**Open question for the owner (T4C-0055):** ten quests require a rare boss drop (0.8–2%) to
turn in, while their text had long described that item as the reward. The text now matches the
code — you bring it. If the intent was the opposite (the drop is the prize, and the turn-in
should not consume it), that is a content change, not an editorial one, and needs its own pass.

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
- Every rule in this file that can be checked automatically should have an invariant test,
  named next to the rule. A rule that exists only as prose will drift. (Recommended after the
  T4C-0025–0027 review findings; the owner may revise.)
- A new or tightened limit always comes with a decision about saves already past it: clamp what
  the game derives, keep what the player chose or spent, and cover it with a load test.
- Use the workflow skills: `balance-change` for any rule or number change, `ship-pr` to land
  it, `verify-website` whenever website-visible data or pages change, and `technical-writer`
  whenever any player-facing wording changes (they pair: one checks the data and the render,
  the other checks the words).
- **A coordinate is not a placement (T4C-0063).** Anything given a `@Spawn` has to stand on
  ground a player can walk to, and picking coordinates off a map picture does not establish
  that: deep *and* shallow water both block movement, as does scenery. Thirty-three of this
  fork's own spawns were unreachable before anyone noticed — Harbormaster Rangor in the sea
  west of Windhowl, and every Sunken Chancel creature including its boss, which made that
  zone's quest impossible to finish. Two rules, both enforced by
  `spawn/SpawnPlacementTest`:
  - Check against `worldmap.colbin`, not against the minimap PNGs. The minimaps are coloured
    from ground art, so open sea looks like a perfectly good beach on them.
  - Walkable is not the same as reachable. A spawn also needs a decent stretch of *connected*
    walkable ground around it, or it is on a sandbar nobody can stand next to.

  The fork's own content lists live in `content/ForkContent`, shared by the compendium
  exporter and that test, so adding new content can never register with one and not the other.
  **Known gap:** the inherited legacy content has roughly 200 spawns on blocking tiles. That is
  its own much larger piece of work, and the test is deliberately scoped to this fork's content
  so it stays green and meaningful rather than being switched off.

## 6. Quests
- **T4C-0038's "don't touch the original quests" default is superseded for this content specifically**
  (owner, in chat, T4C-0066): the owner has asked to audit the original game's own quests against
  the real reference data and fill in what's genuinely missing or broken - the Dragon's Crypt tomb
  raider, Mirak's goblin bounty, Tristan's caravan, Rhodar's hammer and the like are now in scope,
  not off the table. T4C-0038's original point still holds for anything the owner *hasn't* asked
  about: don't invent new rewards or storylines for original content on your own initiative -  the
  scope here is "make the real thing actually work end to end," not "improve on it."
- **Read this before assuming any original-game quest content is missing (T4C-0066).** A full audit
  found the overwhelming majority of it - the Dragon/Dark Fang chain, Mirak's Trust Quest, Stone of
  Life, Book of Feylor, the Gypsy alignment quiz, the entire Crimsonscale Letter chain, the
  Good/Evil Seraph endgame, the Oracle dungeon puzzle, most of Stoneheim - is **already real,
  spawned, working content**, just built on an older per-NPC flag system (`__QUEST_*`,
  `ADDON_STORYLINE_PROGRESS`, `npcFlag`/`globalFlag`) that predates `quest/QuestDef`/`QuestService`
  and therefore never shows up in the Quest Journal. Before writing a new NPC/monster/quest for
  "missing" original content: grep `npc/`/`monster/` for a class matching the canon name (try
  PascalCased/apostrophe-stripped variants, e.g. "Eye-Patched Qardos" -> `EyePatchedQardos.java`)
  and read its `javaBehavior()` - it is very likely already there and already working, and the real
  gap (if any) is usually a missing `@Spawn` placement, a missing kill-counter hookup, or one
  un-wired turn-in keyword on an otherwise-complete NPC, not a from-scratch build. A full canon
  reference (all 5 islands' quests, the NPC/monster/drop/trader charts) was captured from
  t4cbible.com during this pass; ask the owner or re-scrape if a future pass needs it again.
- **Drop-source audit (T4C-0090).** Cross-referenced t4cbible.com/Items against every monster's
  loot table, every shop, and every `giveItem`/`itemCount`/`takeItem` call in the Java source
  (the accurate check - not the compendium's filtered exports). Of 195 items this fork's own
  quest/NPC logic already checks for, 39 had **no source anywhere** - the same "unreachable
  quest" failure mode as the spawn-placement gap above, just for items instead of monster
  placement. 37 of the 39 are now fixed:
  - 15 were a one-line loot-table addition to a monster that already exists and spawns (drop
    chances are a judgment call, ~5-15% for a quest/crafting material, since no source data gave
    an exact rate).
  - Mordred's Key needed its giver monster actually summoned first - `Mordred.java`
    (`MOBMORDRED`) existed but nothing called `c.summon(...)` for it. Now summoned when a
    character opens the six-key door in `DeadBrotherBehavior`, and its loot entry was added.
  - 8 (Arcane Spellbook, Crown of Corruption, Fang of True Resolve, Hourglass of Essence, Key of
    Artherk, Pearl of Wisdom, Robe of Hell, Scroll of Evil Deed) needed a `javaBehavior()` NPC
    that had the right dialogue scaffolding but no logic to actually hand the item over - matches
    the "otherwise-complete NPC missing one wired keyword" pattern above exactly (see
    `TrackerOoglaThraaglurh.java`'s `skull_of_evil` trade for the pattern copied: a keyword or
    condition check, a `giveItem`/flag pair). Robe of Hell and a newly-added Assassin's Blade
    both come from one new trade on `MalachaiFatebringer` (a Scroll of Horse Friendship for a
    25% chance at either).
  - 2 (Fake Blade of Ruin, Necromantic Scroll) were a missing shop listing (`Kiadus`/`Araknor`);
    both items' own price fields were still `0` and needed setting too.
  - A quest-flag **typo bug**, not a missing source: `AnrakBrownbark`'s drum-crafting stage read
    and wrote `QUEST_FLAG_WILL_OF_ARTHERK_QUEST`/`FLAG_COUNTER_DRUM_OF_FATE` (no leading `__`),
    a different flag than every other NPC in that questline uses. That stage could never trigger
    regardless of progress, and was silently stranding Hourglass of Essence and Finely Crafted
    Drum as pointless items to carry.
  **Known gap, not yet done:** `letter_from_damien_to_xanth` sits inside a partially-wired
  "Damien subplot" (`MonsignorDamien`/`Menark`/`Xanth`, flag `__QUEST_DAMIEN_SUBPLOT`) that this
  pass could not fully reconstruct from the code alone - `MonsignorDamien` reads
  `hasItem("letter_from_damien_to_xanth")` to advance the subplot but nothing grants that letter,
  and it wasn't clear which NPC action should. Left open rather than guessed at.
- **Availability buff for island-access and good/evil-path items (T4C-0090 follow-up, owner's
  call).** The owner flagged these three quest categories specifically as painful: island
  access, the good/evil alignment path, and Oracle access. Applied to the item/monster set from
  the audit above that belongs to those categories (Red Spellbook, Blade of Heroism, Bloodstone
  Ring, Essence of Bloodlust, Grail of Purity, Moon Tug Scalp, Raw Crystal, Sword of Might,
  Diamond, Finely Crafted Drum):
  - **Drop chance x3** on each of those specific `LootDrop` entries only - not the monster's
    other loot.
  - **Every `@Spawn` doubled**, each duplicate offset by exactly (+1, +1) from the original tile
    it's copying. Deliberately small: a copy one tile off a position the original spawn already
    proved walkable is very unlikely to land on water or a wall, unlike picking new coordinates
    from scratch. Not proven safe the way `SpawnPlacementTest` proves new-fork spawns safe (that
    test is scoped to `ForkContent`, and these are all legacy monsters) - an acceptable risk
    given the alternative was checking ~2000 new tiles against `worldmap.colbin` by hand.
  - **Oracle access has no item-drop bottleneck to fix** - it's gated by killing Makrsh P'Tangh
    plus a rebirth-count requirement, not a drop. The real bottleneck there was a 24000-second
    (~6.7 hour) global cooldown in `MakrshPtangh.java`'s `SpawnerBehavior.onPopup` before he
    could be fought again, cut to 600 seconds (10 minutes) instead.
  - **Found while implementing this: Bloodlust had zero `@Spawn` anywhere** - its Bloodstone
    Ring/Essence of Bloodlust loot entries (added in the T4C-0090 audit above) could never
    actually be reached no matter the drop chance, since the monster itself never appeared.
    Given two spawns next to Xanth's own position (the only nearby placement with a verified
    walkable NPC standing on it) - own judgment call, not owner-specified coordinates.
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
- **A quest can grant a spell reward (T4C-0068).** `QuestDef.rewardSpellKey` (parallel to the
  existing `rewardItemKey`) resolves via `SpellRegistry.findByName` and adds the spell's name to
  the player's known-spells list in `QuestService.complete()`'s `grantRewardSpell` step - same
  mechanical shape as the item grant, just for `player.getSpells()` instead of the inventory. Does
  nothing (no error) if the player already knows the spell, so a repeat call (e.g. re-running
  `complete()` defensively) can't duplicate the entry. Use this instead of a custom
  `completeWithAlternateReward`/`javaBehavior()` one-off whenever the spell IS the quest's own
  reward, not an alternate to a normal payout - see `quest/definition/RenewedWards.java`.

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

### Quest Journal tabs, chain-stage display, and the Monster Kill Log (T4C-0062)
- The Quest Journal (`gui/screen/QuestScreen.java`) splits into an In Progress tab and a
  Completed tab, filtered from `QuestService.statusFor()`. This is display-only - it doesn't
  change when a quest actually completes, only where it's shown afterward.
- `QuestChainInfo` (`quest/QuestChainInfo.java`) is a small, hand-maintained, UI-only map from
  quest id to "chain name / stage N of M / prerequisite quest ids", used only to print a stage
  line in the Journal's detail panel. It enforces nothing QuestService doesn't already enforce
  (the giver NPC's own dialogue still does the real gating) - it only affects what a player reads.
  The Avalon sanctuary chain now gates `fading_veil_reckoning` behind
  `avalon_wilds_vigil`, alongside Passage to Avalon and the Godsforged chain.
  Existing active or completed Veil saves retain access. NPC dialogue must explain an offer,
  highlight the next keywords, require explicit acceptance and reporting, and stop progression
  until every kill/item objective is met. Opening a conversation must not silently turn in
  Ophira's quests. `ElderOphiraTest` covers these transitions and save compatibility.
- **The `killlog.<canonical monster name>` quest-flag namespace is reserved** for the global,
  per-monster-type kill tally (`QuestService.killLogFlag()`/`killLog()`, backing the Monster Kill
  Log screen, Ctrl+K). It's written on *every* recognized kill, independent of any quest, using
  `MonsterRegistry.findByName()`'s canonical name so aliased spawns (e.g. `"Rat"` → `Brown Rat`)
  share one counter. Don't reuse the `killlog.` prefix for a quest-specific flag - use `quest.<id>.*`
  for those, as every quest already does.

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
- **A regular monster's gold should scale with its level** (`MonsterGoldCurve`,
  `goldMin ~= level * 1.8`, `goldMax ~= level * 5.5`) - this is the roster's own established
  baseline, confirmed by sampling the existing (mostly original-game) content, not an invented
  number. T4C-0071 (owner's call, "a lvl 300 monster dropping 500 gold is ridiculous") found 19
  monsters, nearly all in newer fork-added zones, paying well under this despite a normal level
  and normal XP - in the worst case a level-300 monster paying less gold than a level-30 one
  nearby. `MonsterGoldCurveTest` floors every monster's `goldMax` at 60% of the curve's value for
  its level, so new content can't silently repeat this; a boss is expected to sit *above* the
  curve per the rule above, not just clear the floor. Exempt from the floor: anything with
  `goldMax <= 1`, this roster's existing convention for "not meant to pay real gold" (arena/
  training dummies, a handful of verbatim-ported original-game oddities like the literal `Test
  Skeleton Centaur`).
- **Plain Healing Potion doesn't drop from monsters** (T4C-0071, owner's call - "it becomes
  junk"). It's a 42-gold trivial-tier consumable that piled up faster than it was ever worth
  using; the higher tiers (`serious_healing_potion`/Major, `light_healing_potion`/Light,
  `critical_healing_potion`/Critical, `deific_healing_potion`/Deific) are unaffected and still
  drop normally. Don't add a fresh `LootDrop("healing_potion", ...)` to new content.
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
- **Every monster level 300+ drops mana_prism and critical_healing_potion at 5% each (T4C-0081,
  owner's call).** Applied as one rule in `MonsterRegistry.rebuild()`
  (`withEndgameUtilityLoot`/`ENDGAME_UTILITY_DROP_MIN_LEVEL`/`ENDGAME_UTILITY_DROP_CHANCE`) rather
  than hand-editing every level-300+ monster's own loot table - it stacks on top of whatever a
  monster already drops, and a monster that somehow already lists one of these two keys is skipped
  for that key rather than doubled up. New level-300+ content picks this up automatically; there is
  nothing to add per-monster. Guarded by `EndgameUtilityLootTest`.

## 8. Interface and controls
- **The Locations panel (Ctrl+L) must not show a place a character can't actually reach yet
  (T4C-0091, owner's call).** `NamedLocation`/`NamedLocations` supports three independent gates,
  all combined with AND when more than one is set: `unlockZoneId` (a fork zone's own unlock
  quest), `minIslandAccess` (the original islands' `__QUEST_ISLAND_ACCESS` flag - 0 Arakas, 1
  Raven's Dust confirmed reachable via `RenegadeOrcLeader`), and `requiresAnyItem` (carrying at
  least one of a set of items - used for the Oracle entries, gated on the Key of Artherk/Ogrimar
  per the canon walkthrough, not on a zone or island tier).
  - **Never gate a location on a flag/value nothing in the codebase actually sets to that
    threshold.** `Boreas`/`Yolak`'s own shop logic checks `__QUEST_ISLAND_ACCESS == 2` for a
    Stonecrest scroll, but nothing anywhere sets that flag past 1 - so Stoneheim-tier locations
    (Stonecrest, Araknor, Dionysus Silverstream, Grant Hornkeep, Filandrius, Makrsh Ptangh) stay
    unconditional for now rather than being gated on 2, which would hide them from every
    character permanently instead of progressively. Grep every setter of a flag before gating
    anything new on a threshold of it, the same way `SpawnPlacementTest` protects new spawns -
    the failure mode here is silent and looks identical to "working as designed" until someone
    goes looking for why nobody has reached level 2.
  - **Island membership per landmark/NPC was sourced from the canon walkthrough text**
    (t4cbible.com), specifically the `"NPC (located at Island)"` phrasing, not from word-frequency
    counts across the per-island quest pages - those pages reference NPCs from other islands
    constantly (a Stoneheim quest chain sends the player back to `Lance Silversmith` in Arakas),
    so raw mention counts are unreliable; the explicit "(located at X)" sentences are not.
  - **Known gap:** the Oracle Realm's dungeon between its entrance (the Ivory Chest) and the
    Oracle NPC himself - roughly twenty more chests and monster rooms across five named chambers
    (Perseverance, Deception, Illusions, Swiftness, Despair), per the walkthrough - is not built
    in this codebase at all. The Ivory Chest's coordinates (2660, 2610, world Z 2) were found by
    the owner walking there in a live client session; there's no NPC or object there yet, hence
    the "it's empty" the owner saw clicking it. Building that dungeon is its own large content
    pass, not something to attempt inside a teleport-gating fix.
- **Windows should look like the original game's windows.** The owner called the old storage
  screen "gruesome": text spilling outside the window, the same items listed twice, and art that
  didn't match what was drawn on it. Build screens from the original GUI art (`GUI_BackTrade`,
  `GUI_BackQuest`, `GUI_PopupBack`, `GUI_Button*`) and **tile** textured pieces (stone, parchment,
  grid cells) rather than stretching them. Keep all text inside its panel. The storage screen
  (T4C-0039) is the reference: see its `tile`/`drawThreeSlice` helpers.
- **Check a UI change by looking at it.** Render the screen off-screen (under Xvfb with an
  LWJGL3 harness) and look at the screenshot before calling it done; a compiling screen can
  still be unreadable.
- **Moving an item must never recharge it.** Anything that moves items (storage, trade, a
  future bank or mail) carries each item's remaining charges along with it (`StorageService`
  keeps a per-item charge list parallel to the storage list). Items have no durability or
  breakage mechanic (removed in T4C-0066, owner's call) - gear never degrades and never needs
  repair, so there is nothing to preserve on that front.
- **Everything a player owns must be saved.** Storage items and banked gold were never written
  to the save file until T4C-0039. When you add player-owned state, add it to `PlayerStateDto`
  and `PlayerStateMapper` in the same change, with a round-trip test. Old saves without the new
  field must still load.
- **Progress is saved on a timer, not only on the way out.** `MainGameScreen` autosaves every
  `AUTOSAVE_INTERVAL_SECONDS` (**15 s**, owner's number), on level-up, and on `pause()` (window
  minimise/close), on top of the event-driven `savePlayerState()` calls. Before T4C-0064 plain
  play — XP, levels, kills, loot, walking — only reached disk if the client got to `dispose()`,
  so a crash or a force-closed window threw the session away. Event-driven saves are still worth
  adding for anything expensive or irreversible; the timer is the floor, not the plan.
- **A quest-giver that isn't a `ScriptedNpc` needs its turn-in wired by hand.**
  `ScriptedNpc.onInteractStart` calls `questService.turnInReadyQuests(id, player)` on every
  greet; a `BaseNPC` that overrides `onInteractStart` (the Lighthaven Samaritan did) gets no such
  call, so its quest can be accepted and finished but never completed. Any NPC that calls
  `giveOrReport` must also have a path that calls `turnInReadyQuests`.
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
- **Hovering a spell/skill/item row shows what it does (T4C-0054).** `HudTooltip` is the one
  reusable hover-tooltip primitive (cursor-following, auto-clamped to screen edges); build its
  text with a `*TooltipText.build(...)` static helper next to the data type it describes
  (`ItemTooltipText`, `SpellTooltipText`) rather than inlining strings in the screen class.
  `GuiListScreen` (shared by `LearnScreen`/`ShopScreen`) shows the row's name plus an overridable
  `rowInfoText(row)` plus the blocked-reason on every hover, not just when blocked; a new list
  screen gets this for free by overriding `rowInfoText`. `SpellBook` and `Statistics` wire
  `HudTooltip` directly since they aren't row-list screens.
- **Press-and-hold repeats a "+1" button instead of one click per point (T4C-0054).** `GuiButton`
  itself fires its callback again every ~60ms after an initial ~350ms hold, checked from its own
  `render()` (already called every frame) rather than a new update hook - every spin/plus button
  in the game (stat points, skill points, spell/shop baskets) gets this for free with no per-screen
  change needed.
- **A monster's corpse fades from view 3-5 seconds after death (T4C-0080, owner's call), well
  before it actually respawns.** `BaseMonster.isCorpseVisible()`/`corpseHiddenAtMs` (set in `die()`,
  randomized per death so a field of corpses doesn't blink out in unison) gate the render call
  only - the entity itself keeps ticking toward its own `respawnTime` exactly as before, so aggro/
  loot/quest-kill bookkeeping is untouched. The field defaults to `Long.MAX_VALUE` ("never hide"),
  not 0, so a subclass that flips `isDead` directly without going through `die()` (`EchoOfSelf`'s
  `fade()`) still plays its own death animation instead of vanishing instantly. Guarded by
  `CorpseVisibilityTest`.


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

## 10. The Hourglass Trials (T4C-0048)

A deliberate opposite of the Mirror of Echoes, a few steps from it: where the Echo scales to the
player so difficulty is always fair, the Hourglass is a **fixed-HP, fixed-stat opponent** so a
best time actually measures something, not how strong the character happened to be that life.

- **Where:** Trial Warden Osric (`npc/TrialWardenOsric`) stands in the Colosseum (1740, 1836,
  worldZ 0), a few steps from Ysmera. "trial" summons a Sandglass Sentinel; "times" reports every
  tier's best. Numbers live in `mirror/HourglassTrials`; the monsters are
  `assets/monsters/sandglass_sentinel_1.json` through `_5.json`.
- **Five tiers, each a genuinely static monster definition** - not a scaled instance of one
  monster, five actually-separate JSON files (levels 150/300/450/600/750, stats interpolated
  from the existing `arenamobxp*`/`ARENAMOBXPnn` family so they're not invented from scratch).
  This is deliberate: a "best time" is only meaningful if the opponent never changes.
- **Tier rises with rebirths**, one step every 10 rebirths, capped at tier 5
  (`HourglassTrials.tierFor`) - the same "further along, harder challenge" shape the Mirror's
  ten trials use, just on a coarser axis since there's no player-scaling to lean on here.
- **Repeatable by design, but not a farm spot.** Every Sandglass Sentinel's own `xpOnDeath` is 0
  and its gold is small - the reward for winning isn't the kill, it's the clock. A **new best**
  at a tier pays a one-time bonus (`tier × 5,000` gold, `MainGameScreen.hourglassNewBestReward`);
  clearing a tier again without beating your own record pays nothing from the trial itself
  (only whatever the Sentinel's own small loot table drops). **My call, flag for the owner to
  overrule:** this trades "risk of becoming an efficient repeatable grind spot" for "the record
  matters" - if the owner would rather it pay a real reward every clear, drop the "only on a new
  best" gate and size the payout down accordingly.
- **Timing is not persisted state.** The in-flight clock (`HourglassTrials.ACTIVE_TRIAL_STARTS`)
  is a plain in-memory map, the same non-persistent shape `WarbandCampState` uses - a fight
  abandoned mid-way (relog, walk away) is just abandoned, nothing to clean up on load. Only the
  **best time per tier** (`hourglass.best_ms.tier<N>`) is a persisted quest flag, storing an
  elapsed *duration* in milliseconds (always small - well under the int range for any real
  fight), never an absolute timestamp.
- **Reference website:** both Osric and all five Sandglass Sentinels are listed - unlike the
  Echo, a Sentinel's stats are real and fixed, so a static monster-page row is accurate.

## 11. Lost Keys of Kraanhold (T4C-0049)

A treasure hunt with no quest log: twelve named keys, each a rare drop from one specific
Kraanhold-native monster (or, for three of them, the Windhowl War-Party, T4C-0045). A key's own
flavor text (`item.<key>` in lang.json) is the only hint where its lock waits - there is no quest
flag, no journal entry, no map marker. `item/definition/LostKeysOfKraanhold.java` holds all
twelve as non-equippable tokens, same shape as `BoundGodsigil`/`WyrmScales`.

- **My call, flag for the owner to overrule: four chest NPCs, not twelve.** A strict reading of
  "each opening one chest" would want a dozen physical chests. Consolidating to four - each
  accepting three of the twelve keys, gated on whichever `c.hasItem(...)` matches
  (`npc/SunkenLedgerCoffer.java`, `PlagueWardensStrongbox`, `WyrmlingsHoardCasket`,
  `WarbandsBuriedChest`) - keeps every key's own flavor and reward fully distinct while avoiding
  twelve near-identical NPC files for twelve locations that would mostly differ only in
  coordinates. Each chest sits near where its three keys' source monsters actually spawn, so the
  location still feels earned. If the owner wants the literal dozen, splitting one 3-key chest
  class into three 1-key ones is mechanical, not a redesign.
  - `WarbandsBuriedChest` is placed at the Windhowl War-Party's camp (T4C-0045) on purpose -
    it's the one deliberate cross-reference between this pass's two monster-camp features.
- **Ten keys pay gold + potions; two pay a real item.** Every chest's opening line is real,
  written flavor (the "lore note" the brief asked for is the chest's own response, not a
  separate readable item) - **not** `ItemDefinition.signText`, which the client never actually
  renders to the player (only `content/ItemJavaExporter`/the content-studio tooling read it
  today). Relying on a field nothing displays would have been exactly the kind of invented
  behavior `AGENT.md` rules out. The two marquee keys (Dragonguard's Sealed Key, Warlord's
  Signet Key) instead grant a small unique item each (`sealed_signet_of_the_dragonguard`,
  `warlords_iron_signet` - both `WARRIOR` archetype, `ItemBalance`-formula-compliant, well below
  the legendary tier the Wyrms/Convergent Wyrm occupy).
- **Test note:** `NpcReferenceIntegrityTest` scans every `npc/*.java` file for literal
  `giveItem("...")`/etc. keys and checks they resolve in `ItemRegistry` - but didn't load JSON
  items itself, only relying on some other test class in the same JVM fork having already done
  so first. This pass's two `giveItem` calls for JSON-authored rewards were the first real
  exercise of that path and exposed the gap; the test now loads JSON items in its own
  `@BeforeEach`, same as every other test that touches JSON items.

## 12. The Unsigned Letter (T4C-0050)

A one-time story beat, not a `QuestDef` (there's no kill-count objective - just an item and a
conversation). State lives in `quest/UnsignedLetterQuest.java`, plain quest flags as usual.

- **Trigger:** the moment `RebirthBehavior.perform()` succeeds for a character's *first* rebirth
  (`player.getRebirthCount() == 1`), called from both places that call `perform()` - `npc/Oracle`
  and `npc/AnchoriteRowan`. Grants "An Unsigned Letter" (non-equippable, `item/definition/
  UnsignedLetter.java`, same shape as `BoundGodsigil`) and shows its first line as an immediate
  system message - the same "surface it right when it happens" shape `MirrorTrials`'s
  login-whisper uses, just fired from the rebirth flow instead of login.
- **Resolution is Ysmera's, not a new NPC's.** She already watches every echo a rebirth leaves in
  the Mirror (T4C-0042) - the one character who plausibly already knows a player was just
  reborn, before they've told anyone. A new "letter" topic on `npc/MirrorwardenYsmera.java`
  checks `UnsignedLetterQuest.stage()`/`resolve()`: wrong stage or no letter in hand gets a
  in-character deflection, the right stage consumes the letter and reveals her involvement.
  **My call, flag for the owner to overrule:** the letter carries no further reward beyond the
  reveal itself - the payoff is narrative closure, not gear/gold, since this is meant as a small
  connective story beat between two features (rebirth, the Mirror) rather than new progression.
  If the owner wants it to lead somewhere further (a real questline, a reward), that's future
  work, not a retrofit of this pass.
- Not added to `CompendiumExporter`'s NPC allowlist - Ysmera's own entry already covers her; only
  a genuinely new NPC needs a new allowlist row.

## 13. Multiplayer status (T4C-0057)

**Standing rule (owner, T4C-0083): design every decision for the multiplayer game this will
become.** The client is single-player today, but new state belongs to the character (not to a
global/static or to the client install), anything one player toggles must affect only that
player, anything that changes the whole world is treated as a privileged, server-wide action,
and lookups of *other* characters go through a seam that a server implementation can replace
(for example `input/CharacterDirectory`).

**This client is single-player.** Confirmed in code, not assumed: `MainGameScreen`'s own radar
build has a "Single-player for now: no other players to show yet" comment, and there is no
`OtherPlayer`/`RemotePlayer`-style entity, no network client, anywhere in `src/main/java`. The
only real friendly-clickable entity today is the player's own companion
(`npc/companion/CompanionNPC.java`), which has no AC/mana/resistance model - only HP.

- **A spell that should be castable on "other players"** (T4C-0057: Barrier, Protection, Mana
  Shield, Mana Surge, Bless, Healing) currently has nothing valid to target besides yourself.
  **My call, flag for the owner to overrule:** rather than wire these onto the companion (a
  stretch interpretation, and one it can't fully receive - no buff stats to apply Barrier/
  Protection/etc to), T4C-0057 built only the target-selection half (Shift+quickbar arms a
  friendly-target cursor; clicking anything today reports "no valid target") so a real
  other-player entity slots into the existing click-resolution point later without redesigning
  the casting UI. Self-cast (plain click, no Shift) is unchanged. If the owner wants companion
  healing to work meaningfully before real multiplayer exists, that's new companion-stat work,
  not a retrofit of this pass.
- Any future networking/other-player work should extend `SpellCastingService.TargetKind.
  FRIENDLY_UNIT` (already fully wired server-side) and `tryCastFriendlyTargetedSpell` in
  `MainGameScreen` - the target-kind gating and mana/cooldown accounting already handle it.

## 14. GM ranks and GM commands (T4C-0083)

- **Every character has a GM rank, saved on the character:** player (default), GM, or Super GM.
  Saves from before ranks existed, or with an unknown value, load as a plain player.
- **Normal GMs** can use the GM chat commands. **Super GMs** can also give or take away other
  characters' GM or Super GM rank (`.gm <name> player|gm|super`), including making a GM a
  regular player again.
- **Owners:** characters listed in `gm_seed.json` (game folder, git-ignored) are always Super GM
  and cannot be demoted in game. This is how the first Super GM exists; on a server it becomes
  the operator's admin list. Nobody can change their own rank.
- **My call, flag for the owner to overrule:** commands that change the whole world rather than
  the GM's own character (`.time`/`.day`/`.night`, `.gflag` world flags, rank changes) are Super
  GM only. Everything else needs GM.
- **Regular players aren't told GM commands exist:** a GM command from a player reads as an
  unknown command. A GM missing the Super rank is told which rank is needed.
- **GM toggles are per player and not saved:** god mode, peace mode (monsters ignore you),
  noclip and speed reset when the character is loaded again.
- **Which of the original game's GM commands we port** (from the owner's `GM_COMMANDS.xlsx`
  review): only commands that help build, test or balance content, or that a multiplayer server
  will need. Tier 2 (buff/dispel, monster counts, item info, show internal IDs, take-all,
  rebirth count, self-slay) and player commands (`.roll`/`.dice`, XP/damage-per-hour meters,
  clear chat, FPS) are planned next. Moderation (kick, mute, lockout, IP), per-feature server
  toggles, and the old NMS-server-only commands are left out until there is a server to use them.

## Avalon map construction (T4C-0099)

- **Owner direction:** Avalon should ultimately offer the playable extent and varied destinations
  of a full classic island such as Arakas, Raven's Dust or Stoneheim. Painted empty ground alone
  does not meet this goal. The approved first pass is the sanctuary, one hunting region and its
  staged NPC quest; the remaining island expansion is open work.
- **Readable ground:** keep characters visible in travel routes and fighting clearings. Group
  trees around clearings instead of covering every walkable tile with overlapping canopies.
- **Complete structures:** reuse coherent existing buildings, carrying ground, decor, scale,
  offsets, draw order and collision together. Never assemble a temple from isolated wall sprites.
  Do not inherit another building's teleports when copying its scenery.
- **Safe sanctuary:** a temple needs enclosing walls and accessible entrances. Protected tiles
  prevent combat from either side, including delayed impacts; enemies do not acquire or chase
  protected players. Healing and intentional noncombat HP costs still work. This uses the
  existing safe-haven collision flags and is checked by `SanctuaryCombatRulesTest`.
- **Designated encounters:** place monster groups in recognizable, connected destinations,
  with quest targets inside the objective area and required bosses reachable from the giver.
  Current monster tiers above the player cap remain challenge content; do not present them as
  attainable player levels. Damage/reward balancing is a separate follow-up.
- **Verification:** inspect map changes with the actual terrain/decor renderer as well as
  collision connectivity checks. Sparse screenshots, ground-only minimaps or raw template
  masks do not establish that a playable scene renders correctly.


## World expansion and Avalon separation (T4C-0100)

- **Owner direction:** new continents must not overlap existing interiors. Check historic terrain,
  entrances and both endpoints of travel links before claiming apparently empty world space.
- **Implementation decision:** extend the world eastward from 3072×3072 to 5120×3072 and move
  Avalon +2700 X. Keep the original origin, Y coordinates and layer IDs. Existing islands,
  Library rooms, cave/stair links and dungeon/cavern/underworld dimensions remain in place.
- **Save compatibility:** migrate only identifiable old Avalon tiles, using a versioned ownership
  mask; active positions and pixel-based respawn anchors are independent. Preserve original
  interior positions in ambiguous overlapping areas. Never translate a broad rectangle of saves
  or teleport definitions. Quest progress, inventories and character stats are retained.
- **Ghost trees:** visibility applies across the blighted region, not just the sanctuary. Leave
  sparse scenery outside routes and encounters, allowing room for the whole canopy around
  player, monster and loot positions. The actual rendered scene is the acceptance check.
- The original RT map/zone data retains its original binary dimensions; additional overview
  terrain and zone names support the eastern extension independently. Editing the eastern
  terrain requires regenerating its overview, as well as the compendium's cropped maps.
- The larger dense map uses more memory and loading work; do not infer performance parity
  merely because the tile format accepts larger dimensions. Additional encounter design and
  progression balance across Avalon's extended terrain remain open work.

## New-item resale (T4C-0101)

**Owner rule:** every new droppable item must sell for more than one gold; rarer loot must sell for more.

**Implementation choice:** `ItemSalePricing` owns resale independently of the buy-price field. All JSON equipment opts in automatically, along with the new Java-defined materials, Wyrm scales, Kraanhold keys, Unsigned Letter and Avalon scroll. For zero-buy-price monster loot, value is inversely proportional to the easiest source's drop probability. Repeated independent rolls for the same item on one monster are combined before comparing sources. This prevents a rare alternate source inflating an otherwise common item's value.

Items with positive buy prices retain the existing half-price resale spread. For zero-buy-price items with no monster source, the helper defines fallback tiers for Godsforged products, unique items/crafting materials, and other equipment. See `ItemSalePricing` for the amounts and `ItemSalePricingTest` for checked examples. Undroppable items cannot be sold. The original Java item catalogue otherwise keeps its existing prices.

Ancient Celestial armor and every Empyrean item are endgame loot: each sellable item has a 200,000-gold minimum, while the ordinary drop-chance value is multiplied by 50 (up to the gold cap) so rarer variants remain worth more. This includes Empyrean weapons, not only armor. Stat requirements differ by class and do not set resale value.

The shop, item tooltip and compendium exporter use this same helper. Inventory saves store item keys, so old possessions receive current prices without migration or changes to stats, requirements or quest progress. A sale pays only for successfully removed units and respects the existing gold ceiling.

The pricing opt-in must survive Content Studio editing and Java catalogue export; `T4CContentStudioItemPricingTest` compiles the generated definition and checks both new and legacy modes. The nine new enchanted weapons/shields stocked by Lord of the Shops use positive buy prices (at twice their previous fallback resale), preventing free purchases from becoming a resale exploit; `ItemSalePricingTest` checks this shop spread.


## Map editor build workspace (T4C-0102)

- Build operations validate the whole footprint before changing it; one drag is one undo entry containing complete before/after ground, decor, scale, offsets, depth and collision. New edits invalidate redo; map switches clear history. Template files use relative coordinates and a versioned schema with bounded selections.
- Bundled structure templates come from actual checked-in maps with documented source coordinates. Building masks preserve boundary wall anchors. Custom rectangular captures preserve empty cells as well as scenery. Occupied decor requires explicit Replace; previews indicate rejection before placement.
- Templates never copy NPCs, monsters, interactive object definitions, quests or teleports. Art for stairs/chests is scenery only. Add gameplay definitions separately and validate their destinations.
- Connected wall/room tools use vetted directional sprites and corner offsets from existing buildings. Do not rotate a bitmap or assume every wall family shares the same layout. Generic Line supports manual variants for other families. Walls block movement; room outlines need separately authored doorways and floors.
- Sprite IDs remain unchanged. The editor derives searchable display metadata, retains access to every variant, and stores favorites/recents locally. Never regenerate source maps when rebuilding the editor template library.

- Sprite selection defaults to Paint. A palette drag begins a repeatable stroke at its first canvas sample; a click/quick drop places one sprite. Stamp is the explicit single-placement tool. Templates remain single structures per drag. Do not paint beneath the sidebar; a release over the sidebar cancels the stroke. Each stroke remains one atomic undo entry (T4C-0103).

- Multi-select is a dedicated sidebar mode: box selection uses decor anchor tiles; Shift-box adds, Shift-click toggles, and dragging an existing selection moves the group as one undo entry. Select retains legacy single-object editing. Moving scenery preserves ground and original sprite metadata, never moves actors/travel links, and rejects occupied destinations even when Replace is on. Collision 1/2/9 on selected anchors travels with decor; water and area flags stay at their coordinates. Reject blocking decor over a nonzero destination collision to avoid overwriting zone rules. Undo/redo, tool changes and map switches clear the selection (T4C-0104).

## Avalon outer islands and landmarks (T4C-0105)

- The 5120×3072 world contains exactly two new Avalon islands in this pass: Moonwake Shoals north of the Wilds and Emberglass Crown south of the blighted mainland. Both are joined to existing land by narrow walkable causeways. The mainland gains Crescent Pools, Oathstone Grove and Sable Fen. Keep the region's names and stories original; the Realmud and T4C Fantasy map layouts are structural references only.
- These are terrain and scenery destinations. Do not treat the copied building art as functional stairs, chests or teleports. Spawn groups, level bands, bosses, quest dialogue and travel services are separate follow-ups. Preserve existing NPC/monster/quest coordinates and island origins when adding those systems.
- Preserve the user's current handmade Avalon scenery at (3790–3818, 1228–1265). New island terrain must claim untouched ocean; detail on existing land must leave decorated, blocked and safe-zone tiles intact. Keep road and encounter centers visible rather than introducing dense tree canopies. Validate reachability from the Avalon sanctuary and inspect runtime ground/decor rendering after map changes.
- `docs/avalon-region-expansion.md` records the site dimensions, coordinates and future encounter clearings. `AvalonRegionBuilder` is a one-time authoring tool, intentionally guarded against repainting an already edited island.
## Avalon and the Hollow Dawn campaign (T4C-0106)

- **Owner direction:** Avalon is the self-sufficient level 200-300 region. A distinct third eastern land, Threnody Reach, serves levels 300-400. Avalon 200-300 should take roughly 10-30 hours; treat that as a playtest target, not a guarantee from quest XP alone.
- **Story direction:** an original ancient evil god with minions and lieutenants, revealed through a multi-stage NPC-led quest. The tone may draw on broad epic-fantasy themes, but names, places, characters and plot must be original. The Hollow Dawn lore and route are in `docs/avalon/hollow-dawn-campaign.md`.
- **Implementation choice:** the first two Ophira quests remain save-compatible and use kill objectives; the 2% unique equipment drops are optional. The later route offers two alternative two-stage accounts and six shared Threnody stages. Active stages require acceptance, marked kills, a report and a level floor before progressing. The Pale Cantor and Dusk Regent stages also require guaranteed quest relics consumed at turn-in. Quest completion flags unlock both Threnody travel and the final court.
- **World-map safety:** the world now has 6144x3072 tiles. Old 5120-column coordinates, stair links and cave links stay fixed. Threnody's camp is safe, outer courts are connected, and the inner arena is physically separated by water until its gated travel destination opens. The one-time builder must preserve the previous world rows.

## Witness Isles story, hunting and equipment (T4C-0107)

- **Owner direction:** the story starts on Stoneheim, and travel earned to the Witness Isles and Threnody persists across rebirth. Present a small number of NPC-led choices, with quoted keywords that lead to the next conversation, speaker and objective. A quest must wait at a deed, item or level gate until the player satisfies it; do not rely on an external walkthrough.
- **Implementation choice:** "Witness Isles" is the new player-facing name for Avalon. Preserve existing `avalon_*` quest/item/zone identifiers for save compatibility. Choose either the Moonwake witness account or the Emberglass warder account after Ophira; a branch witness must give a saved clue before the lieutenant quest, while old accepted/completed saves remain valid. Both branches converge on Vael's Threnody story.

## Connected main story (T4C-0109)

- **Owner direction:** Arakas, Raven's Dust, Stoneheim and Oracle history should lead into the Witness Isles and Threnody as chapters of one adventure. Each NPC must give the player the next useful keyword, destination or deed in conversation. The game should be playable without an external walkthrough.
- **Implementation choice:** A new missing-names investigation links existing mainland witnesses to Rangor's passage without changing classic quest endings. Its `witness_story.*` evidence flags are durable, each conversation has a prerequisite, and `story`/`investigation` always repeats the next lead. The mainland evidence is optional for veteran passage and does not force a replay of older quests.
- **Lore continuity:** Oberon became Makrsh P'Tangh; Lothar's heart imprisons Gluriurl; the Harbinger's judgments remain their own history. Rhunor is a separate threat exploiting erasure of mortal testimony. Neither Makrsh's defeat nor rebirth is retroactively made a prerequisite for the new investigation.
- **Branch and objective contract:** Moonwake/Ilyra/Pale Cantor and Emberglass/Soren/Cinder Marshal are alternatives, not cumulative requirements. Their witness must be heard before the clue is granted on new playthroughs. Existing accepted lieutenant stages continue. Maelin and Vael's chapter-specific lines state the next speaker, objective and keyword, and Vael's epilogue follows Rhunor's completed stage. Existing travel and quest flags survive rebirth.
- **Future content boundary:** The current route reuses the established maps, enemies and rewards. Extra rooms, bosses or item-delivery steps can expand it later only with corresponding reachable dialogue and save-compatible gates.
- **Owner direction:** leveling is primarily monster hunting. Spread enough ordinary enemies around each level band for one or two players, with quick refill when players move between clearings. Keep bosses on their own slower cadence. Density and XP per hour need live playtesting before treating the 10-30 hour target as achieved.
- **Owner direction:** high-level items should offer build choices in armor, robes, wings, jewelry and several weapon types, with coherent elemental colors. New droppable items must have an obtainable source and sell for more than one gold; rarity should increase resale. The first equipment families use six elements. Existing palette sprites color robes and wings, but matching colored plate sprites are not in the shipped pack, so distinct plate visuals remain open work.
