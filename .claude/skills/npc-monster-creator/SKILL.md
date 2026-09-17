---
name: npc-monster-creator
description: Authors new monsters (JSON stat blocks, loot, spawn placement) and NPCs (dialogue, shop/training role, quest-giver hooks, companions) for this T4C LibGDX recreation. Use this whenever the user wants to add a new monster, mob, boss, creature, or NPC (shopkeeper, quest-giver, trainer, companion, lore character) to the game, even if they just describe a creature or character concept in plain language rather than naming files.
---

# NPC & Monster Creator

Monsters and NPCs are both "things that stand in the world," but they're authored
through genuinely different mechanisms in this codebase. Read this whole file before
starting — the spawn-placement section in particular corrects an intuitive-but-wrong
assumption about where spawn locations live.

## Orientation: where things live

| Concern | Path |
|---|---|
| Monster JSON definitions (auto-loaded) | `assets/monsters/*.json` |
| Monster JSON → runtime shape | `src/main/java/com/perso/T4C/monster/json/MonsterJsonDef.java`, `MonsterJsonLoader.java` |
| Monster Java classes (per-monster, own `definition()`) | `src/main/java/com/perso/T4C/monster/*.java` |
| Monster runtime/AI | `src/main/java/com/perso/T4C/monster/core/BaseMonster.java`, `DataMonster.java`, `MonsterRegistry.java` |
| **Live spawn wiring** (see warning below) | `@Spawn` annotation on a monster/NPC class, scanned by `src/main/java/com/perso/T4C/spawn/SpawnRegistry.java`, consumed by `monster/core/MonsterManager.java` |
| NPC Java classes | `src/main/java/com/perso/T4C/npc/*.java` |
| NPC declarative spec/behavior framework | `src/main/java/com/perso/T4C/npc/core/{NpcSpec,ScriptedNpc,NpcFactoryRegistry}.java`, `npc/behavior/*.java` |
| Shop/training stock | `src/main/java/com/perso/T4C/npc/catalog/{ShopCatalog,TrainingCatalog}.java` |
| Companion NPCs | `src/main/java/com/perso/T4C/npc/companion/definition/*.java` |
| Quest tie-in | `src/main/java/com/perso/T4C/quest/**` — see the `quest-creator` skill at `.claude/skills/quest-creator/SKILL.md` |
| Item tie-in (loot/shop stock) | see the `item-creator` skill at `.claude/skills/item-creator/SKILL.md` |
| Full annotated monster JSON schema + power calibration + resist mapping | `.claude/skills/npc-monster-creator/references/monster-schema.md` |

---

## 1. Monster authoring

There are two ways to author a monster, and the choice matters for how it gets placed
in the world (Section 2):

**A. Pure JSON** (`assets/monsters/<name>.json`) — `MonsterJsonLoader.loadAndRegister()`
scans this directory at startup, converts each file via `MonsterJsonDef.toMonsterDef()`,
and merges it into `MonsterRegistry`. No Java code needed. The monster gets generic
combat/AI behavior (`DataMonster`/`NamedEventMonster`). This is the simpler path and
should be your default for an ordinary new monster.

**B. Java class** (`src/main/java/com/perso/T4C/monster/<Name>.java`, `extends
DataMonster`, package `com.perso.T4C.monster`) — a static `definition()` method
returning a `MonsterDef` built with the full-fat constructor (see `r151MortalWombat.java`
for a worked example), a `(MonsterDef, float, float)` constructor delegating to `super`,
and one or more `@Spawn(...)` annotations. `MonsterRegistry` reflection-scans this
package at classload and wires it up automatically — no registry list to edit. Use this
when the monster needs a fixed, always-present world location out of the box (see
Section 2) or truly custom AI (override `BaseMonster` methods).

Read `.claude/skills/npc-monster-creator/references/monster-schema.md` for the full
annotated field table before writing a JSON file — it also has the resist-index mapping
(which is *not* the naive Air/Water/Earth/Fire/Dark/Light order you'd guess from reading
the field name) and a worked power-calibration comparison (`sewer_rat.json`, level 3,
against the `arenamobxp*` tiered family from level 50 to 750) so new monsters aren't
eyeballed.

**Fast checklist for a monster's numbers:**
1. Pick a target level. Interpolate health/xpOnDeath/stats/dodge from the two nearest
   existing monsters at similar levels (reference file has the numbers) — don't invent
   round numbers from scratch.
2. Pick `walkPattern`/`attackPattern`/`deathPattern`/sound files by reusing an existing
   creature family's tokens (grep other JSON files and `monster/*.java` for a thematically
   close creature) unless new sprite assets exist for it.
3. Set `resists` asymmetrically to match the monster's theme (ice creature strong vs.
   Water/Air, weak vs. Fire; undead strong vs. Dark, weak vs. Light; etc.) — see the
   reference file's mapping table. Leave indices 6–11 at `100`.
4. Loot: gate rarer/more powerful items behind lower `chance` values, and scale overall
   loot quality to the monster's level/toughness — a rare boss should guard rarer drops
   than a common trash mob. Every `item` key must resolve in the item registry; see
   `item-creator` for how items are defined and how rarity/power tiers are chosen there.
5. Pick a `name` (and Java class name if applicable) whose substrings land it in the
   intended `MonsterClan` (see reference file) if faction membership matters for the
   monster's story role.

---

## 2. Spawning a monster (or NPC) in the world

### ⚠ Correction: `spawn/definition/SpawnGroup*.java` is not live spawn data

There are ~268 files named `src/main/java/com/perso/T4C/spawn/definition/SpawnGroup00NN.java`,
each holding a monster-group name, a respawn timing window, and a long list of world
coordinates, plus `SpawnGroupDefinition`/`SpawnGroups`/`monster/core/SpawnGroupRegistry`
wiring them together. **This looks exactly like "the spawn area mechanism" but it is
dead code** — verified by a repo-wide grep: nothing except `SpawnGroupRegistry.java`
and `SpawnGroups.java` themselves reference `SpawnGroupRegistry`/`SpawnGroups`, and no
runtime manager (`MonsterManager` included) ever calls into it. It appears to be
imported/preserved reference data from the original game, not something wired into
this recreation's live spawn pipeline. **Do not add a new monster's placement by writing
a new `SpawnGroupNNNN.java` file — it will compile fine and do nothing.**

### The mechanism that actually works: `@Spawn`

`src/main/java/com/perso/T4C/spawn/Spawn.java` is a repeatable class annotation:

```java
@Spawn(type = "Mortal Wombat", x = 1542, y = 372, z = 2, stationary = false, aggressive = true)
```

- `type` — the spawn-type string. Must resolve to a monster via `MonsterRegistry.findByName`
  (exact name, normalized name, or `spawnAliases`) or, for NPCs, via `NpcFactoryRegistry`.
- `x`, `y` — tile coordinates.
- `z` — map id: `0` = `WORLDMAP` (overworld), `1` = `DUNGEON`, `2` = `CAVERN`,
  `3` = `UNDERWORLD` (`config/MapDefinition.java`). `MonsterManager.resolveMapZ` also
  special-cases path substrings `/moontug/` → 2 and `/ravensdust/` → 4 as a fallback.
- `stationary` — if true, the monster never patrols/chases (good for shop-adjacent
  ambient monsters, caged beasts, "prop" creatures).
- `aggressive` — per-spawn-point override of the monster def's own `aggro`-derived
  default (`MonsterDef.isDefaultAggressive()`); lets the *same* monster definition be
  passive in one location and aggressive in another.

Because `@Spawn` is `@Repeatable`, one class can have many spawn points (a monster that
appears in several places), and `SpawnRegistry` classpath-scans every class under
`com.perso.T4C.monster` and `com.perso.T4C.npc` for these annotations at startup —
`MonsterManager.initializeMonstersFromMap` then filters by `z` per loaded map and queues
one `PendingSpawn` per point (doubled by `GameConstants.SPAWN_COUNT_MULTIPLIER = 2`).

**Practical consequence: `@Spawn` is a class-level annotation, so a monster only gets an
ambient, always-present world location if it's authored as a Java class (Section 1,
option B) carrying that annotation** — see `r151MortalWombat.java` for a monster and
`npc/Ortanalas.java` / `npc/ColosseumClerk.java` for NPCs, both patterns identical. A
pure-JSON monster (option A) has **no** automatic world placement; it only becomes
reachable by explicit lookup:

- `MonsterManager.spawnMonster(name, worldX, worldY[, respawn])` — used by the GM `/spawn`
  command (`input/GmCommandProcessor.java`) and by scripted "summon" events
  (`screens/MainGameScreen.java`). Good for quest set-pieces, arena mobs (see
  `npc/ColosseumClerk.java` + `tools/ArenaMobGenerator.java` for the pattern used by the
  `arenamobxp*.json` tiered family), or anything meant to appear on demand rather than
  sit idle in the world.
- Taming/summon-by-name flows that go through `MonsterRegistry.findByName`, which also
  checks `spawnAliases`.

**Decision rule:** if the monster is meant to be encountered by a player just walking
around (a farmable mob, a roaming boss, ambient wildlife), write it as a Java class with
`@Spawn`. If it's meant to be conjured by a script/quest/GM/arena system on demand, a
pure-JSON definition summoned via `spawnMonster(name, x, y)` (or an NPC script action —
see Section 4) is simpler and is exactly what the existing `arenamobxp*.json` tier does.

---

## 3. NPC authoring

NPCs auto-register the same way monsters do, but via `NpcFactoryRegistry`, which
classpath-scans `com.perso.T4C.npc` for any class exposing:

- `public static final String ID` (required — the NPC's registry key)
- a `(NpcContext)` constructor
- optional `public static final String DISPLAY_NAME` (falls back to `${npc.<id-lowercase>}`)
- optional `public static final String SPRITE_BASE`
- optional `public static NpcSpec spec()` (only for the declarative pattern below)

No manual registry list to edit — just create the class with these members and add
`@Spawn(type = "<Id>", x = ..., y = ..., z = ..., stationary = ..., aggressive = ...)`
for a fixed world position (same annotation, same rules as Section 2).

### Two authoring patterns — prefer the declarative one for new NPCs

**Declarative (`ScriptedNpc` + `NpcSpec` + `NpcBehavior`)** — the modern pattern, used by
`npc/Ortanalas.java` (trainer) and `npc/ColosseumClerk.java` (custom stateful behavior).
An `NpcSpec` record holds:

- `id`, `displayName`, `spriteBase`, `parts` (`List<NpcSpec.Part(BodyPart, spriteName)>`
  for equipment/appearance layers)
- `welcomeText` — shown on conversation start
- `topics: List<DialogueTopic>`, each `{keywords, response, actions}` — `keywords` and
  `response` are i18n keys (see the convention below), `actions` is a
  `List<NpcSpec.Action(ActionType, targets)>` for declarative behavior (see ActionType
  table below) — this is how a plain trainer/vendor NPC needs **no custom Java logic at
  all**, just spec data.
- `combatProfile` — level/HP/stats/AC/dodge/damage-formula for if the NPC gets attacked
  (almost always a huge HP/AC/dodge wall like `CombatProfile(100, 1_000_000, ..., 65535, "1d23+16")`
  so NPCs are effectively unkillable, matching every shipped example).

For a stock shopkeeper or trainer, you often don't even need a bespoke `NpcBehavior` —
add the NPC's name/id to `npc/catalog/ShopCatalog.java` or `TrainingCatalog.java`
(existing map of id → stock list) and reuse `ShopBehavior`/`TrainingBehavior`, following
the pattern already there for `Fali`, `Karahn`, `Asarr`, etc. Only write a custom
`NpcBehavior` (anonymous class overriding `onConversationStart`/`onKeyword`/...) when the
NPC needs stateful logic beyond stock dialogue — `ColosseumClerk`'s arena-slice
difficulty picker is the reference example.

**`ActionType` values** (`npc/ActionType.java`), wired generically in
`ScriptedNpc#runAction` — attach these to a `DialogueTopic`'s `actions` list instead of
writing imperative code:

`OPEN_SPELL_LEARNING`, `OPEN_SKILL_LEARNING`, `OPEN_SHOP`, `OPEN_REPAIR`, `GIVE_ITEM`,
`GIVE_QUEST`, `END_CONVERSATION`, `HEAL`, `SUMMON_COMPANION`.

`GIVE_QUEST`'s single target is a `QuestDef` id; it calls
`questService.giveOrReport(questId, npcSpecId, player)` for you — this is the standard
way to wire a quest-giver in the declarative pattern (see Section 4).

**Legacy (`BaseNPC` subclass)** — older NPCs like `npc/LighthavenSamaritan.java` extend
`BaseNPC` directly and hand-roll `talk()`, `onTopic()`, dialogue-state machines, and
direct `questService.giveOrReport(...)` calls. It still works and is fine to extend for
an *existing* NPC written this way, but prefer the declarative pattern for anything new
— less code, and the action wiring (quest/shop/heal/etc.) is uniform and reviewable.

### i18n dialogue key convention

Every shipped NPC's player-facing text is externalized to `assets/i18n/lang.json`
(resolved via `I18n.resolve`/`I18n.message`), never hardcoded:

- `npc.<id>` — the NPC's own display name string
- `npc.welcome.<id>` — opening line on conversation start
- `npc.topic.<id>.<N>` — response text for topic `N`
- `npc.topic_keyword.<id>.<N>.<M>` — the `M`-th keyword that triggers topic `N`

Follow this convention exactly (`<id>` lowercased, matching the pattern seen in
`Ortanalas`/`ColosseumClerk`/`LighthavenSamaritan`) so translation tooling keeps working,
and add the actual English strings to `assets/i18n/lang.json` — an NPC whose `SPEC`
references a key with no entry there will show the raw `${...}` placeholder in-game.

### Companion NPCs

A companion (a pet/hireling that follows the player, e.g. `WarriorSquire`,
`MageApprentice`) is a `CompanionDef` record (`npc/companion/CompanionDef.java`) —
equipment parts, level, combat stats, and a `List<SpellEntry>` for its autocast
abilities (trigger, spell key, cooldown, chance, etc.). Unlike monsters/NPCs, this list
**is** manually maintained: add your new `CompanionDef` file under
`npc/companion/definition/` and append it to `CompanionDefinitions.all()`. A companion
is granted to a player via the `SUMMON_COMPANION` NPC action or a script, not via
`@Spawn`.

---

## 4. Story/role guidance

Every NPC that isn't purely decorative should have one clearly stated purpose — pick one
and build the spec around it, don't leave it ambiguous:

- **Quest-giver** — `QuestDef.giverNpc` must be an exact string match for the NPC's
  `ID`. Wire the actual handoff either declaratively (a `DialogueTopic` with a
  `GIVE_QUEST` action targeting the quest id, in the modern pattern) or imperatively
  (`questService.giveOrReport(questId, ID, player)` from a topic handler, the
  `LighthavenSamaritan` pattern). See the `quest-creator` skill
  (`.claude/skills/quest-creator/SKILL.md`) for the full `QuestDef` schema and existing
  quest lore to stay consistent with (it also documents named locations, `worldZ`
  values, and canon lore fragments already committed in `lang.json`).
- **Vendor** — add an entry to `ShopCatalog`, reference it via `OPEN_SHOP`/`ShopBehavior`.
- **Trainer** — add an entry to `TrainingCatalog`, or a `TrainingAndFleeBehavior`/
  `OPEN_SKILL_LEARNING`/`OPEN_SPELL_LEARNING` action, as `Ortanalas` does.
- **Lore-only** — still write real dialogue topics (even 2–3), not a single flat line;
  every shipped example has multiple topics plus a farewell. Check
  `assets/i18n/lang.json` for existing dialogue-only lore hooks (search for the NPC's
  name or nearby NPCs) before inventing new lore that might contradict something already
  committed — the `quest-creator` skill's `references/world-reference.md` lists several
  uncommitted lore hooks (a "Dragon" NPC, Mirak, Resha, Tristan, Yrian, Lantalir, a gypsy
  alignment system) that are fair game to build into a real quest/NPC if the user's ask
  fits one of them.

**Monster ↔ quest story tie-in** — a kill-quest's target monster isn't just a name
match. `QuestService.recordKill` requires **both**:
1. `monsterName` matches `QuestDef.targetMonster` (case-insensitive), **and**
2. the kill's tile position falls inside `areaCenterX/Y` ± `areaRadiusTiles` on
   `targetWorldZ`.

So when you place a new monster to serve as a quest target, its actual spawn point(s)
(Section 2) must land inside the quest's defined area, or kills will silently never
count. `quest/definition/OrtanalasBridgeGoblins.java` (`giverNpc = "Ortanalas"`,
`targetMonster = "Goblin"`, area centered near the Lighthaven bridge) next to
`npc/Ortanalas.java`'s own `@Spawn` coordinates is the reference example — the NPC and
the quest's kill-zone are both placed near the same landmark for a reason.

---

## 5. End-to-end checklist

**New monster, spawned ambiently in the world:**
1. Decide JSON-only vs. Java class (Section 1) — pick Java class if it needs a fixed
   world spot out of the box.
2. Write `assets/monsters/<name>.json` (or the Java `definition()`), calibrating stats
   to a target level against real neighbors (reference file's tier table).
3. Set themed `resists` using the corrected index mapping (reference file).
4. Loot: real item keys, rarity-appropriate `chance` values, gold scaled to level.
5. Java class only: add `@Spawn(type=..., x=, y=, z=, stationary=, aggressive=)` with
   real, collision-free coordinates on an existing map (cross-check
   `quest-creator`'s named-location table, or the map/level editor via the
   `graphic-designer` skill, for sane coordinates).
6. If it's a quest target, confirm the spawn point(s) fall inside that quest's
   `areaCenterX/Y`/`areaRadiusTiles`/`targetWorldZ`.
7. Grep the chosen `name` against existing monster names/aliases to rule out a
   collision.

**New NPC:**
1. Decide its single clear purpose (vendor / trainer / quest-giver / lore-only /
   companion-granter) — Section 4.
2. Write the class under `com.perso.T4C.npc` with `ID`, `DISPLAY_NAME`,
   `(NpcContext)` constructor, and (declarative pattern) a `spec()` returning an
   `NpcSpec` with real dialogue topics and a huge `combatProfile`.
3. Add every dialogue string to `assets/i18n/lang.json` using the
   `npc.<id>`/`npc.welcome.<id>`/`npc.topic.<id>.N`/`npc.topic_keyword.<id>.N.M` key
   convention.
4. Wire its role: `ShopCatalog`/`TrainingCatalog` entry, a `GIVE_QUEST` action (or
   `questService.giveOrReport` call) matching a real `QuestDef.giverNpc`, or a
   `SUMMON_COMPANION` action pointing at a real `CompanionDef`.
5. Add `@Spawn(...)` for its world position.
6. If it's a quest-giver, confirm the matching `QuestDef.giverNpc` string is exact and
   that the quest's kill-zone (if any) overlaps where the target monster actually
   spawns.

## Caveat: could not verify against official T4C lore

`https://www.t4cfantasy.com/Addon` is blocked by this environment's network egress
proxy (`EGRESS_BLOCKED`) — this skill was written **without** being able to cross-check
official T4C faction names, zone geography, or monster/NPC lore against the source
site. Treat what's already committed in this codebase (`assets/i18n/lang.json`,
`quest/definition/*.java`, existing NPC/monster classes) as the working canon, and if
the user's request needs broader official lore, tell them to manually check
`https://www.t4cfantasy.com/Addon` and reconcile any conflicts — don't present invented
lore as canon-verified.
