package com.perso.T4C.world;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.PLAYER_COLLISION_FOOTPRINT_ABOVE_TILES;
import static com.perso.T4C.config.GameConstants.PLAYER_COLLISION_FOOTPRINT_HORIZONTAL_TILES;
import static com.perso.T4C.config.GameConstants.PLAYER_MOVEMENT_RESERVATION_STEP_TILES;

/**
 * The player movement step, as pure functions with no engine, no singleton and no clock.
 *
 * <p>Two rules exist today and they are NOT the same rule; the mode says which one a caller means.
 * See {@code multiplayer/PARITY.md} (movement debt) for the full difference table.
 *
 * <ul>
 *   <li>{@link Mode#GRID_RESERVATION}: the single-player rule, in world pixels ({@link Grid}).
 *       Single-player's {@code PlayerMovement} calls it; behaviour is pinned by
 *       {@code PlayerMovementGoldenTest}.
 *   <li>{@link Mode#SWEPT_SUBSTEP}: the online server's rule, in tile units ({@link Swept}).
 *       The server does not call this yet; {@code MovementRulesTest} pins it against a literal copy
 *       of {@code PreviewWorld.step}.
 * </ul>
 */
public final class MovementRules {
  private MovementRules() {}

  public enum Mode {
    /** Reserve one whole tile, glide to it, slide to a neighbouring direction when blocked. */
    GRID_RESERVATION,
    /** Move a fixed distance per server tick in 0.2 tile substeps, stop at the first blocked one. */
    SWEPT_SUBSTEP
  }

  /** What the grid rule asks of the world. The desktop adapts {@code CollisionManager} to it. */
  public interface Terrain {
    boolean hasCollision(float worldX, float worldY);

    boolean hasPlayerCollision(float worldX, float worldY);

    boolean canMoveForPlayer(float fromX, float fromY, float toX, float toY);

    boolean isPlayerPassableTile(int gridX, int gridY);
  }

  /** A claimed destination tile corner (world pixels) and the unit direction used to reach it. */
  public record Reservation(float targetX, float targetY, int moveX, int moveY) {}

  /** The single-player grid-reservation rule. All positions are world pixels (tile = 32 x 16). */
  public static final class Grid {
    public static final float POSITION_EPSILON = 0.001f;

    private Grid() {}

    /**
     * Claims the next tile for an input direction, or returns null when it is blocked. Tries the
     * requested direction first, then (only with collisions on) slides to the four neighbouring
     * directions in the original game's order: -1, +1, -2, +2 around the requested one.
     */
    public static Reservation reserve(
        Terrain terrain,
        float x,
        float y,
        int inputX,
        int inputY,
        boolean checkCollisions,
        float mapPixelWidth,
        float mapPixelHeight) {
      float targetX = reservationTarget(x, GRID_W, inputX);
      float targetY = reservationTarget(y, GRID_H, inputY);
      boolean primaryAccepted =
          isWithinMapBounds(mapPixelWidth, mapPixelHeight, targetX, targetY)
              && (!checkCollisions
                  || canReservePlayerStep(terrain, x, y, targetX, targetY, inputX, inputY));
      if (primaryAccepted) {
        return new Reservation(targetX, targetY, inputX, inputY);
      }
      if (checkCollisions) {
        return reserveAdjacentDirection(
            terrain, x, y, inputX, inputY, mapPixelWidth, mapPixelHeight);
      }
      return null;
    }

    /** The result of gliding toward a reservation: new position, whether it arrived, distance used. */
    public record Advance(float x, float y, boolean arrived, float spent) {}

    /** Moves toward the reserved tile by at most {@code budget} pixels (horizontal-equivalent). */
    public static Advance advance(float x, float y, Reservation step, float budget) {
      float remainingX = Math.abs(step.targetX() - x);
      float remainingY = Math.abs(step.targetY() - y) * GRID_W / GRID_H;
      float remainingDistance = Math.max(remainingX, remainingY);
      float pixelStep = Math.min(Math.max(0f, budget), remainingDistance);
      float toX = moveToward(x, step.targetX(), Math.abs(step.moveX()) * pixelStep);
      float toY =
          moveToward(y, step.targetY(), Math.abs(step.moveY()) * pixelStep * GRID_H / GRID_W);
      boolean arrived =
          Math.abs(toX - step.targetX()) < 0.001f && Math.abs(toY - step.targetY()) < 0.001f;
      if (arrived) {
        toX = step.targetX();
        toY = step.targetY();
      }
      return new Advance(toX, toY, arrived, pixelStep);
    }

    public static float reservationTarget(float value, float gridSize, int direction) {
      if (direction == 0) {
        return value;
      }
      return previousGridAnchor(value, gridSize, direction)
          + direction * gridSize * PLAYER_MOVEMENT_RESERVATION_STEP_TILES;
    }

    /** The original game's direction numbers: 1 = north, clockwise to 8 = north-west. */
    public static int toT4cDirection(int moveX, int moveY) {
      if (moveX == 0 && moveY < 0) return 1;
      if (moveX > 0 && moveY < 0) return 2;
      if (moveX > 0 && moveY == 0) return 3;
      if (moveX > 0 && moveY > 0) return 4;
      if (moveX == 0 && moveY > 0) return 5;
      if (moveX < 0 && moveY > 0) return 6;
      if (moveX < 0 && moveY == 0) return 7;
      if (moveX < 0 && moveY < 0) return 8;
      throw new IllegalArgumentException("A movement direction cannot be zero");
    }

    public static int[] fromT4cDirection(int direction) {
      return switch (direction) {
        case 1 -> new int[] {0, -1};
        case 2 -> new int[] {1, -1};
        case 3 -> new int[] {1, 0};
        case 4 -> new int[] {1, 1};
        case 5 -> new int[] {0, 1};
        case 6 -> new int[] {-1, 1};
        case 7 -> new int[] {-1, 0};
        case 8 -> new int[] {-1, -1};
        default ->
            throw new IllegalArgumentException("T4C movement direction must be between 1 and 8");
      };
    }

    public static boolean isWithinMapBounds(
        float mapPixelWidth, float mapPixelHeight, float targetX, float targetY) {
      return targetX >= 0f && targetY >= 0f && targetX < mapPixelWidth && targetY < mapPixelHeight;
    }

    private static Reservation reserveAdjacentDirection(
        Terrain terrain,
        float x,
        float y,
        int inputX,
        int inputY,
        float mapPixelWidth,
        float mapPixelHeight) {
      int requestedDirection = toT4cDirection(inputX, inputY);
      for (int offset = 1; offset <= 2; offset++) {
        for (int sign : new int[] {-1, 1}) {
          int[] move = fromT4cDirection(wrapT4cDirection(requestedDirection + sign * offset));
          float targetX = reservationTarget(x, GRID_W, move[0]);
          float targetY = reservationTarget(y, GRID_H, move[1]);
          if (isWithinMapBounds(mapPixelWidth, mapPixelHeight, targetX, targetY)
              && canReserveSlidingStep(terrain, x, y, targetX, targetY, inputX, inputY)) {
            return new Reservation(targetX, targetY, move[0], move[1]);
          }
        }
      }
      return null;
    }

    private static int wrapT4cDirection(int direction) {
      return ((direction - 1) % 8 + 8) % 8 + 1;
    }

    @FunctionalInterface
    private interface FootprintProbe {
      boolean rejects(float offsetX, float offsetY);
    }

    private static boolean footprintAccepts(
        Terrain terrain,
        float fromX,
        float fromY,
        float toX,
        float toY,
        int moveX,
        FootprintProbe probe) {
      if (!terrain.canMoveForPlayer(fromX, fromY, toX, toY)) {
        return false;
      }
      int horizontalTiles = moveX == 0 ? 0 : PLAYER_COLLISION_FOOTPRINT_HORIZONTAL_TILES;
      int verticalTiles = PLAYER_COLLISION_FOOTPRINT_ABOVE_TILES;
      int maxTiles = Math.max(horizontalTiles, verticalTiles);
      for (int tile = 1; tile <= maxTiles; tile++) {
        float offsetX = moveX * GRID_W * tile;
        float offsetY = -GRID_H * tile;
        boolean horizontal = tile <= horizontalTiles;
        boolean vertical = tile <= verticalTiles;
        if (horizontal && probe.rejects(offsetX, 0f)) {
          return false;
        }
        if (vertical && probe.rejects(0f, offsetY)) {
          return false;
        }
        if (horizontal && vertical && probe.rejects(offsetX, offsetY)) {
          return false;
        }
      }
      return true;
    }

    private static boolean canReservePlayerStep(
        Terrain terrain,
        float fromX,
        float fromY,
        float toX,
        float toY,
        int moveX,
        int moveY) {
      boolean diagonal = moveX != 0 && moveY != 0;
      boolean teleportDestination =
          terrain.isPlayerPassableTile((int) (toX / GRID_W), (int) (toY / GRID_H));
      return footprintAccepts(
          terrain,
          fromX,
          fromY,
          toX,
          toY,
          moveX,
          (offsetX, offsetY) -> {
            if (teleportDestination) {
              return false;
            }
            boolean destination = offsetX == 0f && offsetY == 0f;
            if (!(destination
                ? terrain.hasPlayerCollision(toX, toY)
                : terrain.hasCollision(toX + offsetX, toY + offsetY))) {
              return false;
            }
            boolean verticalOnly = offsetX == 0f && offsetY != 0f;
            return !(diagonal && verticalOnly && terrain.hasCollision(fromX, fromY + offsetY));
          });
    }

    private static boolean canReserveSlidingStep(
        Terrain terrain,
        float fromX,
        float fromY,
        float toX,
        float toY,
        int requestedX,
        int requestedY) {
      return footprintAccepts(
          terrain,
          fromX,
          fromY,
          toX,
          toY,
          requestedX,
          (offsetX, offsetY) ->
              !terrain.hasCollision(fromX + offsetX, fromY + offsetY)
                  && terrain.hasCollision(toX + offsetX, toY + offsetY));
    }

    private static float moveToward(float value, float target, float distance) {
      if (value < target) {
        return Math.min(value + distance, target);
      }
      return Math.max(value - distance, target);
    }

    private static float previousGridAnchor(float value, float gridSize, int direction) {
      float reservationSize = gridSize * PLAYER_MOVEMENT_RESERVATION_STEP_TILES;
      float grid = value / reservationSize;
      if (Math.abs(grid - Math.round(grid)) < POSITION_EPSILON) {
        return Math.round(grid) * reservationSize;
      }
      if (direction > 0) {
        return (float) Math.floor(grid) * reservationSize;
      }
      if (direction < 0) {
        return (float) Math.ceil(grid) * reservationSize;
      }
      return Math.round(grid) * reservationSize;
    }
  }
}
