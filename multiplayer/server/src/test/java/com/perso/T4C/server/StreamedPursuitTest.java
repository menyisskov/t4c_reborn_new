package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * No database. The windowed path search: a creature of the streamed whole-world map (about 19 M tiles, where per-solve
 * arrays would be 150 MB) must chase exactly as on a small map, with search state bounded by the expansion limit.
 */
class StreamedPursuitTest {
  private static final Path WORLD = Path.of("../../assets/maps/worldmap/worldmap.colbin");
  private static final double STEP = 1.0 / PreviewWorld.TICK_RATE;

  @Test void aProvokedSkeletonFollowsAPlayerOverTheStreamedMapWithoutPerSolveArrays() throws Exception {
    var plan = HostedRegionPlan.startingSlice(WORLD, true);
    try (var hosted = new HostedWorlds(plan, null, ItemCatalog.loadBundled(), MonsterCatalog.loadBundled())) {
      var region = hosted.regions.getFirst();
      assertTrue(region.map.width * region.map.height > 1 << 20, "the search must take the hashed path on this map");
      int followed = 0, tried = 0;
      var failures = new ArrayList<String>();
      for (var cs : region.world.hostedSpawns()) {
        if (!"Skeleton".equals(cs.definition().getName())) continue;
        double x = cs.spawn().x() - region.map.originX + .0, y = cs.spawn().y() - region.map.originY + .0;
        UUID life = UUID.randomUUID(), who = UUID.randomUUID();
        for (int[] d : new int[][] {{12, 0}, {-12, 0}, {0, 12}, {0, -12}, {8, 8}, {-8, -8}}) {
          double px = Math.floor(x) + d[0] + .5, py = Math.floor(y) + d[1] + .5;
          if (!region.map.canRestOnGrid(px, py) || !reachable(region.map, (int) x, (int) y, (int) px, (int) py)) continue;
          tried++;
          var pursuit = new OnlineMonsterPursuit(region.map, cs.spawn(), cs.definition(), region.spec.key(),
              new OnlineMonsterPursuit.Seed(life, x, y, OnlineMonsterPursuit.Mode.RETALIATION, who, 1));
          var view = new OnlineMonsterPursuit.TargetView(who, 1, 0, region.spec.key(), px, py, true, true, true);
          OnlineMonsterPursuit.Decision last = null;
          double start = Math.hypot(px - x, py - y);
          for (int tick = 0; tick < 900; tick++) {
            last = pursuit.step(List.of(view), List.of(), STEP, true, false);
            if (last.attackReady() || last.mode() != OnlineMonsterPursuit.Mode.RETALIATION) break;
          }
          double end = Math.hypot(px - last.x(), py - last.y());
          if (last.attackReady() || last.mode() == OnlineMonsterPursuit.Mode.RETALIATION && end < start - 3) followed++;
          else failures.add("skeleton " + (int) x + "," + (int) y + " player " + d[0] + "," + d[1]);
        }
        if (tried >= 120) break;
      }
      System.out.println("STREAMED_PURSUIT tried " + tried + " followed " + followed);
      assertTrue(tried >= 20, "enough real placements: " + tried);
      assertEquals(tried, followed, "every placement single-player's creature A* can reach is followed: " + failures);
    }
  }

  private static boolean clear(PreviewMap map, int x, int y, boolean goal) {
    int radius = goal ? 0 : 1;
    for (int yy = y - radius; yy <= y + radius; yy++) for (int xx = x - radius; xx <= x + radius; xx++) if (map.blocksFoot(xx, yy)) return false;
    return true;
  }

  /** The reference reachability: single-player's Pathfinding over static foot collision with one tile of clearance. */
  private static boolean reachable(PreviewMap map, int sx, int sy, int gx, int gy) {
    var seen = new java.util.HashSet<Long>();
    var queue = new java.util.ArrayDeque<int[]>();
    queue.add(new int[] {sx, sy}); seen.add(sx * 100000L + sy);
    while (!queue.isEmpty()) {
      var c = queue.poll();
      if (c[0] == gx && c[1] == gy) return true;
      for (int dy = -1; dy <= 1; dy++) for (int dx = -1; dx <= 1; dx++) {
        if (dx == 0 && dy == 0) continue;
        int nx = c[0] + dx, ny = c[1] + dy;
        if (Math.abs(nx - sx) > 40 || Math.abs(ny - sy) > 40 || !seen.add(nx * 100000L + ny)) continue;
        if (!clear(map, nx, ny, nx == gx && ny == gy)) continue;
        if (dx != 0 && dy != 0 && (!clear(map, c[0] + dx, c[1], false) || !clear(map, c[0], c[1] + dy, false))) continue;
        queue.add(new int[] {nx, ny});
      }
    }
    return false;
  }
}
