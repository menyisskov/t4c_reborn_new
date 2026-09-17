---
name: graphic-designer
description: Deep technical guide to T4C Reborn's LibGDX rendering and asset pipeline — texture/sprite atlas conventions, camera/coordinate rules, and the in-repo map editor. Use this whenever the user wants to add new sprites/art, new animations, a new map, a new location/zone, new monster or NPC visuals/item icons, or work with the game's in-engine editor or expansion map tooling. Touches assets/sprites/**, assets/maps/**, assets/items/*.json, assets/monsters/*.json, src/main/java/com/perso/T4C/render/**, src/main/java/com/perso/T4C/mapping/**, src/main/java/com/perso/T4C/screens/MapEditorScreen.java.
---

# Graphic Designer

Technical guide for graphics work on T4C Reborn (LibGDX recreation of *The 4th Coming*). This is
the deep-pipeline skill: read it before touching any sprite, map tile, monster animation, or the
in-repo editor. It inherits `AGENT.md`'s charter — production-ready output only, no invented
behavior, verify against the actual code/assets instead of assuming.

`AGENT.md` at the repo root is the canonical source for engine rules but its "Rendering & Engine
Constraints" section is truncated in this checkout (it stops mid-sentence after announcing the
inverted-Y camera, with no further detail committed). The rule itself is real and verified against
source below — treat this file as the complete version of that missing detail, and re-check
`AGENT.md` for anything new if it's ever filled in further.

## 1. Engine & rendering fundamentals — non-negotiable rules

- **Camera is Y-down.** `MainGameScreen` builds it as `camera.setToOrtho(true, WINDOW_WIDTH,
  WINDOW_HEIGHT)` — the `true` is LibGDX's `yDown` flag. World Y increases **downward**, opposite
  of stock LibGDX/OpenGL y-up convention. Any offset math you write (sprite draw offsets, decor
  z-ordering, click-to-world conversions) must respect this; do not assume y-up.
- **Tile size is 32×16** (`GameConstants.GRID_W = 32`, `GRID_H = 16`). World pixel position of a
  tile is simply `worldX = tileX * 32`, `worldY = tileY * 16` — no diagonal/isometric stagger math;
  the "isometric" look comes entirely from how the 32×16 ground brick sprites are drawn, not from
  coordinate transforms. `SpriteLoader.Sprite.isGround()` treats any 32×16 sprite as a ground tile.
- **Window is 1280×768** (`GameConstants.WINDOW_WIDTH/HEIGHT`).
- **Sprites are NOT a LibGDX `TextureAtlas`.** There is no `.atlas`/`.pack` file. Every sprite is
  its own PNG, packed into a custom binary container (`sprites_0.bin`, `sprites_1.bin`,
  `sprites_2.bin` under `assets/sprites/`) and decoded into its own `Texture`/`TextureRegion` on
  first use, cached by `SpriteLoader` (`src/main/java/com/perso/T4C/helper/SpriteLoader.java`).
  Textures use `Nearest` filtering + `ClampToEdge` — keep new art pixel-art-crisp, no anti-aliased
  edges that would look wrong un-filtered.
- **Every sprite is looked up by name, case-insensitively**, via
  `SpriteLoader.getRegionFromSpriteName(String)`. There is no numeric/index addressing anywhere in
  game code — the name *is* the contract between a JSON/Java definition and the art.
- **Draw offsets travel with the sprite, not the caller.** Each packed sprite entry carries
  `offset1X/Y` and `offset2X/Y` (for normal vs. mirrored draw) alongside width/height/type/PNG
  bytes. `SpriteOffsetUtil.selectOffset(offset1, offset2, flipX, fallback)` picks the pair to use.
  When you add a new sprite you must supply correct offsets or it will draw off-grid.
- **No mip-mapping/atlas packing concerns** — each region is its own texture, so draw-call batching
  is by texture identity, not atlas page. Large sprite counts on screen do cost more draw calls;
  this is a known tradeoff in the existing architecture, not something to "fix" in new art.

## 2. How a sprite name resolves to a pixel — traced end to end

Example: an item JSON references `"appearanceEquippedPrimary": "PupPlateBody"` and
`"appearanceInventory": "64kInvPlateArmorSleeves"` (see `assets/items/ancient_celestial_air_armor.json`).

1. `ItemJsonDef` (Gson-deserialized) carries those two fields verbatim into `ItemDefinition`.
2. UI/render call sites (`PlayerHUD`, `Inventory`, `GuiInventory`, `GroundItem`,
   `MonsterPuppetDress`/`DataMonster` for equipped monster puppets, etc.) call
   `def.getAppearanceInventory()` / `def.getAppearanceEquippedPrimary()` and pass the **string**
   straight to `SpriteLoader.getRegionFromSpriteName(name)`.
3. `SpriteLoader` has already loaded every shard of `assets/sprites/sprites_*.bin` at startup
   (`loadSpriteBin`), built a `name.toLowerCase() -> id` map from every packed entry, and lazily
   decodes the embedded PNG bytes into a `Texture`/`TextureRegion` the first time that id is drawn.
4. Result: `"PupPlateBody"` and `"64kInvPlateArmorSleeves"` are **not files anywhere in the repo**
   — they are named entries packed inside `sprites_0/1/2.bin`. `grep`/`find` for the name across the
   filesystem will find nothing; the only way to confirm a sprite name exists is to check it against
   the loaded `SpriteLoader` (the in-repo editor's Item/Monster preview panels and Sprite Picker do
   this live — see §6).
5. There is also a **fallback path** keyed by the item's numeric `appearanceId`:
   `ItemIconRegistry.iconFor(appearanceId)` → `ItemIconDefinitions.all()`, a large static
   `Map<Integer, String>` (e.g. `264 -> "64kIconArmor"`) hand-curated from the original game's
   appearance-id space. This is used where only a numeric id is known and no explicit sprite name
   was set; a new item should normally just set `appearanceInventory`/`appearanceEquippedPrimary`
   directly and can leave `appearanceId` as a secondary/legacy hint.

The same by-name resolution is used for ground/decor tiles (`GroundRenderer`, `DecorRenderer`),
map objects (`ObjectRenderer`, via `ObjectMapping.sprite`), and spell VFX (`SpellRenderer`, which
also loads `assets/shaders/spellmask.vert/frag` and `outline.vert/frag` for masked/outlined
effects) — always a sprite-name string flowing into `SpriteLoader`.

## 3. Where new art goes, and how to make a definition find it

**There is currently no wired-up tool to pack a brand-new PNG into `sprites_N.bin`.**
`SpriteBinWriter.merge(...)` (`src/main/java/com/perso/T4C/helper/SpriteBinWriter.java`) is the API
that *would* do this (writes packed entries with name/dims/offsets/PNG), and
`SpriteBinIO`/`SpriteBinWriter` fully support it, but nothing in the current codebase calls
`SpriteBinWriter.merge`. Practically, to add new sprite art today you have two honest options:

1. **Preferred / verifiable:** write a small one-off script (Java, using
   `SpriteBinWriter.Entry`/`SpriteBinWriter.merge`, or Python replicating the format documented in
   `references/sprite-bin-format.md`) that appends your new PNG(s) as named entries to the existing
   shard(s) (or a new `sprites_3.bin` shard — `SpriteLoader.resolveShardHandles` auto-discovers
   shards by the `sprites_N.bin` naming convention, so a new shard index is picked up with no code
   change). Reuse an **existing, unused sprite name** if you are re-skinning something, or pick a
   **new unique name** following the game's existing conventions (see below) if adding new content.
   Verify the sprite loads via the in-repo editor's Sprite Picker (F-key `SPRITE_PICKER` mode) or
   Item/Monster preview panels before wiring a definition to it — that is the fastest way to confirm
   a name actually resolves.
2. **Legacy extraction (only if the original DDA archives are available):** `DdaToSpriteBin.main`
   (`src/main/java/com/perso/T4C/helper/DdaToSpriteBin.java`) drives `DdaExtractor` to read a
   `v2datai.did`/`v2colori.dpd`-indexed DDA library from `Paths.DDA_DIR` (`assets/dda/`) and merge
   matching sprites into a target `.bin`. **`assets/dda/` does not exist in this repo** — this
   pipeline already ran once, historically, to produce the committed `sprites_*.bin`, but it cannot
   run again in this checkout without externally supplying that DDA source tree. Do not assume it
   works; verify `assets/dda/` exists before relying on it.

**Naming conventions to match (so a definition finds your art without extra glue code):**

- **Item icons:** `64kIcon<Category>` (inventory-slot list icon, small), `64kInv<Description>`
  (bigger inventory art), and equip-slot base names that vary by armor material/style (e.g.
  `PupPlateBody`, `PupLeatherBody` — "Pup" = puppet/paperdoll equipped-body render). Look at
  sibling items of the same category in `assets/items/*.json` for the exact prefix pattern before
  inventing a new one.
- **Ground mosaics:** template strings with `(&x, &y)` placeholders, e.g. `"64kNormalGrass (&x, &y)"`
  — see §4. A new ground type needs a full N×M grid of individually named frame sprites matching
  that template, not a single image.
- **Decor/object sprites:** free-form display names matching the original client's naming, e.g.
  `"Fruits 1"`, `"Misc 4 - Part 1 6"`, `"Chest"` — set as `ObjectMapping.sprite` in
  `ObjectMappingDefinitions.java` (§4).
- **Monster/NPC animation base names:** `{Base}#{letter}` (walk), `{Base}A#{letter}` (attack),
  `{Base}C!{letter}` (death) — see §7 for the full encoding and the audit tool that checks it.

**Cross-reference the content-authoring skills for where appearance fields plug into game data**
(these own the JSON/Java schema; this skill owns making the *art itself* resolve correctly):
- `.claude/skills/item-creator/SKILL.md` — item `appearanceEquippedPrimary`/`appearanceEquippedSecondary`/`appearanceInventory`/`appearanceId` fields, armor-set color/style conventions.
- `.claude/skills/spell-creator/SKILL.md` — spell VFX sprite/impact/projectile naming for `SpellRenderer`.
- `.claude/skills/npc-monster-creator/SKILL.md` — monster `walkPattern`/`attackPattern`/`deathPattern` fields (JSON) or the equivalent Java `MonsterDef` constructor args.
  (If any of these don't exist yet on disk, they're being authored in parallel per `game-director`'s
  own note — proceed by reading the underlying JSON/Java yourself; don't block on the file existing.)

## 4. Map/tile mapping-rule files (`src/main/java/com/perso/T4C/mapping/definition/`)

- **`GroundMosaicDefinitions.java`** — maps a legacy ground-type code (hex string key, from the
  original map format's terrain-type byte, e.g. `"0x1"`, `"0x11"`) to a `Definition(id, width,
  height, frames)`: a `width × height` grid of sprite-name templates using `&x`/`&y` placeholders
  (e.g. `"64kNormalGrass (&x, &y)"` at 13×15). This is the ground **autotile mosaic** table —
  it's how a single terrain-type byte in the map binary expands into a tiled field of individually
  named 32×16 ground sprites. Extending/adding a ground type means adding a new keyed `Definition`
  here *and* having every `(x,y)` frame it references actually exist as a packed sprite.
- **`DecorLayerRuleDefinitions.java`** — currently **empty** (`Set.of()`). It's consumed as
  `flaggedDecorNames` by `DecorRenderer`/`MapEditorScreen`'s Decor Layer Rule editor (a rule
  toggle per decor sprite name), but no rules are defined yet in this codebase — treat it as an
  unused hook, not a populated system to study for examples.
- **`ObjectMappingDefinitions.java`** — maps an object **key** (e.g. `"BATTLE CHEST 1"`) to an
  `ObjectMapping(legacyId, sprite, clickAnimate, mirror, animateSound, reverseAnimateSound,
  alwaysBehindEntities, displayName/localeKey, depthTileOffsetY)`. This is the catalog of
  placeable interactive/decorative world objects (chests, furniture, misc props) and how each
  resolves to a sprite name, click-animation, sound, and localization key. Adding a new
  placeable object type means adding an entry here.
- **`ObjectPositionDefinitions.java`** — a large static `List<ObjectPos(name, x, y, z, mirror)>`:
  the **actual placement** of every object-mapping instance across the maps (tile coordinates +
  which map, via `z`: 0=worldmap, 1=dungeon, 2=cavern, 3=underworld — see §6). This is literally
  the world's prop layer, stored as Java source rather than map-binary data.

## 5. Legacy `assets/origin/*` vs. the live map/sprite format — verified conclusion

`assets/origin/` contains original T4C client files: `CavernMap.Map`, `DungeonMap.Map`,
`Map1.Dat`–`Map3.Dat`, `map4.dat`, `maps.dat`, `NPCs.WDA`, `T4C Worlds.WDA`, **`T4C Edit.WDA`**,
several `NPC *.dll` files, `SpellEffects.dll`, `Skills.dll`, `t4c.exe`, and numbered
`t4cgamefile*.vsf`/`.vf` archives.

**Verified: none of these files are read by any loader in `src/main/java`.** A repo-wide search for
every filename in `assets/origin/` and for the literal path `assets/origin` across all Java source
returns zero hits. They are reference/archival material only — most likely kept so a human or a
future porting pass can extract data from them, not something the running game or editor touches.
`T4C Edit.WDA` in particular is almost certainly a saved-data file from the **original T4C map
editor** the user is thinking of ("T4C Edit") — its presence is good evidence that tool existed and
produced this file, but nothing in this codebase can open or read it today.

The one legacy-extraction pipeline that *does* exist in code (`DdaExtractor` /
`DdaToSpriteBin`, §3) expects a **different, specific format**: a `v2datai.did` + `v2colori.dpd`
indexed DDA sprite library under `assets/dda/`, which is not present in this repo and is not the
same format as the `.dll`/`.Map`/`.Dat`/`.WDA` files sitting in `assets/origin/`. So even the one
working legacy-import code path cannot currently consume `assets/origin/`'s contents as-is.

**The live, actually-loaded map format** is the one under `assets/maps/`: per-map
`<name>.mapbin` (tile/ground data), `<name>.colbin` (collision), `<name>.decorbin` (decor layer),
`<name>.musiczones.bin` (music zone regions), for `worldmap/`, `cavern/`, `dungeon/`,
`underworld/`, plus a separate `rt/rt_map_125.dat` + `rt/zone_map.dat` pair (paths wired in
`Paths.java`, purpose not further traced here — treat as a distinct, currently-referenced map
asset if you touch it). **Practical conclusion: build/extend maps against the live `.mapbin`/
`.colbin`/`.decorbin` format via the in-repo editor (§6); don't attempt to load or convert
`assets/origin/*` directly — no code path supports it today.**

## 6. The in-repo editor — what it is, how to launch it, what it really supports

`MapEditorScreen` (`src/main/java/com/perso/T4C/screens/MapEditorScreen.java`, ~18k lines) is a
full in-engine level/content editor, launched via a **separate main class**,
`com.perso.T4C.MapEditor` (`src/main/java/com/perso/T4C/MapEditor.java`) — **not** the normal game
entrypoint (`com.perso.T4C.MyGame`, run by `run.sh`/`run.bat`/`launch.bat`). To launch it, build the
classpath the same way `run.sh` does, then run the editor main class instead:

```bash
mvn -q dependency:build-classpath -Dmdep.outputFile=target/classpath.txt   # once, if target/classpath.txt is missing
java -cp "target/classes:target/natives:$(cat target/classpath.txt)" com.perso.T4C.MapEditor
```

(Windows: same idea with `;` separators and `target\classpath.txt`, mirroring `run.bat`.) It opens
its own 1280×768 window titled "T4C Map Editor", loads `Paths.MAP` (`assets/maps/worldmap/worldmap.mapbin`)
by default, and remembers camera position/zoom across launches in `editor_camera_position.json`
(repo root).

**Confirmed working today** (wired to a menu item or keybinding — see
`references/editor-reference.md` for the full menu tree and every F-key):
- Ground tile placement/painting, sprite picker, autofill/mosaic tooling, "Rebuild Tmpl1/3/4"
  smoothing passes (`F5`/`F6`/`F7`, Tools menu).
- Decor visibility toggle and paint (`F2`).
- Object visibility toggle (`F3`) and the **Object Position Editor** (a dedicated mode) for
  moving/placing existing `ObjectPositionDefinitions` entries by clicking tiles.
- Collision-type painting (`F4`, with a full `CollisionType` picker in the View menu).
- Music-zone region editing (`F10`).
- Teleport editing (`F5` normally toggles the overlay; the teleport editor mode lets you add/edit
  teleport links between maps, including a "Go to Coordinates…" jump).
- **Monster and NPC spawn placement** (`F8` opens the monster picker, `F9` opens the NPC picker):
  pick an existing monster/NPC type and click-place a spawn point on the map. **This does persist**
  — saving spawns calls
  `SpawnJavaExporter.export(...)`, which rewrites the `@Spawn(...)` annotations on the
  corresponding monster/NPC Java class on disk.
- "New Map" (File menu) — see the important caveat below.
- Live sprite hot-reload ("Reload Sprites" in the File menu) so newly packed sprites show up
  without a full relaunch.

**Two honest gaps — do not overclaim these:**
1. **"New Map" does not create a blank map.** `createNewMap()` literally
   `Files.copy(currentMapFile, "NewMap_<timestamp>.mapbin")` — it **duplicates the currently open
   map's binary data** into a new file and switches to editing the copy. There is no blank-canvas /
   width-height picker. In practice: either (a) carve a brand-new **location** out of unused tile
   space inside the existing `worldmap` grid (it's one large contiguous coordinate space — the
   default camera position in `editor_camera_position.json` is already at world pixel
   (91103, 17859), i.e. roughly tile (2847, 1116), showing how large the used area is), which is
   the realistic "add an expansion zone" path and needs **no source-code change**; or (b) clone an
   existing map via "New Map" and paint over it as a genuinely separate instanced map — see next
   point for what that additionally requires.
2. **Only 4 maps are recognized by the running game**, hardcoded in
   `com.perso.T4C.config.MapDefinition` (`WORLDMAP=0, DUNGEON=1, CAVERN=2, UNDERWORLD=3`), each
   with its `.mapbin`/`.colbin` path wired in `Paths.java`. Wiring in a genuinely new 5th map (not
   just a new area of an existing one) requires **adding a new enum constant + `Paths` constants
   and recompiling** — the editor alone cannot register a new map for the live game to load, only
   author the tile data for one you've already wired in.
3. **Object placement in the Object Position Editor does not persist.** Unlike monster/NPC spawns,
   `saveObjectPositions()` only calls `mapRenderer.reloadObjectPositions()` and shows a "saved"
   message — there is no `ObjectPositionJavaExporter` (items, spells, and spawns all have a
   `*JavaExporter`; object positions do not). Placements made in a live editor session are
   **session-only** unless you additionally hand-edit `ObjectPositionDefinitions.java` yourself to
   add the `ObjectPos(...)` entries.
4. **A full Item Editor, Monster Def Editor, Spell Editor, Object Mappings Editor, and Clan
   Relations Editor are fully implemented as UI classes inside `MapEditorScreen`** (`ItemEditorUI`,
   `MonsterDefEditorUI`, `SpellEditorUI`, `ObjectMappingsEditorUI`, `ClanRelationsEditorUI`, each
   with list/add/delete/copy-paste and, for items, live sprite previews via
   `drawSpritePreview`/`resolveItemPreviewSprite`) — **but their `open*Editor()` methods are never
   called from anywhere else in the file.** No menu entry, no keybinding, no button currently
   triggers `openItemEditor()`/`openMonsterDefEditor()`/`openSpellEditor()`/
   `openObjectMappingsEditor()`/`openClanRelationsEditor()`. Treat these as built-but-unwired —
   don't tell a user "open the Item Editor from the editor UI" as if it's reachable today; if this
   matters, the fix is adding a menu entry/keybinding for the relevant `open*Editor()` call, not
   assuming one already exists. Until that's wired, item/monster/spell content is authored by
   editing `assets/items/*.json` / `assets/monsters/*.json` / the Java definitions directly (owned
   by the sibling `item-creator`/`spell-creator`/`npc-monster-creator` skills).

This confirms the in-repo `MapEditorScreen` is the practical successor to the original "T4C Edit"
map editor for **terrain, decor, collision, music zones, teleports, and spawn placement** — but it
is not (yet) a drop-in replacement for the original tool's full expansion workflow (new blank maps,
in-editor item/monster authoring). See `references/editor-reference.md` for the complete menu tree
and keybinding table if you need to drive it precisely.

## 7. Monster/NPC animation conventions and the audit tooling

**Animation pattern encoding** (seen throughout `assets/monsters/*.json` and the 399 individual
Java monster classes in `src/main/java/com/perso/T4C/monster/*.java`, e.g. `r269WaspDrone.java`):

- `walkPattern`: `"{Base}#{letter}"` — e.g. `"GiantWasp#h"`. `#` marks an angled (multi-direction)
  cycle; the letter encodes the frame/angle count as `chr('a' + n - 1)` (so `h` = 8, `k` = 11, …).
  The actual packed sprite names this expands to look like `"GiantWasp000-a"`,
  `"GiantWasp045-a"`, … (angle in degrees + frame letter).
- `attackPattern`: `"{Base}A#{letter}"` — same encoding, base name suffixed with `A`.
- `deathPattern`: `"{Base}C!{letter}"` — suffixed with `C`; `!` marks a **non-angled** (single
  direction) sequence rather than `#`'s multi-angle one.
- The 11 newer monsters in `assets/monsters/*.json` use this directly as JSON string fields; the
  399 legacy monsters are Java classes returning `MonsterDef(...)` with the same three
  strings positionally among its constructor args (see `r269WaspDrone.definition()`).

**`scripts/audit_monster_anims.py`** cross-checks a monster's `walk/attack/death` pattern strings
against (a) the actual sprite names present in `assets/sprites/sprites_*.bin` and (b) the original
client's `VisualObjectList::LoadObject`/`ObjectListing.h` C++ source, to catch monsters whose
patterns don't match what the original appearance-id would have used. **Caveat, stated plainly:**
the script hardcodes Windows paths to an external reference checkout —
`c:\T4C\t4c_client_linux_ref\client\T4C Client\VisualObjectList.cpp` and `ObjectListing.h` — that
does **not exist in this repository**. It will not run as-is in this checkout; running it for real
requires first obtaining that reference client source tree at the expected path (or editing the
script's `VOL`/`LISTING` constants to point at wherever you keep it). When you *do* have that
reference tree, run it after adding/editing a monster to catch a walk/attack/death pattern whose
base name or frame-count letter doesn't match what the original client shipped, before assuming
the animation is complete.

**`monsters_report.csv`** (repo root) is a full stat/balance export (name, level, health, damage,
gold, stat block, dodge, AC, aggro, tameable, respawn time, loot list) covering essentially every
monster. **Caveat, stated plainly:** despite being grouped with the animation-audit tooling by the
task that produced it, its columns contain **no sprite/animation/appearance fields at all** — it is
a general content/balance audit export, not a graphics-completeness report. Don't rely on it to
tell you whether a monster's art is complete; use `audit_monster_anims.py` (once you have the
reference tree) or manual verification against `SpriteLoader`/the editor's sprite picker for that.

## 8. `t4cfantasy.com/Addon` — could not be checked

This environment's egress proxy explicitly blocks `t4cfantasy.com` (and `www.t4cfantasy.com`) —
both fetch attempts returned `EGRESS_BLOCKED`, not a timeout or a content-empty page. No facts
from that site could be verified for this skill. If you have another way to reach it (or the user
pastes its content), fold it in; don't assume anything about the original addon/expansion tooling
or art specs it may document beyond what's verified above from the codebase itself.

## 9. Checklists

### Add one new item icon (existing item, new/re-skinned art)
1. Get the new artwork as a PNG at the correct native size for its use (inventory icon vs.
   equipped-body sprite have different conventional sizes — check a sibling sprite of the same kind
   via the editor's sprite picker/preview for the size and offset values to match).
2. Pack it into `assets/sprites/sprites_N.bin` under a name following §3's convention (new shard is
   fine — `sprites_3.bin` auto-discovers), with correct `offset1/offset2` draw offsets.
3. Point the item's `appearanceInventory`/`appearanceEquippedPrimary` JSON field (see
   `item-creator` skill for the JSON schema) at the exact sprite name, case doesn't matter but spell
   it identically to the packed entry's `name`.
4. Launch the game (or editor) and hot-reload sprites ("Reload Sprites" in the editor, or restart
   the client) — confirm the icon renders in inventory/equipped preview, not just that it "should".
5. If this is a new appearance style with no existing `appearanceId` mapping and you want the
   numeric-id fallback to also work, add an entry to `ItemIconDefinitions.all()`.

### Add a new expansion map/location (realistic, given verified tooling)
1. Decide: is this a **new area within an existing map** (no code change, works today) or a
   **genuinely new separate map** (requires a `MapDefinition` enum + `Paths.java` change and a
   recompile)? Default to the former unless there's a strong reason (e.g. it needs its own
   instance-style isolation like the dungeon/cavern/underworld already have).
2. Launch `com.perso.T4C.MapEditor` (§6). For a new area: navigate to unused tile space in
   `worldmap` (or the target map). For a genuinely new map: add the enum/`Paths` entries and
   recompile first, then use "New Map" to clone an existing map's file as your starting point (it
   will not be blank — expect to paint over/clear existing content).
3. Paint ground tiles (ensure the terrain type's `GroundMosaicDefinitions` mosaic and every frame
   sprite it needs already exist, or add new ones per §4 first).
4. Paint decor, set collision types, and place any interactive objects — **for objects, hand-edit
   `ObjectPositionDefinitions.java` afterward**, since the editor's object placement doesn't
   persist (§6 gap #3).
5. Place monster/NPC spawns via the monster/NPC picker (`F8`/`F9`) — this does persist via
   `SpawnJavaExporter`.
6. Add teleport links in/out of the new area via the Teleport editor mode.
7. Save (File → Save, or exit-save flow) and verify by launching the normal game client
   (`run.sh`/`run.bat`) and walking into the new area — the editor and the live game read the same
   `.mapbin`/`.colbin`/`.decorbin` files, so an editor save should be immediately visible in-game.
