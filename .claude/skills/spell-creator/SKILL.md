---
name: spell-creator
description: Use this whenever the user wants to design or add a new offensive, defensive, or utility spell (or "ability", "skill", "buff", "nuke", "heal", "debuff", "DoT", "AOE") to the T4C game, even if they just describe an effect/idea without saying "spell" — e.g. "I want a spell that freezes enemies", "add a new fire nuke for high-level mages", "make a priest heal-over-time", "give druids a thorns buff". Also use it to rebalance, tune the numbers on, or explain the fields of an existing spell definition.
---

# T4C Spell Creator

Adds a new spell to this LibGDX recreation of *The 4th Coming*. **The authoring path is plain Java, not
JSON** — despite items having moved to a JSON/exporter pipeline (`content/ItemJavaExporter.java`), spells have
not: `content/SpellJavaExporter.java` only *exports* an in-memory `List<SpellData>` back out to generated
Java source (`spell/generated/`, a one-way debugging/tooling dump). The real, hand-authored, loaded-at-runtime
spell definitions are the classes under `src/main/java/com/perso/T4C/spell/definition/*.java`. **Write your new
spell there, as a plain Java class — do not touch `spell/generated/` or look for a JSON file.**

## How a spell plugs into the runtime (read this once)

`SpellDefinitions.all()` (`spell/definition/SpellDefinitions.java`) scans the `spell.definition` package at
class-load time via reflection for **any class with a public static `definition()` method that returns a
`SpellData`** — there is no manual registry to edit. Drop in a new class with that shape and it is
automatically picked up by `SpellRegistry`, sorted by `spellId`. This means:
- The **class name is your only choice that matters for discovery**; give it a clear `PascalCase` name matching the spell (e.g. `FrostNova.java`).
- A `private FrostNova() {}` constructor plus a `public static SpellData definition()` factory is the whole shape — copy any existing file.
- `SpellRegistry.isPlayerCastable()` hides a spell from players' spellbooks unless: its name starts with `${spell.` , does **not** start with `${spell.item_`, `${spell.mob_`, `${spell.test_`, `${spell.npc_`, does **not** end in `_effect}`, and it has at least one `T4cEffect` (or is literally `tame_beast`). Follow the naming convention below or your spell silently won't appear.

### Naming convention (prefix = category, by file and by `${spell...}` key)
| Prefix | Who casts it | Shows in player spellbook? |
|---|---|---|
| *(none)*, e.g. `IceBolt`, `Bless` | Player, learned via a trainer | Yes |
| `Item*`, e.g. `ItemPotionOfHealing` | Triggered by using/wearing an item | No |
| `Mob*`, e.g. `MobPoisonSpell` | Cast by monster AI | No |
| `Npc*` | Cast by a scripted NPC | No |
| `Test*` | Dev/QA only | No |
| `*Effect` suffix | A sub-effect fired by another spell's type-9 `OnTimer` hook (e.g. `Entangle` → `EntangleEffect`) | No |

**For a normal player offensive/defensive/utility spell, use no prefix.** Only use `Item*`/`Mob*`/`Npc*` if the user explicitly asked for an item power, a monster attack, or an NPC script instead of a learnable player spell.

## The `SpellData` schema, annotated

`SpellData` (`spell/SpellData.java`) is an immutable, all-`final`-field record-like class (Lombok `@Getter`).
Use the full 33-argument constructor. Below is `IceBolt.java` (a real, live spell) annotated field-by-field:

```java
package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class IceBolt {
  private IceBolt() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.ice_bolt}",              // name: i18n key, MUST exist in assets/i18n/lang.json
        "${spell.description.ice_bolt}",  // description: i18n key, ditto
        "6",                               // manaCost: a DiceFormula string (flat "6" or an expression)
        0,                                 // radius: AOE radius in tiles, 0 = single target / self
        68,                                // minInt: intelligence required to learn/cast
        26,                                // minWis: wisdom required to learn/cast
        25,                                // minLevel: character level required; also the pivot the
                                            //   exhaustion formulas below scale off of
        true,                              // isAttack: true = offensive (hostile), false = defensive/utility
        true,                              // lineOfSight: true = needs clear LOS to target
        "64kSpellIconWaterAttackSingle",   // iconId: sprite name for the spellbook icon (see Graphics)
        "IceShard",                        // projectileSpell: sprite name for the flying bolt (see Graphics)
        "IceCloud-",                       // impactSpell: sprite name for the impact burst (see Graphics)
        0,                                 // minDamage: legacy flat-range fallback, unused when a
        0,                                 // maxDamage: T4cEffect type-1/10 formula is present (it is here)
        "Small Projectile.wav",            // sound: cast/launch sound file
        "Ice Cloud.wav",                   // soundImpact: impact sound file
        0,                                 // cooldownSeconds: hard per-spell cooldown (most attack spells: 0;
                                            //   gated by exhaustion instead — see Cast Time)
        "0",                               // duration: DiceFormula millis; buff/DOT lifetime, "0" = instant
        "0",                               // frequency: DiceFormula millis; tick interval for OnTimer hooks
        30418,                             // price: reference gold value (NOT what LearnScreen charges — see Purchasing)
        null,                              // buff: legacy/unused field — always pass null (buffs use T4cEffect type 2)
        10026,                             // spellId: unique numeric id (see references/spell-examples.md for ranges)
        4,                                 // element: 1=fire 2=earth 3=air 4=water 5=light 6=dark
        11,                                // targetType: which TargetKind(s) this can be cast at (see Targeting)
        1,                                 // attackType: SpellData.ATTACK_PHYSICAL(1) or ATTACK_MENTAL(2) — shown
                                            //   verbatim in-game as the "Physical"/"Mental" spellbook label
        "100",                             // successRate: DiceFormula 0-100, "100" = never fizzles
        "1000+if((750-(self.level-25)*20)>=0?(750-(self.level-25)*20):0)", // mentalExhaustion (ms formula)
        "750+if((750-(self.level-25)*20)>=0?(750-(self.level-25)*20):0)",  // physicalExhaustion (ms formula)
        "750+if((750-(self.level-25)*20)>=0?(750-(self.level-25)*20):0)",  // attackExhaustion (ms formula)
        30023,                             // visualEffect: legacy numeric VFX id (drives palette recolor, see Graphics)
        0,                                 // visualEffectTarget: numeric VFX id for a second/target-side effect, 0 = none
        true,                              // pvp: true = allowed to target other players
        List.of(                           // t4cEffects: the actual damage/heal/buff/DOT logic, see references/effect-types.md
            new SpellData.T4cEffect(
                1,                          // effectType 1 = instant damage/heal
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-((1d39+31+self.int/15)*self.water/target.r_water)"), // negative = damage
                    new SpellData.T4cEffect.EffectParam(2, null),  // no separate AOE-falloff formula (single target)
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
```

Reference `SpellDefinition.java` (the interface `SpellData` implicitly satisfies via its getters) lists every
field name if you need the accessor names; you never implement it directly, just call the `SpellData`
constructor as above.

## Step 1 — Classify the spell

**Offensive vs defensive vs utility** is driven by `isAttack` + which `T4cEffect` types you use, not a single enum:
- **Offensive**: `isAttack = true`, `targetType` restricted to hostile (see Targeting), uses effect type `1`
  (negative formula) or `10` (drain) for damage, or type `14`/`9` for debuffs/DOTs. Real examples: `IceBolt`, `LightningBolt`, `Fireball`, `Blizzard`.
- **Defensive**: `isAttack = false`, targets self or friendly, uses effect type `2` with attributes like `AC`,
  `max hp`, `r_fire`/`r_water`/… (resistance), or `dodge`. Real examples: `StoneSkin` (AC buff), `DivineVeil` (6-way resist+AC shield), `IceShield`/`FireShield`/`ElectricShield` (retaliation shields — read one before writing a shield spell, they use a different pattern than plain buffs).
- **Utility**: `isAttack = false`, no damage/AC effect — teleport (type `7`/`11`), invisibility/detection
  (type `15`/`16`/`17`), dispel (type `13`), summon (type `6`), or pure attribute utility (`mana`, `exp`, `unlimited`). Real examples: `WordOfRecall`, `Invisibility`, `DetectHidden`, `Dispel`, `TrueSight`.

**Physical vs mental** (`attackType`) *is* explicitly encoded and surfaced to the player: `SpellBook.java` prints
`"Physical"` or `"Mental"` straight from `SpellData.ATTACK_PHYSICAL`/`ATTACK_MENTAL` in the spell's own tooltip.
However there is **no clean rule deriving it from element/offense/defense** — real examples contradict any single
formula (e.g. `IceBolt` and `Fireball` are PHYSICAL, `LightningBolt` and `Blizzard` are MENTAL, despite all four
being elemental attack bolts/nukes; `Bless` is PHYSICAL while `DivineVeil`, also a buff, is MENTAL). Treat it as a
per-spell flavor choice inherited from the legacy game data, not something to compute:
1. Find the 1-2 closest existing spells by role+element (see `references/spell-examples.md`) and copy their `attackType`.
2. If nothing is close, default: single-target elemental bolts → PHYSICAL(1); wide-effect nukes, group heals, and pure buffs/utility → MENTAL(2). This matches the majority of read examples but is a convention, not a rule — say so if asked and let the user override it.

## Step 2 — Set level / int / wis / mana requirements

**Level 150 and above: don't hand-pick numbers — use `spell/HighTierSpellCurve.java`.** The
level cap is 400 (`GameConstants.MAX_PLAYER_LEVEL`) and every school (fire, earth, air, water,
light, dark) has exactly one attack spell at each of levels 150/200/250/300/350/400, all built by
`HighTierSpellCurve.attack(key, spellId, element, tier, Shape.BOLT|AREA)` so requirements, mana,
price and damage stay identical across schools (`HighTierSpellLadderTest` enforces this). Its
Javadoc explains the stat-budget math (5 stat points per level; tier `L` needs `2.5L` main stat +
`0.6L` other stat). A high-tier support spell (heal/ward) should still take its Int/Wis gate from
`HighTierSpellCurve.primaryRequirement`/`secondaryRequirement` — see `DawnwellRenewal`,
`SanctumWard`. Never give a player spell a `minLevel` above the level cap.

Use `references/spell-examples.md` (a table of 10 real spells from level 15 to 100 with their exact
`minLevel`/`minInt`/`minWis`/`manaCost`/element/formula) to interpolate. Rules of thumb pulled from that data:
- `minInt` tracks spells that scale off `self.int` in their formula (mage-flavored); `minWis` tracks `self.wis`-scaling spells (priest-flavored). Set whichever stat the damage/heal formula actually uses noticeably higher than the other — e.g. `IceBolt` (int-scaling) has `minInt=68, minWis=26`; `Bless` (wis-scaling) has `minInt=19, minWis=102`.
- `minLevel` should be plausible for the power level: e.g. 15 for a small single-target bolt, ~60-75 for an AOE nuke doing 2x a bolt's damage, 100 for a top-tier single-target nuke (`Meteor`).
- `manaCost` stays surprisingly low (4-45 typical) even for powerful spells — it is not the primary balance lever here, `minLevel`/stat gates and cast time are. Only go far above that (e.g. `DivineVeil`'s 1000) if you deliberately want the spell to be very mana-expensive to spam.
- **Never** reuse a low-level spell's dice formula scale (e.g. `1d26`) for a level-40+ nuke — scale the dice count/faces and flat bonus up with level the way `LightningBolt`(15, `1d26+12`) → `IceBolt`(25, `1d39+31`) → `Fireball`/`Blizzard`(27/74, `1d52+7x`) → `Meteor`(100, `1d157+125`) do.

## Step 3 — Damage/heal formula (`DiceFormula` syntax)

All numeric-ish `SpellData` string fields (`manaCost`, `successRate`, `duration`, `frequency`, the three
exhaustion fields, and every `T4cEffect` `EffectParam` expression) are parsed by the shared
`helper/DiceFormula.java` engine — **the same engine used for monster melee-attack dice** (e.g. `r141MOBGAUZECORPSE1.java` and friends), so a formula written for a spell and one written for a monster attack look identical.

Syntax, straight from the parser:
- Dice: `NdM` → roll `N` dice of `M` faces, e.g. `1d52`, `2d6`.
- Arithmetic: `+ - * /` with normal precedence, and parentheses.
- Ternary: `if(COND?TRUE:FALSE)` where `COND` uses `< > <= >= == !=`, e.g. `if((750-(self.level-25)*20)>=0?(750-(self.level-25)*20):0)`.
- Caster variables: `self.level`, `self.int`, `self.wis`, `self.str`, `self.end`, `self.agi`, `self.wil`,
  `self.luck`, `self.fire`/`self.earth`/`self.air`/`self.water`/`self.light`/`self.dark` (the caster's elemental *power*, 100 = neutral baseline).
- Target variables (only valid inside a `T4cEffect` damage/heal/hook formula, not in `manaCost`/`successRate`):
  `target.r_fire`/`r_earth`/`r_air`/`r_water`/`r_light`/`r_dark` (target's elemental *resistance*, 100 = neutral;
  dividing by this is how resistance reduces damage — see `IceBolt`'s `.../target.r_water`).
- AOE distance: a bare `r` token in a **param-2** (ranged) formula is substituted with the target's tile
  distance from the effect center at evaluation time (see `Fireball`'s and `Blizzard`'s param-2 falloff formulas, `*(20-r)/20`).
- Sign convention for effect type 1/10: **negative result = damage, positive = heal** (compare `IceBolt`'s
  `-((1d39+...)...)` to `MassHealing`'s `(1d7+81+self.wis/9)*self.light/100)` with no leading `-`).

Full effect-type parameter table (what each `T4cEffect` code + its params mean): `references/effect-types.md`.

## Step 4 — Cast time / cooldown / exhaustion

There is **no single "cast time" field.** `SpellCastingService.begin()` applies all three exhaustion formulas
via `caster.applyExhaustion(mental, physical, attack)`, and `evaluateCastDurationMillis()` reports the
**effective cast time as `max(mentalExhaustion, physicalExhaustion, attackExhaustion)`** (all evaluated with the
caster's live stats/level). `cooldownSeconds` is a separate, additional hard lockout on top of that (most attack
spells leave it `0` and rely purely on exhaustion; a few slow/powerful spells like `DivineVeil` set it, e.g. `5`).

The idiomatic formula pattern, used by essentially every existing spell, is a level-scaled decay down to a floor:
```
mentalExhaustion:   "1000+if((BASE-(self.level-MIN_LEVEL)*20)>=0?(BASE-(self.level-MIN_LEVEL)*20):0)"
physicalExhaustion: "750+if((BASE-(self.level-MIN_LEVEL)*20)>=0?(BASE-(self.level-MIN_LEVEL)*20):0)"
attackExhaustion:   "750+if((BASE-(self.level-MIN_LEVEL)*20)>=0?(BASE-(self.level-MIN_LEVEL)*20):0)"
```
`MIN_LEVEL` matches the spell's own `minLevel`, and `BASE` grows with spell power (750ms at level 15, up to
~1240-1500ms for the biggest AOE/level-100 nukes — sometimes doubled outright, e.g. `Blizzard`/`MassHealing`
wrap the whole expression in `2*(...)`, roughly doubling cast time for their heavier AOE payoff). Copy the
nearest comparable spell's `BASE` from `references/spell-examples.md` and adjust `MIN_LEVEL`.

## Step 5 — Targeting (`targetType`)

`SpellCastingService.targetAccepted(int type, TargetKind kind)` is the actual gate; there is no named enum, just
a hard-coded set membership per `TargetKind`. Don't invent a number — copy the `targetType` from the nearest
real spell in `references/spell-examples.md`. Confirmed real values: `3` = self-or-friendly buff (`Bless`), `5` =
self-only shield/buff (`DivineVeil`, also special-cased in `SpellEffectManager` as "target IS the caster" for
hook formulas), `9` = hostile-only DOT/debuff (`Entangle`), `11` = hostile-only single-target attack (`IceBolt`,
`LightningBolt`), `15` = self-centered AOE that also affects friendlies in `radius` (`MassHealing`), `19` =
hostile-or-position AOE attack (`Blizzard`). If you must pick a genuinely new combination, read the `switch` in
`SpellCastingService.targetAccepted` and match against `TargetKind.SELF/HOSTILE_UNIT/FRIENDLY_UNIT/POSITION`.

`isAttack`/`lineOfSight`/`pvp` interact with targeting too: `pvp=false` blocks the spell from being cast at other
players even if `targetType` would otherwise allow it (`PVP_FORBIDDEN`); `lineOfSight=true` requires clear LOS
except when targeting self.

## Step 6 — Make it purchasable/learnable

A `SpellData` that passes `isPlayerCastable` shows up wherever spells are listed, but a player can only **learn**
it through an NPC trainer wired in `src/main/java/com/perso/T4C/npc/catalog/TrainingCatalog.java`. Add or edit
an entry in that file's `M` map:

```java
Map.entry(
    "SomeTrainerNpcId",
    new E(true, List.of(
        o("ice_bolt", 13, 25625, true),   // existing offer, for reference
        o("your_new_spell", 30, 45000, true))))  // add your spell here
```

`o(skillId, minLevel, goldCost, teaching)`:
- `skillId`: the snake_case suffix of your `${spell.your_new_spell}` i18n key (no `spell.` prefix).
- `minLevel`: level gate to **learn** it from this trainer. This is set **independently** of `SpellData.minLevel`
  in the real data (e.g. `ice_bolt`'s catalog entry gates at level 13 while its own `SpellData.minLevel` is 25) —
  the trainer's gate is typically the *lower*, "can start training toward it" bar, while the spell's own
  `minLevel`/stat requirements are what actually let you *cast* it at full power. Keep them in the same
  ballpark and let `SpellData.minLevel` be the higher/authoritative one.
- `goldCost`: what the player actually pays; independently tuned from `SpellData.price` (see calibration table) — set it in the same order of magnitude as comparable spells, don't try to match `price` exactly.
- `teaching`: `true` for a one-time spell purchase (shows under the NPC's TEACH/LEARN keyword). `false` is for repeatable numeric skill training (e.g. combat skill %), not used for spells — always pass `true` here.

Pick (or ask the user for) a trainer NPC thematically appropriate to the spell's element/school; if none fits, it's fine to add a new `Map.entry` for an existing NPC id already used elsewhere in the world, but adding a *brand-new* NPC is out of scope for this skill (that's world/NPC placement work).

**Also add the i18n strings** — every spell name/description is an i18n key resolved from `assets/i18n/lang.json`
(flat, alphabetically-ish sorted JSON). Add both keys near their alphabetical neighbors:
```json
"spell.your_new_spell": "Your New Spell",
"spell.description.your_new_spell": "One sentence describing what it does.",
```
Without these, the spell's name/tooltip renders as the raw `${spell.your_new_spell}` placeholder.

## Step 7 — Graphics / VFX

Spell visuals are sprite lookups by name, not per-spell art files you author from scratch. `render/SpellRenderer.java`
resolves `projectileSpell`/`impactSpell` strings through `SpriteLoader.getRegionFromSpriteName`, falling back
through `SpellVisualResolver`'s element-keyed defaults and `SpellProjectilePalette`'s recolor-by-`visualEffect`-id
tables if left blank. The actual sprite frames trace back to the legacy `assets/origin/SpellEffects.dll` art
(pre-extracted into the game's sprite atlases) plus a `spellmask` shader (`assets/shaders/spellmask.vert/.frag`)
used for palette recoloring (e.g. the four `64kSpellEnergyBall{Blue,Yellow,Black,Purple}-` recolors driven by `visualEffect`).

**Default to reusing an existing effect — this needs no new art:**
1. Grep `src/main/java/com/perso/T4C/spell/definition/*.java` for `projectileSpell`/`impactSpell` strings matching your spell's element/flavor (e.g. `grep -rn '"IceShard"\|"IceCloud-"' spell/definition/`) and reuse the same string. `SpellVisualResolver`'s element defaults (`references/effect-types.md` doesn't list these; see the `switch` in `SpellVisualResolver.java` directly: 1=fire→explosion look, 2=earth→rocky, 3=air→electric, 4=water→ice, 6=dark→curse-black) are also a safe fallback if you just leave `projectileSpell`/`impactSpell` as reasonable existing names and let the resolver do the rest.
2. Reuse an existing `iconId` too (`64kSpellIcon<Element><Category>`, e.g. `64kSpellIconWaterAttackSingle`, `64kSpellIconFireAttackArea`, `64kSpellIconEarthDefense`, `64kSpellIconLightHealArea`) — grep existing definitions for the closest element+category match.
3. Reuse an existing `visualEffect`/`visualEffectTarget` numeric id from a spell with the palette you want (blue/yellow/black/purple energy ball recolor — see `SpellProjectilePalette.java`'s id sets), or `0` for none.

**Only reaches into new-art territory if:** the user explicitly wants a visual that doesn't exist yet (a genuinely
new projectile shape, a new impact burst, a new icon) — describe the needed asset(s) precisely (silhouette,
color/element, single-frame icon vs multi-frame animated projectile/impact, and which existing sprite it should
slot in alongside) and hand that off to the `graphic-designer` skill (`.claude/skills/graphic-designer/SKILL.md`
if present in this repo) rather than inventing pixels yourself here. State clearly in your spell writeup whether
the spell is (a) a visible physical bolt/AOE that needs a projectile+impact pair, or (b) a "mental"/status effect
that can ship with icon-only or minimal/no distinct VFX (many buffs/utility spells, e.g. `StoneSkin`, `DivineVeil`, reuse a generic impact or `null` for `impactSpell`).

## End-to-end checklist

1. **Classify**: offensive / defensive / utility; pick `attackType` (PHYSICAL/MENTAL) by nearest comparable spell.
2. **Pick numbers**: `minLevel`/`minInt`/`minWis`/`manaCost`/`element`/`targetType` by interpolating `references/spell-examples.md`; pick an unused `spellId` (prefer a `99xxx` id for new content).
3. **Write the formula(s)**: damage/heal/buff `T4cEffect` list using `DiceFormula` syntax, scaled to the spell's level (see `references/effect-types.md` for effect-type codes).
4. **Write the exhaustion formulas** (mental/physical/attack) using the level-scaled-decay pattern, and set `cooldownSeconds` (usually `0`).
5. **Pick graphics**: reuse existing `iconId`/`projectileSpell`/`impactSpell`/`visualEffect` from a similar spell, or describe new-art needs for `graphic-designer`.
6. **Create the class** `spell/definition/YourSpellName.java` following the `IceBolt` template above.
7. **Add i18n keys** `spell.your_spell_name` and `spell.description.your_spell_name` to `assets/i18n/lang.json`.
8. **Wire a trainer**: add an `o("your_spell_name", level, gold, true)` entry to an appropriate NPC in `TrainingCatalog.java`.
9. Sanity-check: no duplicate `spellId`; class compiles; name/description keys resolve; formula uses valid `DiceFormula` syntax (no unsupported operators — the parser only understands `+ - * / d if()` and the listed variables).
