# T4cEffect effect-type codes

Every `SpellData.T4cEffect` has an `effectType` int and a list of `EffectParam(paramId, expression)`
pairs (1-indexed `paramId`, `expression` is a `DiceFormula` string or a literal, `null` if unused).
These codes are read by `SpellEffectManager` (`src/main/java/com/perso/T4C/spell/SpellEffectManager.java`).
There is no enum — the numbers below are reverse-engineered from that file; when in doubt, re-read it.

| type | meaning | param 1 | param 2 | param 3 | param 4 |
|---|---|---|---|---|---|
| 1 | Damage or heal (instant). Negative formula = damage, positive = heal. | center-formula (used when no explicit range) | ranged-formula (used for AOE falloff; formula may contain a bare `r` = distance from center in tiles) | unused by damage code path but conventionally "100" | — |
| 2 | Attribute buff/debuff. Also drives player mana/attribute deltas outside combat. | unused (`null`) | attribute name, see `normalizeBoostAttribute`: `str/agi(dex)/end/int/wis`, `AC`/`armor class`, `max hp`, `attack`, `dodge`, `skill 35`(archery)/`skill 9`/`skill 29`, `air/fire/water/earth/light/dark` (elemental power), `mana`, `radiance`, `r_air/r_fire/r_water/r_earth/r_light/r_dark` (resistance), `exp`, `unlimited` | amount formula (evaluated with caster context) | — |
| 3 | Set a legacy quest/view flag on the caster. | flag id | value | — | — |
| 6 | Summon. | summon type string | definition key | — | — |
| 7 | Teleport caster to fixed coordinates. | tile X | tile Y | world Z | — |
| 9 | Timed hook: periodically (or once) casts another spell (by id) on the target. Used for DOTs/HOTs and delayed procs (e.g. `Entangle`, `MobPoisonSpell`). | linked `spellId` to fire | literal string `"OnTimer"` (required, case-insensitive) | chance percent (0-100) per tick | initial delay in ms before first tick (0 = default 1000ms cadence unless `duration`/`frequency` say otherwise) |
| 10 | Drain life (same evaluation as type 1, but also credits the caster with the health drained). | center-formula | ranged-formula | — | — |
| 11 | Teleport caster to their respawn/bind point. | — | — | — | — |
| 12 | Vaporize (instant-kill / remove target). | — | — | — | — |
| 13 | Dispel a buff by spell id, with a chance. | target `spellId` to dispel | chance percent | — | — |
| 14 | Apply extra exhaustion (attack/mental/move lockout) to the target, with a chance. | attack-exhaustion ms formula | mental-exhaustion ms formula | move-exhaustion ms formula | chance percent |
| 15 | Chance to turn the caster invisible for `duration`. | chance percent | — | — | — |
| 16 | Chance to grant detect-invisible for `duration`. | chance percent | — | — | — |
| 17 | Chance to grant detect-hidden for `duration`. | chance percent | — | — | — |

Notes:
- A spell can carry **multiple** `T4cEffect` entries (e.g. `Bless` stacks three type-2 buffs: max-hp, attack, and skill 35/archery; `DivineVeil` stacks six type-2 resistance/AC buffs).
- `SpellRegistry.isPlayerCastable` requires at least one `T4cEffect` (or the special-cased `tame_beast`) for a spell to show up as player-learnable at all — a definition with an empty effect list is invisible to players even if it has a name/icon.
