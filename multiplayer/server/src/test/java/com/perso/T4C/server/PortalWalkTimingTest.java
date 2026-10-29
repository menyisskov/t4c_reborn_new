package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.Test;

/**
 * T4C-0309. A walker that plans every 100 ms with a cardinal route over the collision map, steering by the tile the
 * glide is arriving on (what the native soak and a real client do), always reaches the skeleton-cave mouth from the
 * Lighthaven hub and the way back to the Lighthaven door from the cave arrival. The server's grid rule never traps it.
 * The walk is long, though: about 2.5 minutes at the shared player speed, which is why the soak's leg timeout of 150 s
 * made roughly one leg per run time out within a few tiles of the goal. The soak's default leg timeout must stay
 * above the slowest ideal route measured here, with room for creatures in the way.
 */
class PortalWalkTimingTest {
  private static final int[][] CARDINALS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
  private static final int TICKS_PER_PLAN = 3;

  /** Matches {@code NativeAdapterSoak.DEFAULT_LEG_TIMEOUT_SECONDS}. */
  private static final int SOAK_LEG_TIMEOUT_SECONDS = 300;

  private static int arrivingTile(double c, int last) {
    int tile = (int) Math.round(c - .5);
    if (Math.abs(c - Math.floor(c) - .5) > 1e-6) tile = last > 0 ? (int) Math.ceil(c - .5) : last < 0 ? (int) Math.floor(c - .5) : tile;
    return tile;
  }

  private static int[] firstStep(PreviewMap map, int sx, int sy, int gx, int gy) {
    if (sx == gx && sy == gy) return null;
    int[] parent = new int[map.width * map.height];
    Arrays.fill(parent, -1);
    int[] queue = new int[parent.length];
    int head = 0, tail = 0, start = sy * map.width + sx, goal = gy * map.width + gx;
    queue[tail++] = start;
    parent[start] = start;
    while (head < tail && parent[goal] < 0) {
      int cell = queue[head++], cx = cell % map.width, cy = cell / map.width;
      for (int[] d : CARDINALS) {
        int nx = cx + d[0], ny = cy + d[1], id = ny * map.width + nx;
        if (nx < 0 || ny < 0 || nx >= map.width || ny >= map.height || parent[id] >= 0 || !map.canStand(nx + .5, ny + .5)) continue;
        parent[id] = cell;
        queue[tail++] = id;
      }
    }
    if (parent[goal] < 0) return null;
    int cell = goal;
    while (parent[cell] != start) cell = parent[cell];
    return new int[] {cell % map.width, cell / map.width};
  }

  /** Seconds a plan-every-100ms cardinal walker needs to stand on the goal tile, or -1 when it never does. */
  private static double secondsToReach(PreviewMap map, int startX, int startY, int goalX, int goalY, int limitSeconds) {
    var world = new PreviewWorld(map);
    var joined = world.joinAt(startX - map.originX + .5, startY - map.originY + .5);
    int gx = goalX - map.originX, gy = goalY - map.originY, lastX = 0, lastY = 0;
    long sequence = 1;
    for (int tick = 0; tick < limitSeconds * PreviewWorld.TICK_RATE; tick++) {
      if (tick % TICKS_PER_PLAN == 0) {
        var at = world.position(joined.token());
        if ((int) Math.floor(at.x()) == gx && (int) Math.floor(at.y()) == gy) return tick / (double) PreviewWorld.TICK_RATE;
        int arrivingX = arrivingTile(at.x(), lastX), arrivingY = arrivingTile(at.y(), lastY);
        int[] next = firstStep(map, arrivingX, arrivingY, gx, gy);
        if (next == null) {
          world.input(joined.token(), sequence++, 0, 0);
        } else {
          lastX = Integer.signum(next[0] - arrivingX);
          lastY = Integer.signum(next[1] - arrivingY);
          world.input(joined.token(), sequence++, lastX, lastY);
        }
      }
      world.step();
    }
    return -1;
  }

  @Test
  void anIdealWalkerAlwaysReachesTheCaveMouthAndTheHubDoorButTheWalkIsLong() throws Exception {
    var plan = HostedRegionPlan.startingSlice(Path.of("../../assets/maps/worldmap/worldmap.colbin"));
    var map = plan.regions().get(0).map();
    double slowest = 0;
    // From the hub spawn, the hub door arrival and the creature circle to the three cave-mouth tiles (portals 12, 13, 14).
    for (int[] start : new int[][] {{2944, 1059}, {2932, 1059}, {2951, 1038}, {2935, 1085}}) {
      for (int[] mouth : new int[][] {{2770, 1094}, {2770, 1095}, {2771, 1094}}) {
        assertTrue(plan.portals.resolve(0, mouth[0] + .5, mouth[1] + .5, x -> true).isPresent(), "A cave-mouth portal tile");
        double seconds = secondsToReach(map, start[0], start[1], mouth[0], mouth[1], 400);
        assertTrue(seconds > 0, "Cave mouth " + mouth[0] + "," + mouth[1] + " must be reachable from " + start[0] + "," + start[1]);
        slowest = Math.max(slowest, seconds);
      }
    }
    // The way back: arriving from the cave at 2772,1096 and walking to the hub door (portal 16 at 2931,1056).
    double back = secondsToReach(map, 2772, 1096, 2931, 1056, 400);
    assertTrue(back > 0, "The hub door must be reachable from the cave arrival");
    slowest = Math.max(slowest, back);
    // Still a long walk (the old 150 s timeout was too short); the floor is loose so a player-speed retune cannot break it.
    assertTrue(slowest > 60, "Measured " + slowest + " s: the long walk this test documents (a short one means the map or speed changed)");
    assertTrue(slowest * 1.5 < SOAK_LEG_TIMEOUT_SECONDS,
        "The soak leg timeout (" + SOAK_LEG_TIMEOUT_SECONDS + " s) must leave 50% headroom over the slowest ideal route (" + slowest + " s)");
  }
}
