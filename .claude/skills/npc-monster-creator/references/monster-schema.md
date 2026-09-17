# Monster JSON schema — full field reference and power calibration

Source of truth: `src/main/java/com/perso/T4C/monster/json/MonsterJsonDef.java` (the
Gson-deserialized authoring shape) and `monster/core/MonsterDef.java` (the expanded
runtime shape it converts into via `toMonsterDef()`). Read both yourself before relying
on this snapshot — it will drift as the engine grows.

## Full field table

| JSON field | Type | Default | Meaning |
|---|---|---|---|
| `name` | string | **required** | Canonical registry key. Must be unique across all Java `definition()` monsters and all `assets/monsters/*.json` files (`MonsterRegistry` throws/log-warns on collisions it can detect, but always grep first). |
| `displayName` | string | `name` | What players see. Convention: an i18n key `${monster.<lowercase_no_spaces>}` resolved via `assets/i18n/lang.json`, not a raw literal — every shipped example uses this. |
| `health` | int | 10 | Max HP. |
| `mana` | int | 0 | Max mana (only relevant if the monster casts spells via `attacks[].spellId`). |
| `xpPerHit` | int | 1 | XP granted to the attacking player *per point of damage landed* (`player.addXp(xpPerHit * effectiveDamage, ...)`), not per swing. Set to 0 for arena/event mobs that only reward XP on death. |
| `xpOnDeath` | int | 1 | Lump XP on the killing blow. |
| `hitDamageMin` / `hitDamageMax` | int | 1 / 2 | Fallback melee damage roll used only when `attacks` is empty. Real monsters almost always define `attacks` instead (see below) — these two fields exist mainly for trivial/legacy critters. |
| `respawnTime` | long (ms) | 30000 | Base respawn delay after death-animation completes. `MonsterManager` also randomizes an actual per-instance respawn window (`RESPAWN_MIN_MILLIS`..`RESPAWN_MAX_MILLIS`) on top of this once spawned — treat this JSON value as a floor/reference, not the exact number. |
| `walkPattern` / `attackPattern` / `deathPattern` | string | `""` | Sprite-sheet animation tokens, e.g. `"Rat#f"` / `"RatA#i"` / `"RatC!j"`. **Leave non-blank to render as a "creature"** reusing an existing animation family (rat, wolf, snake, spider, etc. — grep other JSON files/Java monster classes for the family name to find valid tokens; don't invent new ones without matching sprite assets). Leaving `walkPattern` **blank** switches the monster to "humanoid puppet" rendering driven by the `equipment`/`appearance` fields instead (see below) — used for goblins, arena mobs, and other person-shaped monsters. |
| `soundAttack` / `soundDeath` / `soundHit` | string | `""` | `.wav` filenames from the existing sound set. Reuse an existing creature's sounds (e.g. `"Rat Attack.wav"`) rather than inventing new filenames — new audio assets aren't part of this skill's scope. |
| `gold.min` / `gold.max` | int | 0 / 0 | Gold drop range, rolled uniformly per kill. |
| `loot` | array of `{item, chance}` | `[]` | `item` is an item registry key (must resolve via `item-creator`'s registry — cross-check before shipping). `chance` is 0.0–1.0, **rolled independently per entry** (not a shared loot table budget) — a `chance` of `1.0` always drops. |
| `animateWhileStationary` / `stationaryAnimationPauseSeconds` | bool / float | false / 0.0 | For a `stationary` monster (see spawn override below) that should still play an idle animation loop with pauses, e.g. a caged beast or ambient creature. |
| `stats.{str,end,agi,intel,will,wis,luck}` | int | 10/10/10/10/0/10/0 | Feeds `combatStrength/Endurance/Agility/Intelligence` on the live monster; `combatAttack = agi + level` (see `applyCombatDefinition`). |
| `resists` | int[12] | zeros | **Index order is not what a naive read of the field name order suggests — see "Resist index mapping" below.** Only indices 0–5 are consumed by the engine today; 6–11 exist in the array but nothing reads them — always leave them at `100` (neutral), matching every shipped example. |
| `level` | int | 1 | Combat level, shown next to the monster's name, and gates `tameMaxLevel` comparisons. |
| `dodge` | int | 0 | Base dodge stat. |
| `armorClass.min` / `.max` | int | 0 / 0 | Usually small ordinary ints. **Caveat**: several existing Java-defined monsters (e.g. arena mobs) store an IEEE-754 float bit-pattern here instead (`decodeArmorValue` in `BaseMonster` auto-detects values ≥ 100000 and reinterprets them as float bits) — this is a legacy encoding quirk from data migration; for new content just use plain small integers, you don't need to replicate the float-bit trick. |
| `appearance` | int | 0 | Only meaningful when `walkPattern` is blank (humanoid puppet mode). `10011` = bare male puppet, `10012` = bare female puppet (adds a ponytail part). Otherwise ignored for creature-style monsters — leave at `0`. |
| `equipment.{body,feet,hands,head,legs,weapon,shield,back}` | int | 0 | Item registry IDs dressing the humanoid puppet (only used when `walkPattern` is blank). Each must resolve via `MonsterPuppetDress.find(id)` (backed by the item registry) or that slot renders nothing. |
| `aggro` | int | 0 | **`aggro > 0` makes the monster attack on sight** within `MONSTER_AGGRO_RANGE` (8 tiles); `aggro == 0` makes it passive (`MonsterDef.isDefaultAggressive()`) — it only fights back if attacked. There's no finer-grained numeric aggro-radius scaling read from this value today; treat it as a boolean gate, and a per-spawn-point `@Spawn(aggressive=...)` override (see spawning below) can flip this per instance. |
| `clan` | int | 0 | **Not consumed by combat AI.** Live clan/faction hostility (`MonsterClan`) is derived automatically from the monster's Java class name + `name` string via substring matching in `MonsterClanRelations.resolveClan` (e.g. anything containing "goblin" → `GOBLIN`, "rat"/"bat"/"slime"/"troll"/"worm" → `BEAST`, "skeleton"/"zombie"/"lich" → `UNDEAD`). This JSON field only reaches the admin/content-studio API — pick a `name` whose substrings land it in the clan you intend, don't rely on this integer. |
| `speed` | int | 0 | Present in the schema but not read by `BaseMonster`'s movement (which uses the fixed `MONSTER_SPEED` constant) — safe to leave at 0. |
| `canAttack` | bool | true | Whether the monster can ever perform an attack action. Set false for a pure "prop"/punching-bag monster. |
| `attacks` | array of `{formula, combatAttack, percentage, spellId?, minRange?, maxRange?}` | `[]` | The real damage model, weighted-random-picked by `percentage` among attacks whose range matches the target distance. `formula` is a dice string (`"1d6+2"`) for a physical hit; set `spellId` instead (with `formula` empty) for a spell-like ranged attack, using `minRange`/`maxRange` in tiles. |
| `tameable` / `tameMaxLevel` | bool / int | false / 0 | Lets a player with a taming skill ≥ the monster's `level` (and ≤ `tameMaxLevel`) tame it — see `canBeTamedBy`. |
| `spawnAliases` | string[] | `[]` | Alternate lookup names for `MonsterRegistry.findByName`/`byAlias` — use this when the in-game "spawn type" string (e.g. what an `@Spawn(type=...)` or a quest/script references) differs from the JSON `name`. Also what content-studio's editor API surfaces. |
| `sourceEvents` | map<string,string> | `{}` | Legacy original-game script snippets (`OnDeath`, `OnPopup`, `OnDestroy`, `@spell.*`) carried over for reference/migration. New content generally doesn't need to add these — the current scripting hooks for new behavior live in `npc/script/**`. |

## Power calibration — read real examples, don't guess

Compare a low-level creature against the tiered `arenamobxp*.json` family (which exists
specifically to prove out a scaling curve across a huge level range):

| Monster | level | health | xpOnDeath | str/end/agi/intel/wis | dodge | resists (non-reserved) |
|---|---:|---:|---:|---|---:|---|
| `sewer_rat.json` | 3 | 45 | 34 | 20/18/17/15/15 | 18 | 124,62,93,93,93,5000 |
| `arenamobxp525.json` | 525 | 58014 | 15,489,313 | 540/487/487/645/487 | 2110 | 46,94,46,94,70,5000 |
| `arenamobxp600.json` | 600 | 70607 | 20,391,334 | 615/554/554/735/554 | 2410 | 46,94,46,94,70,5000 |
| `arenamobxp750.json` | 750 | 95794 | 30,195,375 | 765/689/689/915/689 | 3010 | 46,94,46,94,70,5000 |

Takeaways for a new monster targeting level *L*:
- **Health and xpOnDeath scale roughly linearly with level** within the arena family
  (≈110 HP/level, ≈40,000 xpOnDeath/level in the 525–750 band) — don't eyeball it,
  interpolate/extrapolate from the two nearest existing tiers (Java `ARENAMOBXPnn.java`
  classes cover 50–500; JSON `arenamobxp*.json` covers 525–750).
- **Stats scale together**, not independently — str/end/agi/wis move in lockstep and
  `intel` runs slightly ahead of the others in this family. Don't spike one stat far out
  of proportion unless the monster's theme specifically calls for it (e.g. a golem with
  very high `end`/low `agi`).
- **`dodge` scales with level too** (~4/level here) — a high-level monster with a
  low-level `dodge` value will get hit constantly and die too fast.
- `hitDamageMin/Max` matter little once `attacks[]` is populated — calibrate the
  `attacks[].formula` dice and `combatAttack` value instead, again by comparing to a
  same-tier neighbor.
- xpPerHit is 0 for every arena mob (all XP is back-loaded to `xpOnDeath`) but nonzero
  for ordinary world monsters like `sewer_rat` (2) — pick whichever fits how the
  monster is meant to be fought (a grind target vs. a one-shot set-piece).

## Resist index mapping — verified from code, corrects a naive reading

`resists` is a 12-slot int array but the *authoring order does not match array index
order 1:1 for the element names* — it's remapped by `BaseMonster.getElementResistance`.
Traced end to end:

`spell/SpellEffectManager.java` names the six numeric "element" ids it evaluates against:

```java
int fire  = resistance(target, 1);
int earth = resistance(target, 2);
int air   = resistance(target, 3);
int water = resistance(target, 4);
int light = resistance(target, 5);
int dark  = resistance(target, 6);
```

`monster/core/BaseMonster.java#getElementResistance(int element)` maps that numeric id to
an array index:

```java
case 1 -> 3;  // fire  -> resists[3]
case 2 -> 1;  // earth -> resists[1]
case 3 -> 0;  // air   -> resists[0]
case 4 -> 2;  // water -> resists[2]
case 5 -> 5;  // light -> resists[5]
case 6 -> 4;  // dark  -> resists[4]
```

Composed, the **authoritative index order** is:

| Array index | Element | Notes |
|---:|---|---|
| 0 | Air | |
| 1 | Earth | |
| 2 | Water | |
| 3 | Fire | |
| 4 | Dark | |
| 5 | Light | |
| 6–11 | *(unused)* | keep at `100` |

(This corrects the more intuitive-looking `Air, Water, Earth, Fire, Dark, Light` guess —
indices 1 and 2 are **Earth then Water**, not Water then Earth. Verify against the two
files above yourself if the engine changes.)

Scale: `SpellEffectManager` treats a resist value `<= 0` as neutral `100`, and every
shipped baseline uses `100` for "no opinion." Above 100 = more resistant (takes
proportionally less elemental damage); below 100 = more vulnerable; very high values
(`5000`+, as seen on `sewer_rat`'s Light resist and every arena mob's Light resist) are
an effectively-immune ceiling. Pick asymmetric values that match the monster's theme:
an ice creature should sit high on Water/Air and low on Fire; a fire elemental high on
Fire and low on Water; an undead high on Dark and often low on Light; a burrowing/earth
creature high on Earth and plausibly low on Air. Don't ship a flat `100` across the
board for a themed monster — that's the "no theme" default, not a considered choice.

## `MonsterClan` substring rules (`monster/core/MonsterClanRelations.java`)

Clan is inferred from `(javaClassName + " " + name)`, lowercased, by substring match —
pick your monster's `name` (and Java class name, if you write one) with this in mind:

`GOBLIN` ← "goblin" · `HORSE` ← "horse" · `ANIMAL` ← "cow","dromadary","pegasus","pig","unicorn"
· `UNDEAD` ← "skeleton","zombie","mummy","lich" · `DEMON` ← "demon","atrocity"
· `DRACONIAN` ← "draconian","dragon" · `CENTAUR` ← "centaur" · `SKAVEN` ← "skaven"
· `KOBOLD` ← "kobold" · `KRAANIAN` ← "kraanian" · `ORC` ← "orc"
· `INSECT` ← "wasp","scorpion","spider","tarantula"
· `HUMAN` ← "cleric","guard","mage","paysan","priest","thief","warrior","wizard","tank"
· `BEAST` ← "bat","beholder","minotaur","rat","slime","snake","taunting","tree ent","troll","worm"
· else `NEUTRAL`.

Only one hostile relation is wired by default (`GOBLIN` vs `HORSE`); other clans don't
auto-fight each other unless someone edits `MonsterClanRelations`. A monster's clan
membership is mainly useful as quest-fiction flavor (see the quest-creator skill's
world-reference notes) and for whether it aggros other hostile-clan monsters, not player
combat balance.
