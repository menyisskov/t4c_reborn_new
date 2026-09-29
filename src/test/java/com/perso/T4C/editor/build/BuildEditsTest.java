package com.perso.T4C.editor.build;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.SpriteLoader;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BuildEditsTest {
  @TempDir Path temp;

  private MapReader map() throws Exception {
    var path = temp.resolve("test.mapbin");
    try (var out = new DataOutputStream(Files.newOutputStream(path))) {
      out.writeBytes("T4CMAP");
      out.writeByte(6);
      out.writeByte(0);
      for (int value : new int[] {32, 32, 0, 0, 0, 0, 0}) out.writeInt(Integer.reverseBytes(value));
    }
    return new MapReader(path.toFile());
  }

  private MapStamp.Tile original() {
    return new MapStamp.Tile("Grass", "Old wall", .5f, 2f, -3, -90, 7, 9);
  }

  @Test
  void gestureRestoresEveryLayerAndCollisionAndCanRedoAfterSaving() throws Exception {
    try (var map = map()) {
      byte[] collision = new byte[1024];
      original().write(map, collision, 4, 5);
      var points =
          List.of(
              new BuildEdits.Placement(4, 5, "Wall 1", false, 1, 1, 0, -96, 0, true),
              new BuildEdits.Placement(4, 5, "Corner", false, 1, 1, 1, -89, 2, true));
      var edit = BuildEdits.paint(map, collision, points, true);
      assertEquals(1, edit.changes().size());
      edit.apply(map, collision, true);
      var after = MapStamp.Tile.read(map, collision, 4, 5);
      assertEquals("Grass", after.ground());
      assertEquals("Corner", after.decor());
      assertEquals(1, after.collision());
      map.writeCompact(temp.resolve("saved.mapbin").toFile());
      edit.apply(map, collision, false);
      assertEquals(original(), MapStamp.Tile.read(map, collision, 4, 5));
      edit.apply(map, collision, true);
      assertEquals(after, MapStamp.Tile.read(map, collision, 4, 5));
    }
  }

  @Test
  void invalidGestureIsAtomicAndGroundPaintingPreservesScenery() throws Exception {
    try (var map = map()) {
      byte[] collision = new byte[1024];
      original().write(map, collision, 4, 5);
      var occupied = new BuildEdits.Placement(4, 5, "New", false, 1, 1, 0, 0, 0, true);
      assertThrows(
          IllegalArgumentException.class,
          () -> BuildEdits.paint(map, collision, List.of(occupied), false));
      var outside = new BuildEdits.Placement(32, 5, "New", false, 1, 1, 0, 0, 0, true);
      assertThrows(
          IllegalArgumentException.class,
          () -> BuildEdits.paint(map, collision, List.of(occupied, outside), true));
      assertEquals(original(), MapStamp.Tile.read(map, collision, 4, 5));
      BuildEdits.paint(
              map,
              collision,
              List.of(new BuildEdits.Placement(4, 5, "Floor", true, 1, 1, 0, 0, 0, false)),
              false)
          .apply(map, collision, true);
      assertEquals(
          new MapStamp.Tile("Floor", "Old wall", .5f, 2f, -3, -90, 7, 9),
          MapStamp.Tile.read(map, collision, 4, 5));
    }
  }

  @Test
  void capturedMaskRoundTripsAndPastesRelativeCoordinates() throws Exception {
    try (var map = map()) {
      byte[] collision = new byte[1024];
      original().write(map, collision, 4, 5);
      var stamp =
          MapStamp.capture(
              map,
              collision,
              "../My temple",
              "buildings",
              "test",
              4,
              5,
              3,
              3,
              (x, y) -> x == 4 && y == 5);
      var path = stamp.saveNew(temp.resolve("library"));
      assertEquals(temp.resolve("library"), path.getParent());
      assertEquals(stamp, MapStamp.load(path));
      assertEquals(1, stamp.cells().size());
      var edit = BuildEdits.stamp(map, collision, stamp, 20, 21, false);
      edit.apply(map, collision, true);
      assertEquals(original(), MapStamp.Tile.read(map, collision, 20, 21));
      assertEquals(original(), MapStamp.Tile.read(map, collision, 4, 5));
      edit.apply(map, collision, false);
      assertNull(map.getDecorSpriteName(20, 21));
      assertThrows(
          IllegalArgumentException.class,
          () -> BuildEdits.stamp(map, collision, stamp, 32, 21, true));
    }
  }

  @Test
  void invalidTemplatesAreRejectedBeforeUse() throws Exception {
    var file = temp.resolve("bad.json");
    Files.writeString(file, "{\"version\":1,\"width\":99999999}");
    assertThrows(IOException.class, () -> MapStamp.load(file));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new MapStamp(
                1,
                "bad",
                "",
                "",
                1,
                1,
                List.of(new MapStamp.Cell(0, 0, original()), new MapStamp.Cell(0, 0, original()))));
  }

  @Test
  void diagonalWallsAndRoomsStayConnectedInEveryDragDirection() throws Exception {
    var style = WallStyle.load(Path.of("assets/editor/brick-wall.json"));
    for (int dx : new int[] {-8, 8})
      for (int dy : new int[] {-20, 20}) {
        var line = style.line(20, 20, 20 + dx, 20 + dy);
        for (int i = 1; i < line.size(); i++) {
          assertEquals(1, Math.abs(line.get(i).x() - line.get(i - 1).x()));
          assertEquals(1, Math.abs(line.get(i).y() - line.get(i - 1).y()));
        }
        var room = style.room(20, 20, 20 + dx, 20 + dy);
        var cells = new HashSet<BuildGeometry.Point>();
        for (var p : room) {
          assertTrue(p.blocking());
          cells.add(new BuildGeometry.Point(p.x(), p.y()));
        }
        for (var p : cells)
          assertTrue(
              cells.stream()
                  .anyMatch(
                      q ->
                          !q.equals(p)
                              && Math.abs(q.x() - p.x()) <= 1
                              && Math.abs(q.y() - p.y()) <= 1));
        assertEquals(
            4, BuildGeometry.roomCorners(20, 20, 20 + dx, 20 + dy).stream().distinct().count());
      }
    assertThrows(
        IllegalArgumentException.class, () -> BuildGeometry.line(0, 0, 10000, 0, 1, false));
  }

  @Test
  void catalogSearchGroupsVariantsWithoutChangingSpriteIds() {
    var sprites = List.of(sprite("BrickWall 1"), sprite("BrickWall 2"), sprite("WoodDoor"));
    var catalog = new AssetCatalog(sprites);
    var results =
        catalog.search(
            "BRICK wall",
            AssetCatalog.Category.WALLS,
            AssetCatalog.Material.STONE,
            true,
            Set.of(),
            false);
    assertEquals(1, results.size());
    assertEquals("BrickWall 1", results.getFirst().name());
    assertEquals(2, catalog.variants(results.getFirst()).size());
    assertEquals(
        1,
        catalog
            .search(
                "",
                AssetCatalog.Category.ALL,
                AssetCatalog.Material.ALL,
                false,
                Set.of("WoodDoor"),
                true)
            .size());
  }

  private SpriteLoader.Sprite sprite(String name) {
    return new SpriteLoader.Sprite(name, 64, 96, 1, 0, 0, 0, 0, new byte[0]);
  }

  @Test
  void preferencesPersistAndBrokenFilesFallBack() throws Exception {
    var prefs = new BuildPreferences();
    prefs.favorites.add("Wall");
    for (int i = 0; i < 30; i++) prefs.remember("Sprite" + i);
    var file = temp.resolve("prefs.json");
    prefs.save(file);
    var read = BuildPreferences.load(file);
    assertEquals(prefs.favorites, read.favorites);
    assertEquals(24, read.recent.size());
    assertEquals("Sprite29", read.recent.getFirst());
    Files.writeString(file, "garbage");
    assertTrue(BuildPreferences.load(file).favorites.isEmpty());
  }

  @Test
  void bundledTemplatesContainSceneryAndCollision() throws Exception {
    try (var paths = Files.list(Path.of("assets/editor/templates"))) {
      var files = paths.filter(p -> p.toString().endsWith(".json")).toList();
      assertEquals(5, files.size());
      for (var path : files) {
        var stamp = MapStamp.load(path);
        assertTrue(stamp.cells().stream().anyMatch(c -> c.tile().decor() != null));
        assertTrue(stamp.cells().stream().anyMatch(c -> c.tile().collision() > 0));
        assertTrue(stamp.source().contains("("));
      }
    }
  }
}
