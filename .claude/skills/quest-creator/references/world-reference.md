# World reference data for quest design

Source of truth: read the cited files yourself before relying on this list — it is a
snapshot, and the codebase will grow past it.

## Named locations (`teleport/NamedLocations.java`)

These are the only place names the game currently exposes to players in the Locations
fast-travel panel (`new NamedLocation(displayName, tileX, tileY, worldZ)`):

| Display name    | tileX | tileY | worldZ | worldZ meaning |
|------------------|------:|------:|-------:|-----------------|
| Lighthaven       | 2941  | 1062  | 0      | WORLDMAP |
| Silversky        | 1495  | 2470  | 0      | WORLDMAP |
| Windhowl         | 1812  | 1293  | 0      | WORLDMAP |
| Colosseum        | 1725  | 1825  | 0      | WORLDMAP |
| Home             | 2951  | 1038  | 0      | WORLDMAP |
| Makrsh Ptangh    | 2265  | 295   | 1      | DUNGEON |
| Stonecrest       | 144   | 737   | 0      | WORLDMAP |
| Tarantula Pond   | 773   | 1831  | 0      | WORLDMAP |
| Skraug Camp      | 601   | 172   | 0      | WORLDMAP |
| The Oracle       | 2968  | 2141  | 2      | CAVERN |

Treat this as the current canonical list of "real" zones/settlements in this
recreation. Reuse these names rather than inventing new ones unless the user
explicitly asks for a new place — and if they do, place it consistently (pick
coordinates on the correct map, away from existing named spots, and give it a
`worldZ` that matches what kind of place it is).

## `worldZ` / map identity (`config/MapDefinition.java`)

```java
WORLDMAP(...,  0)   // the overworld / outdoor island map
DUNGEON(...,   1)   // indoor dungeon-style interiors (e.g. temple basements)
CAVERN(...,    2)   // cave interiors
UNDERWORLD(..., 3)  // underworld map
```

A `QuestDef`'s `targetWorldZ` and a `NamedLocation`'s `worldZ` both index into this
enum. `MapDefinition.fromZ(int)` resolves a raw int to the enum; unknown values fall
back to `WORLDMAP`. There is no further subdivision (no per-island `worldZ` values) —
"islands" on the overworld are just different tile-coordinate regions of the same
`worldZ = 0` map, identified only by proximity to a `NamedLocation` or by convention
in code comments/i18n text.

## NPCs seen near existing quest content

- `LighthavenSamaritan` (`npc/LighthavenSamaritan.java`) — hands out
  `lighthaven_samaritan_rats`. Lives in/around Lighthaven.
- `Ortanalas` (`npc/Ortanalas.java`, spawn `x=2924, y=1093, z=0`) — hands out
  `ortanalas_bridge_goblins`. Despite the name, this is an NPC (a person), not a
  place — it spawns right next to the Lighthaven bridge, which is why the quest's
  `areaCenterX/Y` (2760, 1010) sits close to Lighthaven's own coordinates
  (2941, 1062).

## Monster clans / factions (`monster/core/MonsterClan.java`,
`monster/core/MonsterClanRelations.java`)

`MonsterClanRelations.resolveClan(className, monsterName)` buckets monsters into:

`GOBLIN, HORSE, ANIMAL, UNDEAD, DEMON, DRACONIAN, CENTAUR, SKAVEN, KOBOLD,
KRAANIAN, ORC, INSECT, HUMAN, BEAST, NEUTRAL`

matched by substring on the monster's class/display name (e.g. anything containing
"goblin" → `GOBLIN`, "skeleton"/"zombie"/"mummy"/"lich" → `UNDEAD`,
"cleric"/"guard"/"mage"/"warrior"/"thief"/"wizard" → `HUMAN`, etc. — read the file
for the full mapping). Only one hostile relation is wired by default:
`GOBLIN` is an enemy of `HORSE`. Use clan membership as a cheap proxy for "faction"
when writing quest fiction (e.g. a goblin-hunting quest is thematically about the
GOBLIN clan; don't call goblins "undead" or "insect" in flavor text).

## Lore fragments already committed in `assets/i18n/lang.json`

These are hooks other quests/NPCs already reference — treat them as canon and don't
contradict them without the user asking for a retcon:

- `npc.dragon.quest.*` — a "Dragon" NPC wants a **tomb raider dealt with in the
  Crypt**; reward line mentions 5000 XP (this is flavor text only — no `QuestDef`
  wires it up yet, so it may be an intended future quest).
- `npc.mirak.trust.quest` — Mirak: "Kill a hundred goblins for **Windhowl** and you
  will deserve my trust."
- `npc.resha.quest.*` — Resha references "bows of Centaur Slaying" and defers other
  quest talk to "Chryseida".
- `npc.tristan.progress.quest` — Tristan: investigate a caravan that "took remains
  from a winged corpse **east of Stonecrest**".
- `npc.yrian.stone.request` — Yrian guards nature via the "**Stone of Life**"; if
  stolen, the player must retrieve it.
- `npc.lantalir.feylor.request` — Lantalir seeks the "**Book of Feylor**".
- `npc.gypsy.question.*` — an alignment system calling the player a "**New Breed**"
  and sorting them as "**Harvester of Life**" or "**Giver of Life**".

None of these are implemented as `QuestDef`s yet — they are dialogue-only hooks. If
you're asked to build out one of these into a real quest, treat the existing line as
the quest's premise and keep new text consistent with it.

## `t4cfantasy.com/Addon` — could not be fetched from this environment

The network egress proxy in this sandbox blocks `t4cfantasy.com` and
`www.t4cfantasy.com` outright (`EGRESS_BLOCKED`). This skill was written **without**
being able to cross-check the official T4C world geography/lore page. Before
inventing new islands, factions, or story elements that go beyond what's already in
this codebase (see above), a human should manually check
`https://www.t4cfantasy.com/Addon` (and the site's other lore pages) and reconcile
any conflicts. Don't present invented lore as canon-verified.
