# Calibration table: real spells by level

All read directly from `src/main/java/com/perso/T4C/spell/definition/*.java`. Use these to sanity-check
a new spell's numbers — pick the two or three nearest by level/role and interpolate, don't invent numbers
from scratch. "Catalog lvl/gold" is the separate `TrainingCatalog.java` purchase gate (see SKILL.md) —
note it does **not** always match the spell's own `minLevel`/`price` fields.

| Spell | class file | minLevel | minInt | minWis | mana | element | attackType | radius | catalog lvl / gold | damage/heal formula (center) |
|---|---|---|---|---|---|---|---|---|---|---|
| Lightning Bolt | `LightningBolt.java` | 15 | 30 | 30 | 4 | 3 (air) | MENTAL | 0 | 10 / 10425 | `-((1d26+12+self.int/36+self.wis/36)*self.air/target.r_air)` |
| Ice Bolt | `IceBolt.java` | 25 | 68 | 26 | 6 | 4 (water) | PHYSICAL | 0 | 13 / 25625 | `-((1d39+31+self.int/15)*self.water/target.r_water)` |
| Fireball | `Fireball.java` | 27 | 94 | 16 | 7 | 1 (fire) | PHYSICAL | 4 (AOE) | 14 / 29457 | `-((1d52+37+self.int/14)*self.fire/target.r_fire)` (+ falloff variant for param 2) |
| Entangle | `Entangle.java` | 31 | 34 | 52 | 10 | 2 (earth) | PHYSICAL | 0 | 15 / 37913 | root/DOT via `T4cEffect` type 9 (`OnTimer`), tick dmg `50*self.earth/target.r_earth` |
| Stone Skin | `StoneSkin.java` | 37 | 37 | 59 | 45 | 2 (earth) | PHYSICAL | 0 (self buff) | 17 / 52577 | AC buff `10+self.int/25+self.wis/13` |
| Bless | `Bless.java` | 59 (data) | 19 | 102 | 140 | 5 (light) | PHYSICAL | 0 | 37 / 126673 | max-hp `1d(self.wis/4)+self.wis`; also `attack` and `skill 35` buffs at `self.wis/2` |
| Mass Healing | `MassHealing.java` | 63 | 19 | 108 | 15 | 5 (light) | MENTAL | 5 (AOE) | 39 / 143577 | `(1d7+81+self.wis/9)*self.light/100` |
| Blizzard | `Blizzard.java` | 74 | 170 | 48 | 17 | 4 (water) | MENTAL | 5 (AOE) | 29 / 195508 | `-((1d52+72+self.int/8)*self.water/target.r_water)` (+ falloff variant) |
| Divine Veil | `DivineVeil.java` | 40 | 20 | 60 | 1000 | 5 (light) | MENTAL | 0 (self) | not in catalog sample | 6x resist/AC buffs at 100%; `spellId 99903` (see "new content" ID range below) |
| Meteor | `Meteor.java` | 100 | 309 | 18 | 23 | 1 (fire) | PHYSICAL | 2 | not in catalog sample | `-((1d157+125+self.int/7)*self.fire/target.r_fire)` |

## Reading the trend

- **Mana cost** stays small (4-45) even for big spells; it is not the main gate — `minInt`/`minWis`/`minLevel`
  and the exhaustion-derived cast time are. Very heavy defensive/utility spells (`Divine Veil`, 1000 mana) are the exception,
  used deliberately to make the spell rare/expensive to spam.
- **Damage dice** scale roughly with level: `1d26` at 15, `1d39` at 25, `1d52` at 27/74, `1d157` at 100. The flat
  bonus and `self.int`/`self.wis` divisor also grow so a level-40 nuke should land well above `1d52` in scale, not reuse a `1d26` from a level-15 spell.
- **Stat gates** (`minInt`/`minWis`) roughly track which "caster stat" the spell should scale from in its formula
  (mage/int-leaning spells push `minInt` high and use `self.int` in the formula; priest/wis-leaning spells do the opposite).
- **Price field** (`SpellData.getPrice()`) is set on every definition but nothing in the runtime reads it for
  gating — `TrainingCatalog`'s own `goldCost` argument is what a player actually pays, and it is tuned independently
  (see e.g. `ice_bolt`: data price 30418 vs catalog gold 25625). Keep the `SpellData` price roughly in the same
  order of magnitude as the catalog price you choose, but do not expect them to match exactly.

## spellId ranges (avoid collisions)

Grep the exact literal before picking one: `grep -rn "10999," src/main/java/com/perso/T4C/spell/definition/`.
Observed ranges in this codebase:
- `~10000-10800`: legacy player-learnable spells (e.g. Fireball 10018, Ice Bolt 10026, Lightning Bolt 10028, Bless 10267).
- `~10190s+`: `Item*` triggered effects (e.g. `ItemPotionOfHealing` 10191).
- `~10300+`: `Mob*` monster-only spells (e.g. `MobPoisonSpell` 10344).
- `99xxx`: used for new, non-legacy content added to this codebase (`DivineVeil` uses `99903`) to guarantee no
  collision with the original game's ID space. **Prefer a `99xxx` id for any brand-new spell** unless you are
  deliberately recreating a specific legacy spell ID.
