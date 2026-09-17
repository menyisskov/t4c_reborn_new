# MapEditorScreen — full menu tree and keybindings

Source: `src/main/java/com/perso/T4C/screens/MapEditorScreen.java`. Line numbers below are
approximate to this snapshot of the file (~18.2k lines) and will drift as the file changes —
re-grep if something doesn't match.

## Menu bar (`buildEditorMenus()`, ~line 13157)

- **File**: New Map (clones current map file — see SKILL.md §6 gap #1) · Save (writes all dirty
  editor state) · Reload Sprites (hot-reloads `SpriteLoader` from the packed `.bin` shards without
  restarting) · Exit (goes through an exit-save confirmation screen).
- **Edit**: Undo · Copy · Paste (tile clipboard, `Ctrl+C`/`Ctrl+V`/`Ctrl+Z`/`Ctrl+W` also work as
  keyboard shortcuts).
- **View**: Decorations toggle · Objects toggle · Collision Editor toggle · Collision Type submenu
  (one entry per `CollisionType` enum value, shows value + display name) · Music Zones toggle ·
  Teleport overlay toggle.
- **Tools**: Autofill (Toggle / Recalculate Map / Repair Missing Ground) · Rebuild Tmpl3 (Whole
  Map / Viewport) · Rebuild Tmpl1 (Whole Map / Viewport) · Rebuild Tmpl4 (Whole Map / Viewport) —
  these rebuild the ground-mosaic smoothing/blend tile layers from the underlying terrain data.
- **Map**: one entry per map file discovered under `Paths.MAPS_DIR` (`assets/maps/`), select to
  switch the editor's active map.
- **Teleport**: "Go to Coordinates…" — jump the camera to an arbitrary tile.

## Keybindings (`keyDown`, ~lines 1839–2143)

| Key | Action |
|---|---|
| `F2` | Toggle decor visibility |
| `F3` | Toggle object visibility |
| `F4` | Toggle Collision Editor mode |
| `F5` | Toggle teleport overlay (normal); `Shift+F5` force-regenerates the Tmpl3 smoothing tiles at the current tile |
| `F6` | Toggle autofill |
| `F7` | Recalculate viewport ground textures |
| `F8` | Open monster picker → place monster spawns (persists via `SpawnJavaExporter`) |
| `F9` | Open NPC picker → place NPC spawns (persists via `SpawnJavaExporter`) |
| `F10` | Toggle Music Zone Editor mode |
| `F11` | Toggle fullscreen (`DisplayModeToggle.toggle()`) |
| `F12` | Repair missing ground under decors |
| `Ctrl+Z` / `Ctrl+W` | Undo |
| `Ctrl+C` / `Ctrl+V` | Copy / paste tile clipboard |
| Arrow keys | Nudge selection / pan, context-dependent on `EditorMode` |
| `Delete`/`Forward Delete` | Delete selected spawn/decor/object depending on mode |
| `Escape` | Close current picker/dialog or return to `SELECT_TILE` mode |
| `M` | Context-dependent (spawn selection clearing in some modes — check line ~2029 in source) |

## `EditorMode` enum (~line 473)

```
SELECT_TILE, SPRITE_PICKER, COLLISION_EDITOR, MUSIC_ZONE_EDITOR, TELEPORT_EDITOR, OBJECT_POSITION_EDITOR
```

Note there is **no `SPAWN` mode** in this enum — monster/NPC spawn placement is driven by the
separate `entityPicker` (`EntityPickerUI`) overlay opened via `F8`/`F9`, layered on top of
whatever `EditorMode` is currently active, not a mode of its own.

## Built-but-unwired editor panels

These classes are fully implemented (list/add/delete/copy/paste, form fields, and for items a live
sprite preview) but their `open*Editor()` methods (~lines 13268–13290) have **zero call sites**
anywhere else in the file as of this snapshot:

- `ItemEditorUI` via `openItemEditor()`
- `MonsterDefEditorUI` via `openMonsterDefEditor()`
- `SpellEditorUI` via `openSpellEditor()`
- `ObjectMappingsEditorUI` via `openObjectMappingsEditor()`
- `ClanRelationsEditorUI` via `openClanRelationsEditor()`

To make any of these reachable, add a `MenuItem` to `buildEditorMenus()` (e.g. a new "Content" menu
title) or a keybinding in the `keyDown` handler that calls the corresponding `open*Editor()`
method — the panels themselves render and behave correctly once opened programmatically (confirmed
by reading their `render`/`handleClick` implementations), they simply have no current entry point.

## Sprite `.bin` container format (as implemented by `SpriteBinIO`)

Not a standard format — custom to this project. Per shard file (`sprites_N.bin`):

- Magic `"T4CBIN"` prefix, then a zlib-compressed payload.
- Decompressed payload: 4-byte magic `"T4C1"` region, then `version:uint32`, `count:uint32`
  (big-endian ints per `struct.unpack_from(">II", ...)` in `scripts/audit_monster_anims.py`'s
  parser — treat that script's `load_sprite_names_fixed()` as a working reference reader).
- Per entry: `nameLen:uint32`, `name:utf8[nameLen]`, then 8 big-endian `uint32`s
  (`width, height, off1X, off1Y, off2X, off2Y, type, pngLen`), then `png:byte[pngLen]` — raw PNG
  bytes, decoded directly into a `Pixmap`/`Texture` by `SpriteLoader`.
- `type` is `0` if `width==32 && height==16` (a ground tile) else `1` (`SpriteBinIO.spriteType`).
- Sprite names containing `"Shd"` are shadow sprites and are treated specially by some tooling
  (excluded from the animation-audit name set, for example) — don't assume every packed name is a
  "real" visible sprite meant for direct reference.
- `SpriteLoader.resolveShardHandles` looks for `sprites_0.bin`, `sprites_1.bin`, … in order,
  stopping at the first missing index; if no numbered shards exist it falls back to a single
  legacy `sprites.bin`. This means a new shard just needs the next sequential index to be picked
  up automatically — no manifest file to edit.
