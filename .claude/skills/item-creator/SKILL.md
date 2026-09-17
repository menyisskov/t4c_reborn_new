---
name: item-creator
description: Adds new in-game equippable items (weapons, armor pieces, amulets, bracelets, rings, belts, capes, and other gear) to the T4C Reborn codebase as author-friendly JSON, wires their stats/boosts, picks an appearance sprite, and decides whether they're shop-bought or monster loot. Use this whenever the user wants to add, design, or balance a new item, even if they only describe a concept or a stat block and don't say the word "item" — e.g. "give the fire mages a new ring", "I want a tougher belt for tanks", "add a rare dagger that drops off the dragon", "here's a set of armor stats, can you make it a real item".
---

# T4C item creator

New equippable items are authored as **JSON files**, not hand-written Java classes. This skill
covers the JSON schema, how to pick stats, how to wire drops/shops, and where the current
codebase has real limitations you should be upfront about rather than paper over.

## Where things live

- **New items**: one file per item at `assets/items/<key>.json`. Loaded at runtime by
  `item/json/ItemJsonLoader.java`, parsed against `item/json/ItemJsonDef.java`, and merged into
  the live registry via `ItemRegistry.registerAdditionalDefinitions` (see
  `item/ItemRegistry.java`). No code change or restart-time registration step is needed beyond
  dropping the file in `assets/items/`.
- **Do not** create new files under `src/main/java/com/perso/T4C/item/definition/*.java`. That
  directory (5000+ files) is the legacy catalog, generated output of
  `content/ItemJavaExporter.java`, and is not where new items belong.
- A generator, `tools/ArmorSetGenerator.java`, produced the 96 files currently in
  `assets/items/` (two 8-flavor, 6-piece armor sets). It's a good reference for the "themed
  multi-piece set" pattern (see below) and for what sprite names are already known-good.

## The JSON schema, annotated

Full field list: `item/json/ItemJsonDef.java`. Real example, `assets/items/empyrean_dark_protector.json`:

```jsonc
{
  "key": "empyrean_dark_protector",       // unique id; also the string other files (shops, loot tables) reference
  "name": "Empyrean Dark Protector",      // display name
  "bodyPart": "BELT",                     // see BodyPart enum — references/stat-ids.md
  "appearanceInventory": "64kInvBelt",    // inventory icon sprite name (must exist — see "Verifying a sprite name" below)
  "price": 0,                             // 0 = drop-only, not sold anywhere. Nonzero = shop-buyable at that price.
  "weight": 2,
  "armorClass": 38.1,                     // 0 for pure jewelry; real armor pieces scale with tier, see below
  "dodgeLost": 0,                         // dodge-skill penalty for wearing this (heavy armor would set this > 0)
  "requirements": {                       // player must meet ALL of these to equip; gates the item to a level/build
    "endurance": 550,
    "strength": 0,
    "agility": 0,
    "intelligence": 200,
    "wisdom": 200,
    "attack": 0
  },
  "attackSpeed": 0.0,                     // display-only outside weapons; see "Weapons" caveat below
  "unique": false,
  "isBow": false,
  "unlimitedUse": true,
  "numId": 0,                             // leave 0 unless you specifically need a legacy numeric id
  "structure": 2,                         // 2 = normal equipment (default). 3 = container/chest — don't use for gear.
  "appearanceId": 235,                    // legacy int icon fallback via ItemIconDefinitions.java; optional if appearanceInventory is set
  "undroppable": false,
  "boosts": [                             // see references/stat-ids.md for the full statId table
    { "boostId": 20425, "statId": 12, "expression": "9" },   // Air resist +9
    { "boostId": 20426, "statId": 13, "expression": "9" },   // Fire resist +9
    { "boostId": 20431, "statId": 24, "expression": "14" }   // Dark power +14 (this item's theme)
  ]
}
```

Fields not shown above that you rarely need to touch: `appearanceEquippedPrimary` /
`appearanceEquippedSecondary` (the sprite shown on the paperdoll when worn — armor pieces set
this, e.g. `"PupPlateBody"`; belts/rings in the current set leave it blank and rely on
`appearanceInventory` only), `secondaryBodyPart` (for two-slot items like gauntlets — see
`references/stat-ids.md`).

`expression` is a formula string evaluated per-player at read time (supports things like
`"self.true_attack*30/100"` or dice notation) — for a flat bonus just use a plain number as a
string, as every current JSON item does.

## Picking `bodyPart`

Use the `BodyPart` enum (full list in `references/stat-ids.md`). A few non-obvious ones:
`BELT` and `CAPE` are real armor slots with meaningful AC in this codebase (not pure jewelry —
see the empyrean "protector" belts, AC 38.1 each); gloves/gauntlets occupy **both**
`LEFT_HAND` and `RIGHT_HAND` via `bodyPart` + `secondaryBodyPart`, each with its own sprite.

## Picking `armorClass` and `requirements` (level-appropriate gating)

Look at what's already in `assets/items/` for calibration rather than inventing numbers from
scratch:

| Tier | Example | End req | Int/Wis req | Per-piece AC (6-piece set) |
|---|---|---|---|---|
| Tier 1 "Ancient Celestial" | `ancient_celestial_*` | 400 | 150 / 150 | ~2x the reference "platemail" split |
| Tier 2 "Empyrean" | `empyrean_*` | 550 | 200 / 200 | ~3x the reference "platemail" split |

Class-flavored pieces (warrior/archer variants) add `strength: 500` or `agility: 500` on top of
the elemental base requirements instead of int/wis.

Jewelry in this codebase is far more concentrated: `ring_of_the_archer.json` requires
`agility: 750` and, being a single item rather than 1/6th of a set, carries a much bigger single
boost (`+150` agility, `+250` archery) than any one armor piece gets. **Requirement magnitude
should track power magnitude** — a drop-only, single-slot item with big flat boosts should gate
on a correspondingly high stat requirement so it isn't usable at low level.

`requirements.attack` gates on attack-skill level and is separate from the raw attributes.

## Choosing `boosts[]` — statId and boostId

Full statId table and the boostId-uniqueness rule (**read this before writing any boost — it's
a real correctness hazard, not a style nit**) are in `references/stat-ids.md`. Short version:

1. Decide what the item should *do* (resist an element, deal more spell damage of an element,
   raise a combat/thief skill, raise a raw attribute, raise AC) and look up the matching
   `statId`.
2. Grep the current max `boostId` in `assets/items/*.json` and claim ids above it (see the
   reference doc for the exact command and current ranges in use). Never reuse an id already in
   another file — the engine sums boosts by `statId` but keys the active set by `boostId`, so a
   collision between two equipped items silently drops one of them.
3. Keep `expression` a plain number string (`"9"`, `"14"`) unless you have a specific reason to
   use a formula — that's the convention every current JSON item follows.

## Color/graphics matching for elemental items

**What I could confirm in-repo:** the existing 96 elemental armor pieces (`empyrean_fire_*`,
`empyrean_water_*`, `empyrean_dark_*`, etc.) all reuse the exact same generic sprite per slot
regardless of element — e.g. every `*_protector.json` uses `"appearanceInventory": "64kInvBelt"`
and every `*_armor.json` uses `"appearanceEquippedPrimary": "PupPlateBody"`, whether it's the
fire, water, air, earth, light, or dark flavor (verified by diffing `empyrean_fire_armor.json`
against `empyrean_water_armor.json`). There is currently **no per-element colored sprite
variant** wired for this armor line — the element is expressed only through the item's *name*
and its *boosts* (fire → statId 13/17, water → 14/18, etc. — see the reference table), not
through distinct art. Jewelry icons follow the same pattern: `item/definition/*.java` shows
`64kInvRings 1`–`5` and `64kInvNecklace 1`–`3` as plain numbered variants, not color-named.

**What I could not confirm:** I attempted to fetch `https://www.t4cfantasy.com/Addon` and
`https://www.t4cbible.com/itemeffect` (the community's canonical references for T4C's item/lore
color conventions) but both domains are blocked by this environment's network egress proxy, and
a web search turned up no page content with concrete color-to-element mappings. **A human should
manually check t4cfantasy.com/Addon before treating any color choice as canonical lore** — don't
invent a fire=red/water=blue/etc. scheme and present it as established fact.

**Practical rule:** if a genuinely different-colored or element-themed sprite already exists for
the slot you need (check via the verification method below), prefer it and use it consistently
for that element. Otherwise, follow the observed in-repo convention: reuse the existing generic
sprite for that slot and let the item's name and boosts carry the elemental theme.

**Verifying a sprite name actually exists — do this before using any name you haven't seen used
elsewhere in the repo.** Sprites are not PNG files or a standard texture atlas; they're packed
into deflate-compressed custom binaries at `assets/sprites/sprites_{0,1,2}.bin` (format defined
in `helper/SpriteBinIO.java`, loaded by `helper/SpriteLoader.java`). Plain `grep`/`strings`
**will not find names in these files** because they're compressed — don't rely on that. Two
reliable ways to check:

1. **Reuse a name already referenced elsewhere** — grep `assets/items/*.json` and
   `src/main/java/com/perso/T4C/tools/ArmorSetGenerator.java`'s `PIECES`/icon tables for the
   slot you need; anything already in use there is guaranteed to exist and load correctly.
2. **For a name you haven't seen used**, write a small throwaway Java snippet (don't check it
   in) that calls `SpriteBinIO.readAll(Path.of("assets/sprites"), "sprites", packed -> ...)` and
   filters `packed.name()` case-insensitively for your candidate substring, e.g. `"Ring"` or
   `"Cape"`, to list the real names available for that slot. Delete the snippet afterward.

## Weapons: a real limitation to flag before promising one

`ItemJsonDef.toItemDefinition()` (in `item/json/ItemJsonDef.java`) always passes `dmgFormula =
null` and `atkDelay = "0"` into the underlying `ItemDefinition` — **the JSON schema does not
expose a weapon damage formula at all**, even though legacy weapons have one (e.g.
`item/definition/CedarLongbow1.java` sets `dmgFormula = "1d59+133+5*arrow_dmg/4"`). Combat code
(`helper/CombatMath.java`, `rollDefinitionDamage`) checks: if `dmgFormula` is null/empty/"0", it
falls back to a flat, strength/dexterity-independent 1–4 damage roll. **A WEAPON authored purely
as JSON today will deal weak, generic fallback damage regardless of its stats.**

If asked to add a new weapon with real, scaled damage, don't silently hand-write a legacy Java
`ItemDefinition` class to route around this (that goes against the JSON convention and the
"don't hand-write new Java item classes" rule). Instead, surface the gap to the user and offer
the honest fix: extend `ItemJsonDef` (and its `toItemDefinition()`) to also accept and pass
through `dmgFormula`/`atkDelay`, mirroring how legacy items express them — a small, additive
change consistent with the existing pattern, not a new hand-authored item class. Do this only if
the user wants it; a purely cosmetic/stat-boost weapon (e.g. a dagger that's mostly a
skill-boost item, like `CedarLongbow1`'s archery boost) is fine to add via JSON as-is, since its
`boosts[]` still work normally — only the base weapon-damage number is affected.

## Buy vs. drop, and scaling rarity to power

- `price: 0` → drop-only, not sold anywhere.
- `price` > 0 → shop-buyable. Add the item's `key` string to the relevant NPC's item list in
  `src/main/java/com/perso/T4C/npc/catalog/ShopCatalog.java` (each shop is a `List<String>` of
  item keys, e.g. `KARAHN`, `FALI`, `CHRYSEIDA`).
- To wire a monster drop, add an entry to that monster's JSON under `assets/monsters/`:
  ```json
  "loot": [ { "item": "<key>", "chance": 0.05 } ]
  ```
  `chance` is 0.0–1.0; **lower = rarer**. Loot is rolled via `monster/loot/LootTable.java`.

**Be honest about precedent:** the repo currently has only 11 monster files and a single loot
entry in the wild (`Sewer Rat` → `Torch` at `chance: 0.03`, a common consumable) — there's no
existing example of a rare-gear drop rate to copy. Use judgment: scale `chance` down as the
item's power/requirements go up (a `price: 0`, high-stat-requirement item like the Empyrean sets
should sit well under 0.03), and put it on a monster whose level is plausible for a player who
could actually equip it — don't put an item requiring `endurance: 550` on a level-3 rat.

## Building a themed multi-piece armor set

Follow the pattern in `tools/ArmorSetGenerator.java` rather than hand-authoring 6 files with
ad hoc numbers:

- 6 pieces per set: armor (BODY), boots (FEET), gauntlets (LEFT_HAND+RIGHT_HAND), helmet (HEAD),
  leggings (LEGS), protector (BELT).
- Each piece's AC is that piece's share of a reference armor-class total, scaled by a tier
  multiplier (2x for tier 1, 3x for tier 2 in the existing generator).
- A set's total resist/power/stat budget (e.g. "70 total Air resist across the whole set") is
  split across the 6 pieces proportional to each piece's AC share, using largest-remainder
  rounding so the declared total is hit exactly once every piece is worn — see `splitByWeight()`.
- Elemental flavors (fire/water/air/earth/light/dark) each get resists in all six schools plus a
  concentrated power boost in their own theme; class flavors (warrior/archer) get a raw stat
  (strength/agility) plus a combat/archery skill boost instead.
- For a new set, either add a new tier/flavor to `ArmorSetGenerator.java` and re-run it (keeps
  the numbers internally consistent and the `boostId` counter contiguous), or replicate this
  same proportional-split math by hand for a one-off set if running the generator isn't
  practical.

**Be honest about set bonuses:** there is **no runtime "wear N of 6 pieces → bonus" mechanic**.
`EquipmentBonusRules.java` sums boosts strictly per equipped item; the generator only pre-splits
a themed set's totals across pieces at *authoring* time so the numbers look coherent once fully
worn — nothing in the engine checks "is this player wearing the whole set." If a user asks for
an actual mechanical set bonus (e.g. "+50 AC extra for wearing all 6"), say plainly that this
would require new engine code in `EquipmentBonusRules.java`/`player/Player.java` to detect a
full matching set and add an extra bonus — it is not something a single item JSON, or even six
of them, can express on their own. Don't fake it by inflating individual piece stats and calling
it a set bonus.

## Jewelry (rings, amulets, bracelets, belts, capes)

`NECK` (amulets), `RING1`/`RING2` (rings), `BRACER` (bracelets) items in this codebase are
boost-focused with little or no `armorClass` — `armorClass: 0` is the norm. Unlike a 6-piece
armor set where a stat budget is spread thin across pieces, a jewelry item is the *whole*
budget on one slot, so its individual boosts run noticeably higher than any single armor piece's
share (compare `ring_of_the_archer`'s `+150`/`+250` single-item boosts to the `+9`/`+14`
per-element values on a 1-of-6 armor piece). `BELT` and `CAPE`, despite sounding like jewelry,
are treated as real armor slots in this codebase (see the empyrean "protector" belts, which
carry AC 38.1) — don't zero out their AC by default just because they're accessory-shaped.

## End-to-end checklist for one new item

1. Pick `bodyPart` (and `secondaryBodyPart` if it's a two-slot item).
2. Decide the power tier and set `requirements` to match (compare against the table above; scale
   requirements with power).
3. Set `armorClass` (0 for pure jewelry/weapons; tier-appropriate for armor/belt/cape).
4. Pick `boosts[]`: look up each effect's `statId` in `references/stat-ids.md`, grep the current
   max `boostId` and claim fresh ids above it, use plain-number `expression`s unless you need a
   formula.
5. Pick `appearanceInventory` (and `appearanceEquippedPrimary`/`Secondary` if it's visibly worn)
   by reusing a name already seen in `assets/items/*.json` or `ArmorSetGenerator.java`; if you
   need something new, verify it exists via the `SpriteBinIO` snippet method — never guess a
   name.
6. If it's a **weapon**, stop and flag the `dmgFormula`/`atkDelay` gap above before promising
   real scaled damage.
7. Decide buy vs. drop: set `price` (0 or shop value); if buyable, add the key to a shop list in
   `ShopCatalog.java`; if a drop, add a `loot` entry with a power-appropriate `chance` to a
   suitable monster's JSON in `assets/monsters/`.
8. Write the file to `assets/items/<key>.json` and double-check `key` is unique (grep
   `assets/items/*.json` and, to be safe, `item/definition/*.java` for the same key string).
