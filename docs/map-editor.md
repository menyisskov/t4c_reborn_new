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
