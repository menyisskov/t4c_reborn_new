---
name: quest-creator
description: Designs and implements new quests for the T4C game (kill-count/fetch-style objectives, quest-giver NPCs, per-character progress flags, rewards, multi-stage or multi-island questlines, and the i18n text they need). Use this whenever the user wants to add a new quest, questline, or storyline to the game, wants an NPC to "give a quest" or "reward the player for killing X", or just pitches a story/quest idea in plain language without saying the word "quest". Also use it to review or extend the two existing quests (Lighthaven rats, Ortanalas goblins) or to figure out where quest progress/flags for a character are stored.
---

# Quest Creator

## Reality check before you start

The quest system in this codebase is **very young**: as of this writing there are
exactly two quests, both defined in
`src/main/java/com/perso/T4C/quest/definition/`. There is no quest editor, no
external data file, and only one supported quest *shape*: "kill N of monster X
inside one circular area on one map, report to NPC Y for gold+XP." Don't assume
richer built-in support (multi-step objectives, item-fetch quests, branching
dialogue trees, item rewards) exists — it doesn't yet. Where the user's idea needs
more than that, say so plainly and build the extra logic explicitly (see
"Beyond a single `QuestDef`" below) rather than pretending the data model covers it.

Read the two existing quests before writing a new one — they're short:
- `quest/definition/LighthavenSamaritanRats.java`
- `quest/definition/OrtanalasBridgeGoblins.java`

## The `QuestDef` schema, field by field

Source: `src/main/java/com/perso/T4C/quest/QuestDef.java` (a Lombok
`@AllArgsConstructor` — the constructor order below is exact, read from the source,
not guessed).

```java
public QuestDef(
    String id,              // 1. unique quest key, snake_case, e.g. "ortanalas_bridge_goblins"
    String title,           // 2. i18n placeholder, "${quest.<id>.title}"
    String giverNpc,        // 3. NPC id/type that offers & completes this quest (must match
                             //    the NPC's spawn/registry id exactly, case-insensitive)
    String targetMonster,   // 4. monster name/display-name the player must kill
    int requiredKills,      // 5. how many kills complete the quest
    int targetWorldZ,       // 6. which map: 0=WORLDMAP, 1=DUNGEON, 2=CAVERN, 3=UNDERWORLD
                             //    (see config/MapDefinition.java)
    int areaCenterX,        // 7. objective-area center, tile X (on targetWorldZ's map)
    int areaCenterY,        // 8. objective-area center, tile Y
    int areaRadiusTiles,    // 9. objective-area radius, tiles (circular; see insideObjectiveArea)
    int rewardGold,         // 10. gold paid to the player on completion
    int rewardXp,           // 11. XP paid to the player on completion (via XpCurve)
    String offerText,       // 12. i18n placeholder, "${quest.<id>.offer}" — shown when accepted
    String completionText,  // 13. i18n placeholder, "${quest.<id>.completion}" — shown on turn-in
    String completedText,   // 14. i18n placeholder, "${quest.<id>.completed}" — shown if the
                             //    player talks to the giver again after completing
    String activationFlag   // 15. OPTIONAL (there's a 14-arg overload that defaults this to
                             //    null). A quest-flag name; if the player already has this
                             //    flag set to non-zero, the quest is treated as STARTED even
                             //    though giveOrReport() was never called for it. Used to let
                             //    something else (e.g. character creation) auto-activate a
                             //    quest. It also changes the *kill-progress flag name* — see
                             //    below.
);
```

There is **no item-reward field** and **no multi-objective field** — despite the
`quest.reward.item.none` i18n string existing in the UI, `QuestDef` cannot currently
grant an item. If a quest needs an item reward, that's new functionality (extend
`QuestDef` + `QuestService.complete()` + the UI), not just a new data entry — flag
this to the user rather than silently dropping the item reward.

### Fully annotated real example

```java
// OrtanalasBridgeGoblins.java
new QuestDef(
    "ortanalas_bridge_goblins",                 // id
    "${quest.ortanalas_bridge_goblins.title}",  // title
    "Ortanalas",                                // giverNpc — an NPC standing near the
                                                 //   Lighthaven bridge, NOT a place name
    "Goblin",                                   // targetMonster
    15,                                         // requiredKills
    0,                                          // targetWorldZ = WORLDMAP (outdoors)
    2760, 1010,                                 // areaCenterX, areaCenterY
    100,                                        // areaRadiusTiles
    1000,                                       // rewardGold
    750,                                        // rewardXp
    "${quest.ortanalas_bridge_goblins.offer}",
    "${quest.ortanalas_bridge_goblins.completion}",
    "${quest.ortanalas_bridge_goblins.completed}",
    null);                                      // activationFlag — none; player must
                                                 //   talk to Ortanalas to start it
```

Compare against `LighthavenSamaritanRats` (`requiredKills=15`, `targetWorldZ=1`
DUNGEON — the "temple basement", `areaRadiusTiles=120`, `rewardGold=0`,
`rewardXp=2500`, `activationFlag="__NEWBIE_QUEST"` — this one auto-starts, likely
for new characters). **Caveat found while researching**: that quest's
`completion` i18n text says "500 gold coins and 300 experience points", but the
actual numeric fields pay `0 gold / 2500 XP` — the flavor text and the real reward
have drifted apart. Don't repeat this mistake: when you write offer/completion
text that names a gold/XP amount, make sure it matches `rewardGold`/`rewardXp`
exactly, or better, keep flavor text reward-number-free.

## How per-character quest progress is actually stored

There is no separate "quest state" table. `Player` (`player/Player.java`) carries a
generic, already-flexible flag bag:

```java
private Map<String, Integer> questFlags = new HashMap<>();
public int getQuestFlag(String flag);
public void setQuestFlag(String flag, int value);
public void setTimedQuestFlag(String flag, int value, long durationMillis); // expiring flag
```

This map is persisted with the rest of character save state:
`PlayerStateDto.questFlags` ↔ `PlayerStateMapper` ↔ `PlayerStateStore` /
`LocalCharacterStore`. Anything you `setQuestFlag(...)` survives logout/relog and
server restarts automatically — you do not need to touch the save format for a
normal quest.

`QuestService` (`quest/QuestService.java`) builds two flag names per quest from its
`id` (or its `activationFlag`, if set):

```java
statusFlag(def) -> "quest." + id + ".status"   // 0=not started, 1=active, 2=completed
killsFlag(def)  -> "quest." + (activationFlag != null ? activationFlag : id) + ".kills"
```

`QuestService.STATUS_NOT_STARTED / STATUS_ACTIVE / STATUS_COMPLETED` are the only
states. Progress increments happen in `recordKill()`, gated by target monster name
+ `insideObjectiveArea()` (circle check on `targetWorldZ`/`areaCenterX/Y`/
`areaRadiusTiles`) + the quest currently being `STATUS_ACTIVE`.

**When a new quest needs more than an int counter and a status** (e.g. "did the
player pick dialogue option A or B", "which of 3 islands has the player already
visited", "a multi-stage quest with 4 distinct phases"): you almost certainly do
NOT need new save-file fields. `questFlags` is a free-form `String -> int` map —
just invent more flag keys under a namespace you control, e.g.
`"quest.my_quest_id.stage"`, `"quest.my_quest_id.visited_island_b"`, and read/write
them yourself with `player.getQuestFlag(...)`/`setQuestFlag(...)` from your NPC
script or Java NPC class. Reserve "this needs a new `Player`/`PlayerStateDto`
field" for genuinely non-integer data (free text, a list, a timestamp you can't
express as an int) — that's real save-format surgery (`Player.java` +
`PlayerStateDto.java` + `PlayerStateMapper.java`, plus a migration story for
existing save files) and should be called out explicitly to the user as bigger than
"just add a quest", not done silently.

## Registering a new quest

1. Add a definition class in `quest/definition/`, following the two existing ones
   exactly (private constructor, static `definition()` factory returning
   `QuestDef`).
2. Add it to `QuestDefinitions.all()`:
   ```java
   public static List<QuestDef> all() {
     return List.of(
         LighthavenSamaritanRats.definition(),
         OrtanalasBridgeGoblins.definition(),
         YourNewQuest.definition());
   }
   ```
   `QuestRegistry` caches this list on first `load()` and indexes it by lower-cased
   `id`; it doesn't hot-reload, so a running server needs a restart to pick up a new
   definition, same as any other code change.
3. Add its four i18n text keys to `assets/i18n/lang.json` (flat JSON map, edited by
   hand today — `I18n.update()` exists for programmatic writes but the two existing
   quests were added directly to the file):
   ```json
   "quest.your_new_quest_id.title": "...",
   "quest.your_new_quest_id.offer": "...",
   "quest.your_new_quest_id.completion": "...",
   "quest.your_new_quest_id.completed": "..."
   ```
   Keep the JSON alphabetically-ish grouped near the other `quest.*` keys for
   reviewability (the file isn't strictly sorted, but existing quest blocks are
   contiguous).
4. Wire an NPC to actually offer/complete it — see next section. A `QuestDef` with
   no NPC calling `giveOrReport`/`turnInReadyQuests` for it is inert.

## Wiring the quest-giver NPC

Two patterns exist in this codebase; pick whichever matches how the NPC itself is
built (see the `npc-monster-creator` skill, if present in this repo's
`.claude/skills/`, for how to create the NPC in the first place — this skill only
covers the quest side of the wiring):

**A. Hard-coded Java NPC** (e.g. `npc/LighthavenSamaritan.java`,
`npc/Ortanalas.java`): call the injected `QuestService` directly from the NPC's
dialogue-handling code:
```java
String response = questService.giveOrReport(YOUR_QUEST_ID, ID, player); // ID = this NPC's id
if (response != null && !response.isBlank()) showDialog(response, 0L);
```
and somewhere in the NPC's greet/interaction path, offer completion:
```java
questService.turnInReadyQuests(ID, player);
```

**B. Scripted NPC** (`npc/core/ScriptedNpc.java`, driven by a script/JSON action
list): use the `GIVE_QUEST` action with the quest id as its single target —
`ScriptedNpc` calls `questService.giveOrReport(action.targets().get(0), spec.id(), player)`
for you; `turnInReadyQuests` is invoked automatically on interaction
(`ScriptedNpc` line ~368).

In both cases the NPC's own id (`spec.id()` / the class's `ID` constant) **must
equal** the `QuestDef.giverNpc` value, compared case-insensitively —
`QuestService.giveOrReport` silently refuses (and logs a warning) if a different
NPC tries to hand out a quest it doesn't own.

## Multi-island / multi-area quests

A single `QuestDef` supports exactly one target monster, one kill count, and one
circular area on one `targetWorldZ`. It has **no built-in notion of sequential
stages or multiple areas**. For a quest that should span islands/areas, choose one
of these honestly-scoped approaches and say which one you're using:

1. **Chain of `QuestDef`s** (preferred, stays data-only): split the storyline into
   several quests, each with its own id, giver NPC, and single-area objective. Gate
   quest 2 on quest 1 being finished by giving quest 2's giver NPC dialogue logic
   that checks `QuestService.statusFor(player, quest1Def) == STATUS_COMPLETED`
   before offering quest 2 (or use `activationFlag` on quest 2, set by quest 1's
   completion code, so quest 2 auto-activates the moment quest 1 finishes). Each
   stage can live on a different island/`worldZ` — that's exactly what
   `targetWorldZ`/`areaCenterX/Y` are for, per stage.
2. **One quest, custom stage tracking** (needed for "kill monster in EITHER zone A
   or B", "visit an NPC on island B after collecting something on island A", or any
   objective `recordKill`'s single-area circle check can't express): keep a single
   `QuestDef` for the reward/bookkeeping shell, but track the real progress with
   your own `quest.<id>.stage` flag(s) read/written from NPC script code, not from
   `recordKill`. Be explicit with the user that this requires actual game-logic
   code (NPC dialogue conditions, possibly a custom trigger), not just a new
   `QuestDef` entry.

For real zone/island names and `worldZ` values to reference (don't invent new place
names casually), see `references/world-reference.md` in this skill — it lists the
game's current named locations (`teleport/NamedLocations.java`), the `worldZ`/map
enum, and NPC/monster-clan context gathered from this codebase.

**Inter-island travel**: there is no in-fiction "ship" or "portal" system visible in
the researched code — cross-map/cross-region movement is implemented as fixed
teleport points (`teleport/definition/Teleport*.java`, ~1000+ numbered classes, each
a `TeleportDefinition(id, fromZ, fromX, fromY, toZ, toX, toY)` — read a couple to
confirm the exact field order before relying on it) plus the player-facing
`NamedLocation` fast-travel list. If a quest requires the player to travel between
areas, either rely on existing teleports/named locations getting them there, or ask
whether a new teleport point is in scope — don't invent new travel lore (airships,
portals, etc.) without checking with the user, since **this skill could not verify
the official T4C setting against `t4cfantasy.com/Addon`** (see below).

## Story and lore guidance

Before inventing new NPCs, places, or factions for a quest:

1. **Check what already exists in this codebase** — the two live quests, the
   dialogue-only lore hooks already sitting in `assets/i18n/lang.json` (Dragon's
   Crypt tomb-raider, Mirak's Windhowl goblin bounty, Tristan's Stonecrest
   investigation, Yrian's Stone of Life, Lantalir's Book of Feylor, the
   Harvester/Giver-of-Life alignment system — full list in
   `references/world-reference.md`), the named locations, and monster clans
   (factions: GOBLIN, ORC, SKAVEN, KOBOLD, UNDEAD, DEMON, DRACONIAN, CENTAUR,
   KRAANIAN, INSECT, HUMAN, BEAST, ANIMAL, HORSE, NEUTRAL — from
   `monster/core/MonsterClanRelations.java`). A new quest should fit into this
   fabric rather than contradict it — e.g. don't write goblins as peaceful allies
   of Lighthaven when an existing quest has the player killing them as a threat to
   Lighthaven's bridge.
2. **Use `npc-monster-creator`** (if available under `.claude/skills/`) when the
   quest needs a new NPC or new monster — that skill owns spawn definitions, dialog
   scripts, and monster stat/loot tables; this skill only owns the `QuestDef` +
   quest-flag + i18n side.
3. **Cross-check `https://www.t4cfantasy.com/Addon`** (and the main site) for the
   official T4C world geography, factions, and any documented quest lines before
   inventing content that claims to be "canon". **This skill's own research could
   not reach that site** — the sandbox's network egress proxy blocks
   `t4cfantasy.com` and `www.t4cfantasy.com` outright. Tell the user plainly that
   lore grounding in this skill comes only from what's already in the codebase, and
   that they (or a session with working network access) should manually check
   `t4cfantasy.com/Addon` before treating any newly invented world lore as
   authoritative.

## Reward calibration (scale from the two existing quests)

| Quest | Kills required | Gold | XP | Notes |
|---|---:|---:|---:|---|
| `lighthaven_samaritan_rats` | 15 | 0 | 2500 | Newbie/auto-start quest (`activationFlag`), dungeon basement, small radius (120) |
| `ortanalas_bridge_goblins` | 15 | 1000 | 750 | Regular open-world quest, worldmap, radius 100 |

There's no formula documented anywhere — these are just two hand-picked data
points at roughly the same kill count (15) with very different gold/XP splits.
When calibrating a new quest, pick a `requiredKills` and reward pair that's
consistent with one of these two "tiers" (cheap newbie XP-only quest, vs. modest
open-world gold+XP quest) unless the user asks for something bigger, and always
double check the offer/completion text quotes the same numbers you put in
`rewardGold`/`rewardXp`.

## End-to-end checklist for adding one new quest

1. Decide: quest id (snake_case), giver NPC (existing or new — use
   `npc-monster-creator` if new), target monster (existing or new), map/`worldZ`,
   objective-area center + radius, `requiredKills`, `rewardGold`/`rewardXp`,
   whether it auto-activates (`activationFlag`) or is offered on talk.
2. Sanity-check the area coordinates and `worldZ` against
   `references/world-reference.md` / `NamedLocations.java` so the quest's location
   actually matches the fiction ("near Lighthaven" should be worldZ 0, coordinates
   close to `(2941, 1062)`, etc.).
3. Check `references/world-reference.md` (and, ideally, `t4cfantasy.com/Addon`
   manually) so the story doesn't contradict existing lore/factions/places.
4. Write the `QuestDef` class in `quest/definition/`.
5. Add it to `QuestDefinitions.all()`.
6. Add the four `quest.<id>.*` i18n keys to `assets/i18n/lang.json`, with
   completion/offer text that matches the actual `rewardGold`/`rewardXp` numbers.
7. Wire the giver NPC (`GIVE_QUEST` scripted action, or direct
   `questService.giveOrReport`/`turnInReadyQuests` calls) — confirm the NPC's id
   matches `giverNpc` exactly.
8. If the quest needs state beyond "kill count toward a single target in one area"
   (multi-stage, multi-island, branching), design the extra `quest.<id>.*` flags
   explicitly and say so — don't silently under-implement it as a single
   `QuestDef` that can't actually express the story.
9. Build and, if there's a way to run the game locally, walk the quest end to end
   (accept → progress → turn-in → re-talk-to-giver-after-completion) before
   calling it done.
