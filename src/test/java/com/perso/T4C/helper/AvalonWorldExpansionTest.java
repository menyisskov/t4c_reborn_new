package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.config.Paths;
import com.perso.T4C.spawn.SpawnDefinition;
import com.perso.T4C.spawn.SpawnRegistry;
import com.perso.T4C.teleport.TeleportDefinition;
import com.perso.T4C.teleport.TeleportRegistry;
import java.io.File;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class AvalonWorldExpansionTest {
  @Test
  void expandedWorldPreservesLibraryAndProvidesReadableAvalonEncounters() throws Exception {
    try (MapReader map = new MapReader(new File(Paths.MAP))) {
      CollisionReader collision = new CollisionReader(new File(Paths.COLLISION_MAP));
      assertEquals(6144, map.getWidth());
      assertEquals(3072, map.getHeight());
      assertEquals(map.getWidth(), collision.getWidth());
      assertEquals(map.getHeight(), collision.getHeight());
      // These are original pre-Avalon library floors and stair endpoints, not copied island art.
      assertLibrary(map, collision, 985, 1465, "Floor: Wooden 3", 5);
      assertLibrary(map, collision, 1080, 1400, "Floor: Wooden 5", 5);
      assertLibrary(map, collision, 1000, 1464, "Floor: Wooden 1", 0);
      assertLibrary(map, collision, 1155, 1418, "Floor: Wooden Separation", 0);
      assertLibrary(map, collision, 1058, 1606, "Floor: Wooden 1", 5);

      // The migration mask also covers old walkable void: every possible destination needs land.
      for (int y = 1100; y < 2250; y++) {
        for (int x = 650; x < 2100; x++) {
          if (!AvalonWorldLayout.isLegacyAvalonTile(x, y)) continue;
          String target = map.getGroundSpriteName(x + AvalonWorldLayout.SHIFT_X, y);
          assertTrue(
              target != null && !target.equals("Black Tile") && !target.startsWith("Ground_Water"),
              "old save would migrate into missing terrain at " + x + "," + y);
        }
      }

      List<SpawnDefinition> monsters =
          SpawnRegistry.monsters().stream().filter(s -> s.z() == 0 && s.x() >= 3072).toList();
      assertFalse(monsters.isEmpty());
      for (SpawnDefinition spawn : monsters) {
        assertFalse(collision.hasCollision(spawn.x(), spawn.y()), "blocked spawn: " + spawn);
        String ground = map.getGroundSpriteName(spawn.x(), spawn.y());
        assertTrue(
            ground != null && !ground.equals("Black Tile") && !ground.startsWith("Ground_Water"),
            "missing encounter ground: " + spawn);
        for (int dy = -23; dy <= 23; dy++) {
          for (int dx = -23; dx <= 23; dx++) {
            if (dx * dx + dy * dy >= 24 * 24) continue;
            assertFalse(
                isGhostTree(map.getDecorSpriteName(spawn.x() + dx, spawn.y() + dy)),
                "ghost canopy obscures encounter: " + spawn);
          }
        }
      }
      int trees = 0;
      for (int y = 1430; y <= 1690; y++) {
        for (int x = 3990; x <= 4250; x++) {
          if (isGhostTree(map.getDecorSpriteName(x, y))) trees++;
        }
      }
      assertTrue(trees > 0 && trees < 100, "retain sparse ghost scenery, found " + trees);
    }
  }

  @Test
  void originalLibraryStairsStillConnectOriginalRooms() {
    List<TeleportDefinition> links = TeleportRegistry.load();
    assertTrue(links.contains(new TeleportDefinition(1003, 0, 1000, 1399, 0, 999, 1465)));
    assertTrue(links.contains(new TeleportDefinition(1004, 0, 1000, 1464, 0, 999, 1400)));
  }

  private static void assertLibrary(
      MapReader map, CollisionReader collision, int x, int y, String ground, int flag) {
    assertEquals(ground, map.getGroundSpriteName(x, y));
    assertNull(map.getDecorSpriteName(x, y));
    assertEquals(flag, collision.getCollision(x, y));
    assertFalse(AvalonWorldLayout.isLegacyAvalonTile(x, y), "Library saves must stay in place");
  }

  private static boolean isGhostTree(String name) {
    if (name == null) return false;
    String lower = name.toLowerCase(Locale.ROOT);
    return lower.contains("deadtree")
        || lower.contains("deadforesttree")
        || lower.contains("darktree")
        || lower.contains("leaflesstree");
  }
}
