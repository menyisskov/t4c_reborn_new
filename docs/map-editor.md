# T4C Map Editor

## Start on Windows

Install Java 21 and Maven, then double-click `editor.bat` in the repository root. The launcher builds the current sources, prepares the dependency classpath, and starts `com.perso.T4C.MapEditor`. Run it from this checkout so it can find `assets/` and save edits here.

The editor opens the world map by default, or the last map recorded in `editor_camera_position.json`. Use the **Map** menu to switch among discovered `.mapbin` files. Use **File > Save** to save, and **File > Exit** to run the editor's save flow before closing.

## What you can edit now

- Terrain and ground tiles, including fill and transition tools.
- Decor sprites such as walls, trees, and chest art, with offsets, depth, and collision rules.
- Collision tiles, music zones, monster placements, and NPC placements.
- Existing sprite, monster, NPC, and object catalogs through the editor's pickers and panels.

Use **File > New Map Draft...** to give a name to a copy of the current map. The copy appears under `assets/maps/drafts/<name>/` and includes its decor, collision, and music layers. You can then edit the copied terrain and placements independently. Monster and NPC placements on a draft are saved as `<name>.monsters.json` and `<name>.npcs.json` in that folder.

The running game currently registers four map layers in `MapDefinition`: world map, dungeon, cavern, and underworld. A new draft is editable in the editor but does not become a playable game layer until the game is wired to load it and assigned a layer ID. Draft spawn JSON is for authoring; runtime spawn definitions for registered layers still come from Java `@Spawn` annotations. NPC dialogue and quest flags are authored in the game's NPC and quest code; the map editor does not edit those yet. The editor's teleport and object-position panels also do not currently persist new runtime definitions.

## Avalon sanctuary and Wilds

The rebuilt sanctuary is on the world map around **(4040, 1477)**. Elder Ophira is inside the temple at **(4044, 1462)**. The Wilds paths lead to Stalker clearings at **(3950, 1345)** and **(4000, 1390)**, the Warden grove at **(3905, 1400)**, and Caradoc's clearing at **(3965, 1460)**. This is the first rebuilt section; the rest of Avalon still needs further map design.

`AvalonSanctuaryBuilder` records the original building polygons, terrain layout and routes. It defaults to validation without saving. Passing `--apply` rebuilds this section and overwrites subsequent manual edits within its documented mask, so keep manual work in a draft or commit it before regenerating.

For a scenery preview using the game's terrain, decor and object renderer, run this from the repository root after compiling:

```powershell
$deps = (Get-Content target/classpath.txt -Raw).Trim()
java -cp "target/classes;$deps" com.perso.T4C.tools.MapSceneCapture 3990 1435 110 115 0.7 target/avalon-preview.png
```

The capture uses a hidden OpenGL window and omits characters. Its arguments are tile X, tile Y, width, height, image scale and output PNG. Collision/quest tests complement the scenery preview; a capture alone does not verify gameplay.


## Enlarged world and coordinate compatibility

The world map is now **5120×3072**. Avalon is in the eastern extension, shifted **+2700 X**
from its former coordinates. All original world positions retain their X/Y values; the
separate dungeon, cavern and underworld maps retain their original dimensions. Copying or
moving terrain does not automatically move NPC/monster annotations, quests, objects or
travel endpoints. Update those explicitly and validate them together.

Travel links have a source and destination `(X,Y,layer)`. A move only changes endpoints that
belong to the moved area. The existing Library stairs and cave links remain at their original
coordinates. Old Avalon-overwritten terrain was restored from its pre-Avalon baseline.

The one-time save migration uses `avalon-legacy-tiles.bin`, an audited ownership mask. It
moves identifiable old Avalon player positions and bound respawns independently; overlapping
original interior tiles are deliberately excluded. If an old save was in an ambiguous overlap,
it remains at the original position; use unlocked Avalon travel to reach the new island.
Do not run older game builds against saves written by the enlarged-world version.

After editing eastern ground, regenerate the in-game overview and website map crops:

```powershell
$deps = (Get-Content target/classpath.txt -Raw).Trim()
java -Xmx3g -cp "target/classes;$deps" com.perso.T4C.tools.WorldOverviewExporter
java -Xmx3g -cp "target/classes;$deps" com.perso.T4C.tools.MinimapExporter compendium/data
java -cp "target/classes;$deps" com.perso.T4C.tools.CompendiumExporter compendium/data
```

The expansion adds 67% more world tiles. Memory and loading costs increase with the dense
terrain arrays; this does not enlarge the other map layers.


`AvalonWorldExpansion` is the one-time historical migration tool, retained for auditing.
It compares commits `21273765`, `2d804017` and `d18b1772` against their parents, restores
only their affected old-world fields, and requires the unexpanded source map. It refuses to
run on the already enlarged map. `--apply` writes the result; without it, it only validates.
Normal editing uses the editor and the shifted sanctuary/terrain authoring tools.
