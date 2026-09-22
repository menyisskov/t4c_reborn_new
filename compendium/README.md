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
ones).

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
java -cp "target/classes:$(cat /tmp/cp.txt)" com.perso.T4C.tools.CompendiumExporter compendium/data
```

This overwrites `compendium/data/*.json` and `compendium/data.js`.

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
