package com.perso.T4C.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.death.DeathPenaltyService;
import com.perso.T4C.editor.build.MapStamp;
import com.perso.T4C.helper.CollisionMapIO;
import com.perso.T4C.helper.CollisionType;
import com.perso.T4C.helper.MapReader;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.BitSet;
import org.junit.jupiter.api.Test;

/** Checks the shipped terrain, collision layer, and mainland connection together. */
class AvalonRegionMapIntegrationTest {
  private static final int WIDTH = 5120;
  private static final int HEIGHT = 3072;

  @Test
  void newIslandsAndMainlandLandmarksAreWalkableFromAvalon() throws Exception {
    try (MapReader map = new MapReader(new File("assets/maps/worldmap/worldmap.mapbin"))) {
      var collisions = CollisionMapIO.read(new File("assets/maps/worldmap/worldmap.colbin"));
      assertEquals(WIDTH, map.getWidth());
      assertEquals(HEIGHT, map.getHeight());
      assertEquals(WIDTH, collisions.getWidth());
      assertEquals(HEIGHT, collisions.getHeight());
      byte[] data = collisions.getData();
      BitSet reached = flood(data, 4040, 1477);

      for (var site : AvalonRegionBuilder.SITES) {
        int tile = index(site.x(), site.y());
        assertTrue(reached.get(tile), site.name() + " is disconnected from Avalon");
        assertFalse(
            CollisionType.fromValue(data[tile] & 255).isBlocksMovement(),
            site.name() + " center must be walkable");
        assertFalse(
            map.getGroundSpriteName(site.x(), site.y()).startsWith("Ground_Water"),
            site.name() + " center must have land terrain");
      }

      // The causeways are actual terrain connections. Water outside their footprints stays sea.
      assertTrue(reached.get(index(3835, 855)));
      assertTrue(reached.get(index(4315, 2410)));
      assertEquals(CollisionType.DEEP_WATER.getValue(), data[index(3500, 650)] & 255);
      assertEquals(CollisionType.DEEP_WATER.getValue(), data[index(4800, 2600)] & 255);

      // Existing sanctuary protection remains, while the new adventure areas are ordinary land.
      assertTrue(DeathPenaltyService.isSafeHaven(data[index(4040, 1477)] & 255));
      for (var site : AvalonRegionBuilder.SITES) {
        assertFalse(
            DeathPenaltyService.isSafeHaven(data[index(site.x(), site.y())] & 255),
            site.name() + " should not silently become a sanctuary");
      }
    }
  }

  @Test
  void reusedBuildingWallsKeepTheirFullCollisionFootprint() throws Exception {
    byte[] data = CollisionMapIO.read(new File("assets/maps/worldmap/worldmap.colbin")).getData();
    assertBuildingWalls(data, "temple", 3868, 692);
    assertBuildingWalls(data, "cottage", 3727, 765);
    assertBuildingWalls(data, "storehouse", 4070, 605);
    assertBuildingWalls(data, "cottage", 4305, 2585);
    assertBuildingWalls(data, "storehouse", 4450, 2684);
    assertBuildingWalls(data, "cottage", 3604, 1317);
    assertBuildingWalls(data, "storehouse", 4440, 2050);
  }

  private static void assertBuildingWalls(byte[] data, String name, int x, int y)
      throws Exception {
    MapStamp stamp = MapStamp.load(Path.of("assets/editor/templates", name + ".json"));
    int blockingCells = 0;
    for (var cell : stamp.cells()) {
      if (!CollisionType.fromValue(cell.tile().collision()).isBlocksMovement()) continue;
      blockingCells++;
      int tile = index(x + cell.x(), y + cell.y());
      assertTrue(
          CollisionType.fromValue(data[tile] & 255).isBlocksMovement(),
          name + " wall is passable at " + (x + cell.x()) + "," + (y + cell.y()));
    }
    assertTrue(blockingCells > 10, name + " template should include a wall footprint");
  }

  private static BitSet flood(byte[] data, int x, int y) {
    BitSet reached = new BitSet(WIDTH * HEIGHT);
    ArrayDeque<Integer> queue = new ArrayDeque<>();
    int start = index(x, y);
    reached.set(start);
    queue.add(start);
    while (!queue.isEmpty()) {
      int current = queue.removeFirst();
      int cx = current % WIDTH;
      int cy = current / WIDTH;
      if (cx > 0) visit(data, reached, queue, current - 1);
      if (cx + 1 < WIDTH) visit(data, reached, queue, current + 1);
      if (cy > 0) visit(data, reached, queue, current - WIDTH);
      if (cy + 1 < HEIGHT) visit(data, reached, queue, current + WIDTH);
    }
    return reached;
  }

  private static void visit(byte[] data, BitSet reached, ArrayDeque<Integer> queue, int tile) {
    if (!reached.get(tile) && !CollisionType.fromValue(data[tile] & 255).isBlocksMovement()) {
      reached.set(tile);
      queue.add(tile);
    }
  }

  private static int index(int x, int y) {
    return y * WIDTH + x;
  }
}
