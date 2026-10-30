package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * T4C-0372: in a streamed world-0 region local tile coordinates equal absolute ones (up to x = 6144), and the shared
 * movement rule runs in float world pixels (x * 32 is about 187,000 px at the far east, where one float step is
 * 0.0156 px). The server steps in a frame whose origin is a whole number of tiles near the player, so the float
 * keeps the precision it has near the origin. These walks pin that: the same walk at x about 5800 must stay on the
 * glide lattice and must match the walk at x about 200 to far better than the soak guard's tolerance.
 */
class ServerMovementFarEastPrecisionTest {
  private static final int WIDTH = 6144;
  private static final int HEIGHT = 1600;
  private static final int TICKS = 800;
  /** The soak guard's tolerance (PreviewMap.LATTICE_EPSILON) is 2e-3 tile. */
  private static final double GUARD = 2e-3;

  private static PreviewMap openMap() {
    return new PreviewMap(new boolean[HEIGHT][WIDTH], 0, 0, 10, 10);
  }

  /** x and y after every tick of holding (dx, dy) from the tile centre (tx + .5, ty + .5). */
  private static double[][] walk(PreviewMap map, int tx, int ty, int dx, int dy) {
    var terrain = new ServerMovement.Terrain(map);
    var glide = new ServerMovement.Glide();
    double x = tx + .5;
    double y = ty + .5;
    double[][] out = new double[TICKS][];
    for (int i = 0; i < TICKS; i++) {
      var moved = ServerMovement.step(map, terrain, glide, x, y, dx, dy);
      x = moved.x();
      y = moved.y();
      out[i] = new double[] {x, y};
    }
    return out;
  }

  private static double worstLatticeDeviation(double[][] path, int dx, int dy, double x0, double y0) {
    double worst = 0;
    for (double[] p : path) {
      double moveX = (p[0] - x0) * dx;
      double moveY = (p[1] - y0) * dy;
      if (dx != 0 && dy != 0) worst = Math.max(worst, Math.abs(moveX - moveY));
      else if (dx != 0) worst = Math.max(worst, Math.abs(p[1] - y0));
      else worst = Math.max(worst, Math.abs(p[0] - x0));
    }
    return worst;
  }

  private static double worstDifference(double[][] a, double[][] b, int shiftX) {
    double worst = 0;
    for (int i = 0; i < a.length; i++) {
      worst = Math.max(worst, Math.abs(a[i][0] - shiftX - b[i][0]));
      worst = Math.max(worst, Math.abs(a[i][1] - b[i][1]));
    }
    return worst;
  }

  @Test
  void aStraightAndADiagonalWalkAtTheFarEastMatchTheWalkNearTheOrigin() {
    PreviewMap map = openMap();
    int[][] directions = {{1, 0}, {1, 1}, {-1, 1}, {-1, -1}, {0, 1}};
    java.util.List<String> failures = new java.util.ArrayList<>();
    for (int[] d : directions) {
      double[][] near = walk(map, 200, 700, d[0], d[1]);
      double[][] far = walk(map, 5800, 700, d[0], d[1]);
      double nearDeviation = worstLatticeDeviation(near, d[0], d[1], 200.5, 700.5);
      double farDeviation = worstLatticeDeviation(far, d[0], d[1], 5800.5, 700.5);
      double difference = worstDifference(far, near, 5600);
      String label = "direction " + d[0] + "," + d[1];
      System.out.printf("%s near dev %.6f far dev %.6f far-vs-near %.6f%n", label, nearDeviation, farDeviation, difference);
      if (nearDeviation >= 1e-3) failures.add(label + " near deviation " + nearDeviation);
      if (farDeviation >= 1e-3) failures.add(label + " far deviation " + farDeviation + " (guard " + GUARD + ")");
      if (difference >= 1e-3) failures.add(label + " far vs near " + difference);
      for (double[] p : far) {
        if (!map.onGridPath(p[0], p[1])) {
          failures.add(label + " off the glide lattice at " + p[0] + "," + p[1]);
          break;
        }
      }
    }
    assertTrue(failures.isEmpty(), failures.toString());
  }

  @Test
  void aWalkAcrossTheFrameBoundaryKeepsGlidingWithoutAJump() {
    PreviewMap map = openMap();
    // The frame origin changes every 256 tiles; a diagonal walk from just before one must stay continuous.
    double[][] path = walk(map, 5110, 700, 1, 1);
    double px = 5110.5;
    // A tick may cover one speed-sized step plus a little rounding, never a jump.
    double stepLimit =
        1.25 * com.perso.T4C.config.GameConstants.PLAYER_SPEED
            / com.perso.T4C.config.GameConstants.GRID_W
            / PreviewWorld.TICK_RATE;
    for (double[] p : path) {
      assertTrue(Math.abs(p[0] - px) < stepLimit, "a tick moved " + (p[0] - px) + " tiles");
      px = p[0];
    }
    assertEquals(path[TICKS - 1][0] - 5110.5, path[TICKS - 1][1] - 700.5, 1e-3);
  }
}
