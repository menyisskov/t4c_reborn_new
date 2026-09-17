# Content pass: The Sunken Chancel & Cinderreach Hills

Two new outdoor encounter zones, fully implemented and wired end-to-end (monsters, loot,
spells, quests, NPCs). Written up here so the shape of the pass — and the ideas that didn't
make the cut — are easy to review in one place before merging.

## Research note: t4cfantasy.com / t4cbible.com

The task was to mine `t4cfantasy.com` (Addon/Bible pages) and `t4cbible.com` for lore to build
from. Both domains are blocked by this environment's network egress proxy (`EGRESS_BLOCKED`) —
same limitation the `item-creator`, `npc-monster-creator`, and `quest-creator` skills already
flag. Direct fetches of `t4cfantasy.com/Addon`, `t4cfantasy.com/Bible/*`, and `t4cbible.com/*`
all failed. Web search (not blocked) surfaced enough real T4C lore to ground this pass without
inventing wholesale:

- The game world is **Althea**, spanning the islands **Arakas**, **Raven's Dust**, and
  **Stoneheim**, with a fourth "Ancient Land" island added later in the original game's history.
- T4C Bible categorizes monsters by island/source: Arakas, Raven's Dust, Stoneheim, Seraph,
  Add-on, Beta, and Mini-Boss tiers.
- Raven's Dust content includes Bane's Island, the City of Silversky, Deep Ones Cave, Skeleton
  Cave, and the RD Crypt (with Zhakar's Tower) — which lines up suggestively with this repo's
  existing `Silversky` named location and the `Zhakar` trainer NPC already in `TrainingCatalog`.

**This could not be cross-checked further** — someone with working access to
`t4cfantasy.com/Bible` and `t4cbible.com` should verify the two zones below (and the backlog
ideas) don't collide with or contradict official zone geography, faction names, or monster
identities before treating them as canon. Everything below was built from what's *already
committed* in this codebase (`assets/i18n/lang.json` lore hooks, `NamedLocations.java`,
`MonsterClanRelations.java`) plus the web-search fragments above, per the existing skills'
"don't invent lore you can't verify" rule.

## What shipped in this pass

### Zone A — The Sunken Chancel (water/undead, level ~38–50)
A flooded cult shrine on the coast east of Silversky. A drowned priesthood has been dragging
locals under since the tide "woke" their chancel.

- **Monsters**: `Drowned Acolyte` (trash, lvl 38), `Tideclaw Crab` (lvl 42), boss
  `Mordrenn the Drowned Inquisitor` (lvl 50).
- **Items**: `Mordrenn's Drowned Cowl` (HEAD, boss drop), `Tideclaw Band` (RING1, crab drop),
  `Acolyte's Tarnished Locket` (NECK, sold by Chryseida).
- **Spells**: `Riptide Surge` (water attack bolt, lvl 45), `Drowned Ward` (water/dark resist +
  AC buff, lvl 48) — both taught by the new trainer NPC.
- **Quest**: `Tide Warden's Plea` (`silversky_tide_warden`) — kill 20 Drowned Acolytes, offered
  by the new NPC `Tide Warden Bryn`, stationed near Silversky's coast.
- **World placement**: WORLDMAP, center (1750, 2300), radius 140 — chosen by grepping every
  existing `@Spawn` coordinate on `z=0` and picking a pocket with zero neighbors within that
  radius (see methodology note below).

### Zone B — Cinderreach Hills (fire, level ~58–70)
Smoldering foothills south of Windhowl, home to a fire-wolf pack and the young drake they
answer to.

- **Monsters**: `Cinder Whelp` (trash, lvl 58), `Ashfang Stalker` (lvl 63), boss
  `Ignarok the Emberfang` (lvl 70).
- **Items**: `Cinderwrought Sash` (BELT, Stalker drop), `Sunken Vestment` — no, see note below —
  `Ignarok's Emberfang Claw` (WEAPON, boss drop, unique).
- **Spells**: `Cinderburst` (fire AOE nuke, lvl 68), `Emberheart Resolve` (fire resist + AC +
  max-HP buff, lvl 66) — both taught by the new trainer NPC.
- **Quest**: `The Emberfang Hunt` (`emberfang_hills_bounty`) — kill 20 Ashfang Stalkers, offered
  by the new NPC `Rurik Cinderwatch`, stationed south of Windhowl.
- **World placement**: WORLDMAP, center (1900, 1600), radius 140 — same zero-neighbor placement
  method; notably the hills directly around the existing `Skraug Camp` named location were
  **rejected** as a boss zone site because they're already dense with existing spawns (170+
  `@Spawn` points inside a 320×320 tile window there) — Cinderreach was pushed further out,
  southeast of Windhowl instead, to avoid stacking content on top of what's already there.

(`Sunken Vestment`, a CAPE sold by Chryseida, is water-themed and part of Zone A's loot/shop
set, not Zone B's — noted above only because the item list got interleaved when drafting.)

## Honest limitations, flagged rather than papered over

- **`Ignarok's Emberfang Claw`'s base weapon damage.** Per the `item-creator` skill:
  `ItemJsonDef.toItemDefinition()` never sets a `dmgFormula`, so *any* JSON-authored weapon —
  this one included — falls back to the engine's flat, stat-independent 1–4 melee roll
  regardless of its stats. The claw's `boosts[]` (fire power, attack skill, melee damage bonus)
  are real and active; its *base* swing damage is not scaled. Extending `ItemJsonDef` to accept
  `dmgFormula`/`atkDelay` is a small, additive fix (mirrors how legacy weapons already express
  it) but touches shared parsing code, so it wasn't done unattended here — flagging it as the
  natural follow-up if a "real" scaled weapon drop is wanted.
- **No new sprite art.** Every monster reuses an existing animation family (Zombie, Scorpion,
  Wolf, the "Skeleton King" boss puppet, the Agmorkian/Kraanian drake rig) and every item reuses
  an existing, `SpriteBinIO`-verified sprite name (verified live against `assets/sprites/*.bin`
  with a throwaway lookup, not guessed). The theme is carried by name/stats/resists, not new art
  — consistent with the `graphic-designer` skill's own finding that this repo has no wired-up
  pipeline for packing brand-new PNGs today.
- **Spawn-point collision checking was grep-based, not visual.** Coordinates were chosen by
  scanning every existing `@Spawn(..., z = 0, ...)` in the Java monster/NPC classes and picking
  pockets with zero hits in a 280×280 tile window — but the game's *ground terrain* at those
  coordinates was never inspected (no GUI/display in this environment to run
  `com.perso.T4C.MapEditor`). **Before merging, someone should open the two zones in the in-repo
  map editor** (`java -cp ... com.perso.T4C.MapEditor`, jump to (1750, 2300) and (1900, 1600) on
  `worldmap`) and confirm the terrain there is walkable/sane — the spawns will "work" either way
  (monsters don't need painted terrain to spawn), but they might land on water/void tiles that
  look wrong.
- **Registry snapshot tests updated.** `SpellRegistryParityTest` (287→291) and
  `LighthavenSamaritanTest` (462→464 NPC registrations) had hardcoded totals that this pass
  necessarily changed; both were bumped to the new real counts, not loosened.

Full test suite (`mvn test`) and a full `mvn compile` both pass after this change.

## Backlog: further ideas not built this pass

Kept lighter-weight (a sentence or two each) since the goal here was breadth of *ideas*, not
building everything at once. Several deliberately hook into lore fragments **already committed**
in `assets/i18n/lang.json` but not yet built into real quests (see `quest-creator`'s
`references/world-reference.md`) — building on those is lower-risk than inventing new factions,
since the flavor text already exists as canon.

**Quests / story**
- **The Dragon's Crypt tomb-raider** — `npc.dragon.quest.*` already promises 5000 XP for
  dealing with a tomb raider in "the Crypt"; no `QuestDef` wires it up yet. A natural next quest:
  reuse the RD Crypt lore surfaced above.
- **Mirak's Windhowl goblin bounty** — `npc.mirak.trust.quest` asks for 100 goblin kills "for
  Windhowl." Since `ortanalas_bridge_goblins` already exists near Lighthaven, a second,
  higher-count goblin quest anchored at Windhowl (using the existing `Goblin` monster, a fresh
  area) would pair naturally as a "trust" storyline capstone.
- **Tristan's Stonecrest investigation** — `npc.tristan.progress.quest` references a caravan
  that "took remains from a winged corpse east of Stonecrest." Strong hook for a fetch-flavored
  quest once item rewards are supported (see limitation below), or a kill-quest against a
  winged/draconian monster near Stonecrest today.
- **Yrian's Stone of Life** and **Lantalir's Book of Feylor** — both already-committed
  NPC hooks with no quest behind them; either could become a short fetch/kill questline once a
  fitting monster guards the item.
- **A three-quest "Harvester of Life" alignment arc** — `npc.gypsy.question.*` already sets up a
  binary alignment system (Harvester vs. Giver of Life). Worth scoping as its own multi-quest
  design pass rather than a single `QuestDef`, since it implies branching, which the current
  quest system doesn't natively support (see `quest-creator`'s "Beyond a single QuestDef"
  section) — a good candidate for the next `game-director`-orchestrated request.

**Monsters / bosses**
- A **Raven's Dust "Deep Ones"**-flavored aquatic monster line (per the web-search lore above),
  distinct from this pass's Drowned Acolytes, if the team wants to lean into the real T4C
  Raven's Dust geography once `t4cfantasy.com` is reachable to confirm names.
- A **third, higher-level capstone boss** tying Zone A and Zone B together narratively (e.g. a
  shared "old god" both cults answer to) — deliberately not built here since it would need a
  genuine multi-stage quest chain (see quest-system limitation above).

**Items**
- A matching **6-piece elemental armor set** for either zone's element (water/dark or fire),
  following `tools/ArmorSetGenerator.java`'s proportional-split pattern — this pass only added
  single accessory/armor pieces, not a full set.
- The **weapon damage formula fix** described above, unlocking real scaled-damage JSON weapons
  generally (benefits far more than just `Ignarok's Emberfang Claw`).

**Maps**
- Both new zones currently live as *outdoor encounter areas* painted onto the existing
  `worldmap` terrain — no new tile art, no new dungeon interior. A genuine next step *with
  editor access*: paint an actual sunken-chancel interior (as a `DUNGEON`/`CAVERN` map area) and
  a cave mouth for Ignarok's den, then move the two bosses off the open worldmap and into those
  interiors for a more "dungeon-boss" feel — see `graphic-designer`'s "Add a new expansion
  map/location" checklist for the exact editor workflow (`F8`/`F9` for spawn placement persist
  automatically; hand-placed decor objects do not, per that skill's documented gap).
- A **5th top-level map** (a real new `MapDefinition` entry, not just new worldmap territory) is
  possible per the `graphic-designer` skill but requires an enum + `Paths.java` change and a
  recompile — flagged only as a "this is possible, not attempted" option, since neither zone in
  this pass needed it.
