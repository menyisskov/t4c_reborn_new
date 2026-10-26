package com.perso.T4C.world;

import java.util.Objects;

/** Static collision rules for one world. Sources must remain stable for this instance's lifetime. */
public final class WorldCollision {
  @FunctionalInterface
  public interface TilePredicate {
    boolean test(int x, int y);
  }

  private final int width;
  private final int height;
  private final TilePredicate movement;
  private final TilePredicate sight;

  public WorldCollision(
      int width,
      int height,
      TilePredicate movement,
      TilePredicate sight) {
    if (width <= 0 || height <= 0) throw new IllegalArgumentException("Invalid world dimensions");
    this.width = width;
    this.height = height;
    this.movement = Objects.requireNonNull(movement, "movement");
    this.sight = Objects.requireNonNull(sight, "sight");
  }

  public boolean contains(int x, int y) {
    return x >= 0 && y >= 0 && x < width && y < height;
  }

  /**
   * Preserves the desktop wall-foot rule: the front of a continuous wall is walkable. Callers must
   * validate destinations with contains separately, as offline movement does with map pixel bounds.
   */
  public boolean blocksMovement(int x, int y) {
    return rawBlocks(x, y) && !isWalkableWallFoot(x, y);
  }

  /** Wall-foot movement exceptions never open a line of sight through the wall. */
  public boolean blocksLineOfSight(int x, int y) {
    return !contains(x, y) || sight.test(x, y);
  }

  private boolean rawBlocks(int x, int y) {
    return !contains(x, y) || movement.test(x, y);
  }

  private boolean isWalkableWallFoot(int x, int y) {
    return isWallFootColumn(x, y) && (continuesWallFront(x - 1, y) || continuesWallFront(x + 1, y));
  }

  private boolean continuesWallFront(int x, int y) {
    return isWallFootColumn(x, y)
        || isWallFootColumn(x, y - 1)
        || isWallFootColumn(x, y + 1);
  }

  private boolean isWallFootColumn(int x, int y) {
    return rawBlocks(x, y) && rawBlocks(x, y + 1) && !rawBlocks(x, y - 1);
  }
}
