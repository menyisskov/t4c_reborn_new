package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class GridPathfindingParityTest {

  // These traces were recorded from Pathfinding at f4b34fa3 before extraction.
  @Test
  void followsOriginalOpenGridAndObstacleTieOrder() {
    Fixture clear = new Fixture(5, 5);
    assertEquals("1,1 2,2 3,3 4,4", trace(GridPathfinding.findPath(0, 0, 4, 4, 5, 5, clear)));
    Fixture center = new Fixture(5, 5);
    center.block(1, 1);
    assertEquals("1,0 2,0 2,1 2,2 3,3 4,4",
        trace(GridPathfinding.findPath(0, 0, 4, 4, 5, 5, center)));
    assertEquals("", trace(GridPathfinding.findPath(0, 0, 4, 4, 5, 5, center, 1)));
  }

  @Test
  void keepsCornerGuardAndGoalClearanceException() {
    Fixture corner = new Fixture(5, 5);
    corner.block(1, 0);
    corner.block(0, 1);
    assertEquals("", trace(GridPathfinding.findPath(0, 0, 2, 2, 5, 5, corner)));
    Fixture goalNearObstacle = new Fixture(5, 5);
    goalNearObstacle.block(4, 2);
    assertEquals("3,2", trace(GridPathfinding.findPath(2, 2, 3, 2, 5, 5, goalNearObstacle, 1)));
    goalNearObstacle.block(3, 2);
    assertEquals("", trace(GridPathfinding.findPath(2, 2, 3, 2, 5, 5, goalNearObstacle, 1)));
  }

  @Test
  void unavailableCollisionAndBoundsKeepOriginalBehavior() {
    Fixture unavailable = new Fixture(5, 5);
    unavailable.block(1, 1);
    unavailable.initialized = false;
    assertEquals("1,1 2,2", trace(GridPathfinding.findPath(0, 0, 2, 2, 5, 5, unavailable)));
    assertEquals("1,1 2,2", trace(GridPathfinding.findPath(0, 0, 2, 2, 5, 5, null)));
    assertTrue(GridPathfinding.findPath(-1, 0, 2, 2, 5, 5, unavailable).isEmpty());
    assertTrue(GridPathfinding.findPath(2, 2, 2, 2, 5, 5, unavailable).isEmpty());
  }

  @Test
  void checksBudgetOnlyAtEveryThirtySecondExpansion() {
    Fixture clear = new Fixture(100, 1);
    AtomicInteger reads = new AtomicInteger();
    assertEquals("", trace(GridPathfinding.findPath(0, 0, 99, 0, 100, 1, clear, 0,
        () -> reads.getAndIncrement() == 0 ? 0L : 16_000_001L)));
    assertEquals(2, reads.get());
    reads.set(0);
    assertEquals(99, GridPathfinding.findPath(0, 0, 99, 0, 100, 1, clear, 0,
        () -> reads.getAndIncrement() == 0 ? 0L : 16_000_000L).size());
  }

  private static String trace(List<GridPathfinding.GridPoint> path) {
    StringBuilder result = new StringBuilder();
    for (GridPathfinding.GridPoint point : path) {
      if (!result.isEmpty()) result.append(' ');
      result.append(point.x).append(',').append(point.y);
    }
    return result.toString();
  }

  private static final class Fixture implements GridPathfinding.Collision {
    private final boolean[][] blocked;
    private boolean initialized = true;

    Fixture(int width, int height) {
      blocked = new boolean[height][width];
    }

    void block(int x, int y) {
      blocked[y][x] = true;
    }

    @Override
    public boolean isInitialized() {
      return initialized;
    }

    @Override
    public boolean hasStaticCollisionAtGrid(int x, int y) {
      return y >= 0 && y < blocked.length && x >= 0 && x < blocked[0].length && blocked[y][x];
    }

    @Override
    public boolean hasStaticCollisionNearGrid(int x, int y, int clearanceTiles) {
      for (int nearY = y - clearanceTiles; nearY <= y + clearanceTiles; nearY++) {
        for (int nearX = x - clearanceTiles; nearX <= x + clearanceTiles; nearX++) {
          if (hasStaticCollisionAtGrid(nearX, nearY)) return true;
        }
      }
      return false;
    }
  }
}
