package com.perso.T4C.helper;

import java.util.ArrayList;
import java.util.List;

/** Existing client pathfinding entry point, backed by the shared grid search. */
public final class Pathfinding {
  private Pathfinding() {}

  public static final class GridPoint {
    public final int x;
    public final int y;

    public GridPoint(int x, int y) {
      this.x = x;
      this.y = y;
    }
  }

  public static List<GridPoint> findPath(
      int startX, int startY, int goalX, int goalY, int width, int height,
      CollisionManager collisionManager) {
    return findPath(startX, startY, goalX, goalY, width, height, collisionManager, 0);
  }

  public static List<GridPoint> findPath(
      int startX, int startY, int goalX, int goalY, int width, int height,
      CollisionManager collisionManager, int clearanceTiles) {
    GridPathfinding.Collision collision = collisionManager == null ? null : new GridPathfinding.Collision() {
      @Override
      public boolean isInitialized() {
        return collisionManager.isInitialized();
      }

      @Override
      public boolean hasStaticCollisionAtGrid(int x, int y) {
        return collisionManager.hasStaticCollisionAtGrid(x, y);
      }

      @Override
      public boolean hasStaticCollisionNearGrid(int x, int y, int clearance) {
        return collisionManager.hasStaticCollisionNearGrid(x, y, clearance);
      }
    };
    List<GridPathfinding.GridPoint> shared = GridPathfinding.findPath(
        startX, startY, goalX, goalY, width, height, collision, clearanceTiles);
    if (shared.isEmpty()) {
      return List.of();
    }
    List<GridPoint> result = new ArrayList<>(shared.size());
    for (GridPathfinding.GridPoint point : shared) {
      result.add(new GridPoint(point.x, point.y));
    }
    return result;
  }
}
