package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.Test;

/**
 * T4C-0314. The 2-hour native soak saw two adapters at positions the strict standing rule ({@code canStand}, which folds
 * the tile above the foot into the block) calls blocked. The server's grid rule is single-player's: it looks at the static
 * foot collision, and its wall-slide step may enter a tile whose tile above is blocked when the departure tile had the
 * same block. So players can legitimately come to rest on a tile {@code canStand} refuses, and a character who logs out
 * there could not rejoin ("position" 409). Joins must accept every tile the movement rule lets a player rest on, and the
 * soak's walkability guard must know the glide lattice (between two adjacent tile centres, diagonals included).
 */
class GridRestPositionTest {
  private static final Path WORLD = Path.of("../../assets/maps/worldmap/worldmap.colbin");

  private static PreviewMap startMap() throws Exception {
    return HostedRegionPlan.startingSlice(WORLD).regions().stream()
        .filter(r -> r.spec().key().equals("start")).findFirst().orElseThrow().map();
  }

  /** Where a player ends up after holding one direction for a while and then letting go. */
  private static double[] restAfter(PreviewMap map, ServerMovement.Terrain terrain, int tx, int ty, int dx, int dy) {
    var glide = new ServerMovement.Glide();
    double x = tx + .5, y = ty + .5;
    for (int tick = 0; tick < 12; tick++) {
      var moved = ServerMovement.step(map, terrain, glide, x, y, dx, dy);
      x = moved.x();
      y = moved.y();
    }
    for (int tick = 0; tick < 60 && glide.active(); tick++) {
      var moved = ServerMovement.step(map, terrain, glide, x, y, 0, 0);
      x = moved.x();
      y = moved.y();
    }
    return glide.active() ? null : new double[] {x, y};
  }

  @Test
  void everyTheGridRuleCanRestOnIsRejoinableAndOnTheGuardLattice() throws Exception {
    var map = startMap();
    var terrain = new ServerMovement.Terrain(map);
    int foldedRests = 0, checked = 0;
    for (int ty = 1; ty < map.height - 1; ty++)
      for (int tx = 1; tx < map.width - 1; tx++) {
        if (!map.canStand(tx + .5, ty + .5)) continue;
        for (int dx = -1; dx <= 1; dx++)
          for (int dy = -1; dy <= 1; dy++) {
            if (dx == 0 && dy == 0) continue;
            var rest = restAfter(map, terrain, tx, ty, dx, dy);
            if (rest == null) continue;
            checked++;
            assertTrue(map.canRestOnGrid(rest[0], rest[1]), "rest " + rest[0] + "," + rest[1] + " from " + tx + "," + ty);
            assertTrue(map.onGridPath(rest[0], rest[1]), "lattice " + rest[0] + "," + rest[1]);
            if (!map.canStand(rest[0], rest[1])) foldedRests++;
            // The join that follows a logout there must not be refused.
            final double rx = rest[0], ry = rest[1];
            assertDoesNotThrow(() -> new PreviewWorld(map).joinAt(rx, ry), "rejoin at " + rx + "," + ry);
          }
      }
    assertTrue(checked > 100_000, "the sweep covered the start crop, got " + checked);
    // The sweep must actually reach the case this test is about.
    assertTrue(foldedRests > 0, "no rest on a tile the strict rule refuses was reached");
  }

  @Test
  void theGuardLatticeAcceptsTwoSoakObservedMidGlidePositionsAndStillRejectsOffLatticeOnes() throws Exception {
    var map = startMap();
    int ox = map.originX, oy = map.originY;
    // Abs 2867.0,1081.0 is the midpoint of a diagonal glide between tiles 2866,1080 and 2867,1081 (both enterable).
    assertTrue(map.onGridPath(2866.99997 - ox, 1080.99999 - oy));
    // Abs 2797.0,1104.0 is the corner of foot-blocked tile 2797,1104: a diagonal between the free tiles 2797,1103 and 2796,1104.
    assertTrue(map.onGridPath(2797.00003 - ox, 1104.0000076 - oy));
    // T4C-0308: walking along a row boundary (y on a whole number, x anywhere) is not a glide path.
    assertFalse(map.onGridPath(2785.3 - ox, 1096.0 - oy));
    assertFalse(map.onGridPath(2785.7 - ox, 1096.0 - oy));
    // A tile centre inside a foot-blocked tile is never a rest.
    assertFalse(map.onGridPath(2797.5 - ox, 1104.5 - oy));
    // Outside the crop.
    assertFalse(map.onGridPath(-1, 5));
  }
}
