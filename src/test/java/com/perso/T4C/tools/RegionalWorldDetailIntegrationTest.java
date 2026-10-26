package com.perso.T4C.tools;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.perso.T4C.editor.build.MapStamp;
import com.perso.T4C.helper.CollisionMapIO;
import com.perso.T4C.helper.CollisionType;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.spawn.SpawnRegistry;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.Test;

class RegionalWorldDetailIntegrationTest {
  private static RegionalWorldDetailBuilder.Layout layout() throws Exception {
    return new Gson()
        .fromJson(
            Files.readString(Path.of("assets/maps/worldmap/world-detail-layout.json")),
            RegionalWorldDetailBuilder.Layout.class);
  }

  @Test
  void fortGatesAndDistributedHuntingRemainConnectedAndTheFinalArenaStaysSealed() throws Exception {
    var map = CollisionMapIO.read(new File("assets/maps/worldmap/worldmap.colbin"));
    var layout = layout();
    int width = map.getWidth();
    byte[] collision = map.getData();
    Map<Integer, BitSet> regions = new HashMap<>();
    for (int[] root : new int[][] {{4040, 1477}, {5505, 1150}, {5650, 2290}, {2380, 2430}})
      regions.put(root[0], flood(collision, width, root[0], root[1]));
    assertFalse(
        regions.get(5505).get(2290 * width + 5650), "The story-gated arena must remain isolated");
    for (var site : layout.forts()) {
      int root =
          site.x() < 3072
              ? 2380
              : site.x() < 5120 ? 4040 : site.name().equals("Hollow Crown") ? 5650 : 5505;
      var reached = regions.get(root);
      int half = site.radius() / 2;
      for (int sign : new int[] {-1, 1}) {
        int x = site.x() + sign * half, y = site.y() + sign * (site.radius() - half);
        assertTrue(reached.get(y * width + x), site.name() + " gate is blocked");
      }
    }
    var live = new HashSet<>(SpawnRegistry.monsters());
    for (var point : layout.hunting()) {
      assertTrue(live.contains(point), "Authored spawn missing from registry: " + point);
      int root = point.x() < 3072 ? 2380 : point.x() < 5120 ? 4040 : 5505;
      assertTrue(
          regions.get(root).get(point.y() * width + point.x()),
          "Hunting point is disconnected: " + point);
    }
  }

  @Test
  void nativeBuildingsHaveSolidWallsAndOpenUsableDoorways() throws Exception {
    var collision = CollisionMapIO.read(new File("assets/maps/worldmap/worldmap.colbin"));
    var layout = layout();
    try (var map = new MapReader(new File("assets/maps/worldmap/worldmap.mapbin"))) {
      for (String name : List.of("cottage", "storehouse", "temple")) {
        var building =
            layout.buildings().stream()
                .filter(b -> b.template().equals(name))
                .findFirst()
                .orElseThrow();
        var stamp = MapStamp.load(Path.of("assets/editor/templates", name + ".json"));
        for (var cell : stamp.cells()) {
          String art = cell.tile().decor();
          if (art == null) continue;
          int x = building.x() + cell.x(), y = building.y() + cell.y();
          int tile = collision.getData()[y * collision.getWidth() + x] & 255;
          if (art.toLowerCase(Locale.ROOT).contains("door")) {
            assertFalse(
                CollisionType.fromValue(tile).isBlocksMovement(), name + " entry is blocked");
            assertNull(map.getDecorSpriteName(x, y), name + " entrance should be visibly open");
          } else if (art.toLowerCase(Locale.ROOT).contains("wall"))
            assertTrue(
                CollisionType.fromValue(tile).isBlocksMovement(), name + " wall is passable");
        }
      }
    }
  }

  private static BitSet flood(byte[] collision, int width, int x, int y) {
    BitSet seen = new BitSet(collision.length);
    int[] queue = new int[collision.length];
    int head = 0, tail = 0, start = y * width + x;
    seen.set(start);
    queue[tail++] = start;
    while (head < tail) {
      int at = queue[head++], cx = at % width, cy = at / width;
      for (int next : new int[] {at - 1, at + 1, at - width, at + width}) {
        if (next < 0 || next >= collision.length || seen.get(next)) continue;
        if (Math.abs(next % width - cx) + Math.abs(next / width - cy) != 1) continue;
        if (!CollisionType.fromValue(collision[next] & 255).isBlocksMovement()) {
          seen.set(next);
          queue[tail++] = next;
        }
      }
    }
    return seen;
  }
}
