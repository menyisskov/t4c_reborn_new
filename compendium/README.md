# T4C Reborn Compendium

A local, static, searchable stat-sheet site documenting every zone, monster, item, spell,
NPC and quest added on top of the original T4C fork — in the spirit of the classic
`t4cfantasy.com` stat sheet, but modern, cross-linked, and colored by item rarity/element.

## Running it

No build step, no server required — the data is embedded directly in `data.js`, so it
works from the filesystem:

```
open compendium/index.html          # macOS
xdg-open compendium/index.html      # Linux
```

Or, if your browser blocks local `file://` script execution, serve the folder:

```
cd compendium && python3 -m http.server 8090
# then open http://localhost:8090
```

## Regenerating the data

All game data (`data/*.json` and the bundled `data.js`) is generated from the live game
registries by `tools/CompendiumExporter.java` — never hand-edit those files directly
(except `data/zones.json`, `data/statids.json`, `data/meta.json`, which are hand-authored
reference/curation files the exporter reads back in and bundles alongside the generated
ones). `data/maps/*.png` and `data/maps.json` are also generated, by a second tool,
`tools/MinimapExporter.java` — see "Zone map images" below; run it *before*
`CompendiumExporter` so `maps.json` exists for the bundler to read back in.

**`.github/workflows/compendium.yml` does this automatically** on every push to `main`: it
recompiles, re-runs the exporter, and pushes a `chore: regenerate compendium data` commit
straight to `main` if anything drifted (stat tweaks, new loot, reworded quest text, etc.). So
for changes to *already-tracked* content, you don't need to do anything — the site catches up
on its own after merge.

You still need to run it yourself, locally, when:

- You're adding a **genuinely new** zone/monster/spell/NPC/quest and want to see it on the
  site before merging (CI only runs after a push to `main`, not on PR branches).
- The new content needs a source-code change first — the "is this new" allow-lists at the top
  of `CompendiumExporter.java` (`NEW_MONSTER_NAMES`, `NEW_SPELL_CLASSES`, `NEW_NPC_IDS`,
  `NEW_QUEST_IDS`) are hand-curated (there's no `isNew`/rarity field in the game data itself),
  so a class name has to be added there — and, for a new zone, a row added to
  `compendium/data/zones.json` — before the exporter will pick it up at all. CI regenerating
  on merge doesn't skip this step; it just means you don't *also* have to commit the
  regenerated JSON by hand once the allow-lists are updated.

```bash
mvn -q compile
mvn -q dependency:build-classpath -Dmdep.outputFile=/tmp/cp.txt
java -cp "target/classes:$(cat /tmp/cp.txt)" com.perso.T4C.tools.MinimapExporter compendium/data
java -cp "target/classes:$(cat /tmp/cp.txt)" com.perso.T4C.tools.CompendiumExporter compendium/data
```

This overwrites `compendium/data/*.json`, `compendium/data/maps/*.png` and
`compendium/data.js`.

## Zone map images

`tools/MinimapExporter.java` renders a stylized top-down PNG for every zone listed in
`data/zones.json` (i.e. new-since-fork zones only — not the whole 3072x3072 worldmap, which
at real pixel resolution would be several gigapixels, far too large for a web page): a
padded crop around the zone's own `worldmapCenter`, colored tile-by-tile from the *real*
game art (`assets/maps/worldmap/worldmap.mapbin` for which ground sprite sits on each tile,
`assets/sprites/sprites_*.bin` for that sprite's actual average pixel color — genuinely
sampled, not invented placeholder colors). It also writes `data/maps.json`: every NPC and
monster spawn point (plus any nearby original-game landmark) inside each zone's cropped
area, in world tile coordinates, which `app.js`'s `#/maps/:id` page turns into clickable
pins. Ground layer only — no decor/building overlay — so treat exact edges loosely; a
monster type gets tagged `"boss"` in `maps.json` purely by spawn-count (≤2 spawns inside the
crop), since `MonsterDef` has no explicit boss/unique flag to key off instead.

Re-run it whenever a zone's `worldmapCenter`/radius changes in `zones.json`, or a new
zone/NPC/monster spawn is added — same "needs a local run before merging" caveat as
`CompendiumExporter` above; `compendium.yml` also re-runs it on every push to `main`.

## Scope

This site documents **new-since-fork content only**, not the entire legacy game database:
everything in `CHANGELOG.md`/`TASKS.md` (zones, their monsters/items/spells/quests/NPCs),
plus every JSON-authored item under `assets/items/` (all 120 are new — the JSON item
pipeline itself didn't exist before this fork) and every player-castable spell (to give
full context on where the 10 new ones sit power-wise). It is not a full T4C wiki.

## Structure

- `index.html`, `app.css`, `app.js` — the site itself (vanilla JS, no framework, no
  external dependencies — hash-routed single page app).
- `data.js` — generated; everything `app.js` reads, bundled as `window.T4C_DATA`.
- `data/*.json` — the same data as separate files, for anyone who wants raw JSON instead of
  the bundle.
- `data/maps/*.png`, `data/maps.json` — generated zone map images and their NPC/monster/
  landmark pin coordinates; see "Zone map images" above.
