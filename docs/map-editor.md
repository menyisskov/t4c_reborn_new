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

## Build workspace

The build sidebar opens by default. Press **B**, or use **Tools > Build workspace (B)**, to hide or reopen it. Drag its left edge to resize it. Search matches sprite identifiers, readable names, category, material and facing. Use **Families** to group variants, **Variants** to show every matching sprite, and the arrow buttons or **R** to cycle the selected family. Favorites and the 24 most recently chosen sprites persist locally. Names appear above thumbnails; hovering and selecting reveal more detail.

| Tool | Action |
| --- | --- |
| Stamp | Choose Stamp for one copy per click or drag. |
| Paint | Selecting an asset activates Paint. Hold the left mouse button and drag across the map for multiple copies. You can also drag from the asset thumbnail into the map and keep dragging; painting starts where the pointer first enters the canvas. A click or quick drop places one copy. |
| Line | Drag between two points to repeat the selected sprite. Spacing controls the interval. |
| Brick wall | Drag a straight wall; it snaps to a map diagonal and uses the matching brick facing. |
| Brick room | Drag between opposite corners to create four connected brick walls with corner pieces. |
| Capture | Enter a template name, then drag a rectangle over existing terrain to save a reusable template. |
| Template | Choose a template, then drag it onto the map or click to place it. |
| Select | Return to the existing map selection and editing controls. |

A green preview is valid; red means the gesture extends outside the map or would replace existing scenery. **Replace** explicitly allows occupied decor tiles to be overwritten. **Blocking** adds absolute collision to generic sprite placement; brick walls and rooms always block. Turning Blocking off preserves the tile's existing collision, rather than clearing it. Use the collision editor when deliberately opening a doorway. A room outline has no automatic door or floor: add an entrance and floor as part of your design.

Each placement or stroke is one **Undo** operation. **Ctrl+Z** undoes, **Ctrl+Y** or **Ctrl+Shift+Z** redoes while the workspace is active. A new edit clears redo history, and switching maps clears both histories. Right- or middle-drag pans the map; the wheel zooms over the map and scrolls the sidebar over the asset grid. **Escape** cancels a gesture and returns to Select. **Ctrl+S** saves while the workspace is active; **File > Save** works in every mode.

### Multi-select and move scenery

Click **Multi-select (drag a box)** at the top of the sidebar. Drag a rectangle around the base tiles of walls, trees or other scenery, then drag any highlighted tile or selected sprite to move the group. Click a sprite to select just that object. **Shift-drag** adds another rectangle; **Shift-click** adds or removes one object. The sidebar shows the selected count. **Escape** clears the selection and returns to the original Select tool.

Moves snap to tiles and show a colored destination preview. Red means the move is invalid; releasing changes nothing. Releasing over the sidebar or toolbar cancels the move. A group move is one undo step; undo/redo clears the selection so old coordinates cannot move unrelated objects. Ground stays in place, and scenery retains its scale, offsets and depth. Blocking collision (absolute, fly-over or force field) on selected anchors travels with the scenery; water and area rules stay in place. A blocking object cannot overwrite a destination collision or zone rule. Multi-select never replaces unselected scenery, even with Replace enabled. Selection rectangles are limited to 65,536 tiles.

This moves **scenery only**: NPCs, monsters, interactive objects, entrances and travel links stay at their existing coordinates. Use Capture/Template when you want terrain included in a reusable copy. The regular **Select** tool still provides the original individual-object editing controls.

### Reusable templates

The bundled library contains the **Lighthaven temple, cottage, storehouse, underground chamber, and cavern passage**. These are snapshots of existing game scenery with original tile offsets, scale, depth and collision. The cavern passage and underground chamber have open connection points; join them to other sections when building a larger interior. Brick wall/room tools use a vetted Lighthaven brick style. For other wall families, select a sprite and use Line, cycling variants manually where needed.

Templates store relative coordinates and can be placed on any map with sufficient space. Captures include terrain, decor and collision, including empty cells within the selected rectangle. Consequently, Replace can clear existing scenery inside that rectangle. Bundled building templates use polygon masks to avoid copying neighboring structures. **NPCs, monsters, quests, interactive objects and teleport links are not included.** Add those separately so entrances do not accidentally lead to the source building. Copying stairs art does not create a working travel link, and copying chest art does not create a loot container.

Custom captures are saved immediately to `assets/editor/templates/custom/` under unique names; placing a template changes only the current map until you save it. Copy the JSON files to share them or remove them to delete templates, then reopen the Templates tab. Captures are limited to 65,536 tiles. Large custom templates preview their first 8,192 cells plus the full footprint outline; placement and undo always include the complete selection. Local palette preferences are stored in `editor_build_preferences.json`.

The bundled library can be regenerated from the checked-in maps with `com.perso.T4C.tools.EditorTemplateExporter` after compilation. This writes only the bundled template JSON and brick-style metadata, never source maps. Run it from a clean checkout to avoid incorporating unrelated manual map edits.

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

### Repeat-placement regression check

After `mvn test`, run `com.perso.T4C.editor.build.BuildWorkspaceInputSmoke` with `target/test-classes`, `target/classes` and the dependency classpath. It opens a hidden OpenGL editor and verifies palette/canvas repeat dragging, atomic undo/redo, explicit single stamps, quick drops and sidebar cancellation. It does not save map edits and restores palette preferences on exit. Run from a clean checkout with the default palette layout.
