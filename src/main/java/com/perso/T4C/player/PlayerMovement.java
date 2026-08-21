package com.perso.T4C.player;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.PLAYER_COLLISION_FOOTPRINT_ABOVE_TILES;
import static com.perso.T4C.config.GameConstants.PLAYER_COLLISION_FOOTPRINT_BELOW_TILES;
import static com.perso.T4C.config.GameConstants.PLAYER_COLLISION_FOOTPRINT_HORIZONTAL_TILES;
import static com.perso.T4C.config.GameConstants.PLAYER_MOVEMENT_RESERVATION_STEP_TILES;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.config.NativeTimingProfile;
import com.perso.T4C.helper.CollisionManager;
import com.perso.T4C.model.Coordinates;
import java.util.ArrayDeque;
import java.util.List;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class PlayerMovement {
  private static final Logger log = LoggerFactory.getLogger(PlayerMovement.class);
  private static final long BLOCK_LOG_COOLDOWN_MS = 500L;
  private static final int LOADED_POSITION_RECOVERY_RADIUS = 32;
  private static final float POSITION_EPSILON = 0.001f;
  private String currentAngle = "000";
  private boolean moving = false;
  private boolean flipX = false;
  private final ArrayDeque<PathNode> path = new ArrayDeque<>();
  private long lastBlockLogAt = 0L;
  private boolean activeGridStep = false;
  private float activeStepTargetX = 0f;
  private float activeStepTargetY = 0f;
  private int activeStepMoveX = 0;
  private int activeStepMoveY = 0;
  private float activeStepExpectedX = 0f;
  private float activeStepExpectedY = 0f;
  private int activeStepExpectedZ = 0;

  public static final class PathNode {
    public final int x;
    public final int y;

    public PathNode(int x, int y) {
      this.x = x;
      this.y = y;
    }
  }

  public void setPath(List<PathNode> nodes) {
    path.clear();
    if (nodes != null) {
      path.addAll(nodes);
    }
  }

  public void clearPath() {
    path.clear();
  }

  public void stop() {
    moving = false;
    path.clear();
    clearActiveGridStep();
  }

  public boolean recoverLoadedCollisionPosition(Player player) {
    CollisionManager collisionManager = CollisionManager.getInstance();
    if (player == null
        || !player.isPlayerCollisionsEnabled()
        || !collisionManager.isInitialized()) {
      return false;
    }
    Coordinates pos = player.getCoordinates();
    int originX = Math.round(pos.getX() / GRID_W);
    int originY = Math.round(pos.getY() / GRID_H);
    if (!hasStaticPlayerFootprintCollision(collisionManager, originX, originY)) {
      return false;
    }
    int bestX = Integer.MIN_VALUE;
    int bestY = Integer.MIN_VALUE;
    int bestDistanceSquared = Integer.MAX_VALUE;
    for (int radius = 1; radius <= LOADED_POSITION_RECOVERY_RADIUS; radius++) {
      for (int y = originY - radius; y <= originY + radius; y++) {
        for (int x = originX - radius; x <= originX + radius; x++) {
          if (Math.max(Math.abs(x - originX), Math.abs(y - originY)) != radius
              || hasStaticPlayerFootprintCollision(collisionManager, x, y)) {
            continue;
          }
          int dx = x - originX;
          int dy = y - originY;
          int distanceSquared = dx * dx + dy * dy;
          if (distanceSquared < bestDistanceSquared) {
            bestX = x;
            bestY = y;
            bestDistanceSquared = distanceSquared;
          }
        }
      }
      if (bestX != Integer.MIN_VALUE) {
        break;
      }
    }
    if (bestX == Integer.MIN_VALUE) {
      log.warn(
          "Could not recover loaded player collision position at tile=({},{})", originX, originY);
      return false;
    }
    player.setWorldPosition(bestX * GRID_W, bestY * GRID_H, pos.getZ());
    clearActiveGridStep();
    moving = false;
    log.info(
        "Recovered loaded player collision position from tile=({},{}) to tile=({},{})",
        originX,
        originY,
        bestX,
        bestY);
    return true;
  }

  public void move(Player player, float dx, float dy, float delta) {
    boolean directInput = dx != 0f || dy != 0f;
    if (directInput) {
      path.clear();
    }
    NativeTimingProfile timing = NativeTimingProfile.current();
    // continueActiveGridStep multiplies this unit budget by MOVX/MOVY.
    // Convert the existing world speed to native movement units so changing
    // the profile does not unexpectedly slow the player down.
    float travelBudget =
        GameConstants.PLAYER_SPEED
            * Math.max(0f, delta)
            / timing.movX()
            * player.getEffectiveSpeedMultiplier();
    boolean advancedThisUpdate = false;
    while (true) {
      Coordinates pos = player.getCoordinates();
      if (activeGridStep && !isAtExpectedActiveStepPosition(pos)) {
        clearActiveGridStep();
      }
      if (activeGridStep) {
        float beforeX = pos.getX();
        float beforeY = pos.getY();
        travelBudget = continueActiveGridStep(player, pos, travelBudget);
        Coordinates advancedPos = player.getCoordinates();
        advancedThisUpdate |=
            Math.abs(advancedPos.getX() - beforeX) >= POSITION_EPSILON
                || Math.abs(advancedPos.getY() - beforeY) >= POSITION_EPSILON;
        if (activeGridStep || travelBudget < POSITION_EPSILON) {
          return;
        }
        continue;
      }
      float requestedDx = dx;
      float requestedDy = dy;
      if (!directInput) {
        discardReachedPathNodes(pos);
        if (path.isEmpty()) {
          if (!advancedThisUpdate) {
            moving = false;
          }
          return;
        }
        PathNode target = path.peekFirst();
        requestedDx = Math.signum(target.x * GRID_W - pos.getX());
        requestedDy =
            Math.signum(target.y * GRID_H - pos.getY()) * ((float) GRID_H / (float) GRID_W);
      }
      int inputX = (int) Math.signum(requestedDx);
      int inputY = (int) Math.signum(requestedDy);
      if (inputX == 0 && inputY == 0) {
        if (!advancedThisUpdate) {
          moving = false;
        }
        return;
      }
      float targetX = reservationTarget(pos.getX(), GRID_W, inputX);
      float targetY = reservationTarget(pos.getY(), GRID_H, inputY);
      CollisionManager collisionManager = CollisionManager.getInstance();
      boolean checkCollisions =
          player.isPlayerCollisionsEnabled() && collisionManager.isInitialized();
      boolean primaryAccepted =
          isWithinMapBounds(player, targetX, targetY)
              && (!checkCollisions
                  || canReservePlayerStep(
                      collisionManager, pos.getX(), pos.getY(), targetX, targetY, inputX, inputY));
      if (primaryAccepted) {
        reserveGridStep(pos, targetX, targetY, inputX, inputY);
      } else if (checkCollisions
          && tryReserveAdjacentDirection(player, collisionManager, pos, inputX, inputY)) {
      } else {
        maybeLogBlockedMove(collisionManager, targetX, targetY);
        if (!advancedThisUpdate) {
          moving = false;
        }
        return;
      }
      computeAngleFlip(activeStepMoveX, -activeStepMoveY);
      if (travelBudget < POSITION_EPSILON) {
        moving = true;
        return;
      }
    }
  }

  private boolean tryReserveAdjacentDirection(
      Player player, CollisionManager collisionManager, Coordinates pos, int inputX, int inputY) {
    int requestedDirection = toT4cDirection(inputX, inputY);
    for (int offset = 1; offset <= 2; offset++) {
      for (int sign : new int[] {-1, 1}) {
        int[] move = fromT4cDirection(wrapT4cDirection(requestedDirection + sign * offset));
        if (tryReserveSlideStep(player, collisionManager, pos, move[0], move[1], inputX, inputY)) {
          return true;
        }
      }
    }
    return false;
  }

  private int wrapT4cDirection(int direction) {
    return ((direction - 1) % 8 + 8) % 8 + 1;
  }

  private boolean tryReserveSlideStep(
      Player player,
      CollisionManager collisionManager,
      Coordinates pos,
      int moveX,
      int moveY,
      int requestedX,
      int requestedY) {
    float targetX = reservationTarget(pos.getX(), GRID_W, moveX);
    float targetY = reservationTarget(pos.getY(), GRID_H, moveY);
    if (!isWithinMapBounds(player, targetX, targetY)
        || !canReserveSlidingStep(
            collisionManager, pos.getX(), pos.getY(), targetX, targetY, requestedX, requestedY)) {
      return false;
    }
    reserveGridStep(pos, targetX, targetY, moveX, moveY);
    return true;
  }

  @FunctionalInterface
  private interface FootprintProbe {
    boolean rejects(float offsetX, float offsetY);
  }

  private boolean footprintAccepts(
      CollisionManager collisionManager,
      float fromX,
      float fromY,
      float toX,
      float toY,
      int moveX,
      int moveY,
      FootprintProbe probe) {
    if (!collisionManager.canMoveForPlayer(fromX, fromY, toX, toY)) {
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

  private boolean canReservePlayerStep(
      CollisionManager collisionManager,
      float fromX,
      float fromY,
      float toX,
      float toY,
      int moveX,
      int moveY) {
    boolean diagonal = moveX != 0 && moveY != 0;
    boolean teleportDestination =
        collisionManager.isPlayerPassableTile((int) (toX / GRID_W), (int) (toY / GRID_H));
    return footprintAccepts(
        collisionManager,
        fromX,
        fromY,
        toX,
        toY,
        moveX,
        moveY,
        (offsetX, offsetY) -> {
          if (teleportDestination) {
            return false;
          }
          boolean destination = offsetX == 0f && offsetY == 0f;
          if (!(destination
              ? collisionManager.hasPlayerCollision(toX, toY)
              : collisionManager.hasCollision(toX + offsetX, toY + offsetY))) {
            return false;
          }
          boolean verticalOnly = offsetX == 0f && offsetY != 0f;
          return !(diagonal
              && verticalOnly
              && collisionManager.hasCollision(fromX, fromY + offsetY));
        });
  }

  private boolean canReserveSlidingStep(
      CollisionManager collisionManager,
      float fromX,
      float fromY,
      float toX,
      float toY,
      int moveX,
      int moveY) {
    return footprintAccepts(
        collisionManager,
        fromX,
        fromY,
        toX,
        toY,
        moveX,
        moveY,
        (offsetX, offsetY) ->
            !collisionManager.hasCollision(fromX + offsetX, fromY + offsetY)
                && collisionManager.hasCollision(toX + offsetX, toY + offsetY));
  }

  private boolean hasStaticPlayerFootprintCollision(
      CollisionManager collisionManager, int gridX, int gridY) {
    if (collisionManager.hasStaticCollisionAtGrid(gridX, gridY)) {
      return true;
    }
    for (int x = gridX - PLAYER_COLLISION_FOOTPRINT_HORIZONTAL_TILES;
        x <= gridX + PLAYER_COLLISION_FOOTPRINT_HORIZONTAL_TILES;
        x++) {
      for (int y = gridY - PLAYER_COLLISION_FOOTPRINT_ABOVE_TILES;
          y <= gridY + PLAYER_COLLISION_FOOTPRINT_BELOW_TILES;
          y++) {
        if (collisionManager.hasStaticCollisionAtGrid(x, y)) {
          return true;
        }
      }
    }
    return false;
  }

  private int toT4cDirection(int moveX, int moveY) {
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

  private int[] fromT4cDirection(int direction) {
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

  private void reserveGridStep(
      Coordinates pos, float targetX, float targetY, int moveX, int moveY) {
    activeGridStep = true;
    activeStepTargetX = targetX;
    activeStepTargetY = targetY;
    activeStepMoveX = moveX;
    activeStepMoveY = moveY;
    activeStepExpectedX = pos.getX();
    activeStepExpectedY = pos.getY();
    activeStepExpectedZ = pos.getZ();
  }

  private float continueActiveGridStep(Player player, Coordinates pos, float travelBudget) {
    computeAngleFlip(activeStepMoveX, -activeStepMoveY);
    float remainingX = Math.abs(activeStepTargetX - pos.getX());
    float remainingY = Math.abs(activeStepTargetY - pos.getY()) * GRID_W / GRID_H;
    float remainingDistance = Math.max(remainingX, remainingY);
    float pixelStep = Math.min(Math.max(0f, travelBudget), remainingDistance);
    float toX = moveToward(pos.getX(), activeStepTargetX, Math.abs(activeStepMoveX) * pixelStep);
    float toY =
        moveToward(
            pos.getY(), activeStepTargetY, Math.abs(activeStepMoveY) * pixelStep * GRID_H / GRID_W);
    boolean arrived =
        Math.abs(toX - activeStepTargetX) < 0.001f && Math.abs(toY - activeStepTargetY) < 0.001f;
    if (arrived) {
      toX = activeStepTargetX;
      toY = activeStepTargetY;
    }
    player.setWorldPosition(toX, toY, pos.getZ());
    moving = true;
    activeStepExpectedX = toX;
    activeStepExpectedY = toY;
    activeStepExpectedZ = pos.getZ();
    if (arrived) {
      clearActiveGridStep();
      discardReachedPathNodes(player.getCoordinates());
    }
    return Math.max(0f, travelBudget - pixelStep);
  }

  private float moveToward(float value, float target, float distance) {
    if (value < target) {
      return Math.min(value + distance, target);
    }
    return Math.max(value - distance, target);
  }

  private boolean isAtExpectedActiveStepPosition(Coordinates pos) {
    return Math.abs(pos.getX() - activeStepExpectedX) < POSITION_EPSILON
        && Math.abs(pos.getY() - activeStepExpectedY) < POSITION_EPSILON
        && pos.getZ() == activeStepExpectedZ;
  }

  private boolean isWithinMapBounds(Player player, float targetX, float targetY) {
    return targetX >= 0f
        && targetY >= 0f
        && targetX < player.getMapPixelWidth()
        && targetY < player.getMapPixelHeight();
  }

  private float reservationTarget(float value, float gridSize, int direction) {
    if (direction == 0) {
      return value;
    }
    return previousGridAnchor(value, gridSize, direction)
        + direction * gridSize * PLAYER_MOVEMENT_RESERVATION_STEP_TILES;
  }

  private float previousGridAnchor(float value, float gridSize, int direction) {
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

  private void discardReachedPathNodes(Coordinates pos) {
    while (!path.isEmpty()) {
      PathNode target = path.peekFirst();
      if (Math.abs(pos.getX() - target.x * GRID_W) >= 0.001f
          || Math.abs(pos.getY() - target.y * GRID_H) >= 0.001f) {
        return;
      }
      path.pollFirst();
    }
  }

  private void clearActiveGridStep() {
    activeGridStep = false;
    activeStepTargetX = 0f;
    activeStepTargetY = 0f;
    activeStepMoveX = 0;
    activeStepMoveY = 0;
    activeStepExpectedX = 0f;
    activeStepExpectedY = 0f;
    activeStepExpectedZ = 0;
  }

  private void maybeLogBlockedMove(CollisionManager collisionManager, float worldX, float worldY) {
    long now = System.currentTimeMillis();
    if (now - lastBlockLogAt < BLOCK_LOG_COOLDOWN_MS) {
      return;
    }
    lastBlockLogAt = now;
    int gridX = (int) (worldX / GRID_W);
    int gridY = (int) (worldY / GRID_H);
    int value = collisionManager.getCollisionValue(worldX, worldY);
    log.info(
        "Move blocked at world=({},{}) tile=({},{}), collisionValue={}, collisionMap={}x{}",
        (int) worldX,
        (int) worldY,
        gridX,
        gridY,
        value,
        collisionManager.getCollisionWidth(),
        collisionManager.getCollisionHeight());
  }

  private void computeAngleFlip(float dx, float worldDy) {
    if (dx == 0 && worldDy > 0) {
      currentAngle = "180";
      flipX = false;
    } else if (dx == 0 && worldDy < 0) {
      currentAngle = "000";
      flipX = false;
    } else if (dx < 0 && worldDy == 0) {
      currentAngle = "090";
      flipX = false;
    } else if (dx > 0 && worldDy == 0) {
      currentAngle = "090";
      flipX = true;
    } else if (dx < 0 && worldDy > 0) {
      currentAngle = "135";
      flipX = false;
    } else if (dx > 0 && worldDy > 0) {
      currentAngle = "135";
      flipX = true;
    } else if (dx < 0 && worldDy < 0) {
      currentAngle = "045";
      flipX = false;
    } else if (dx > 0 && worldDy < 0) {
      currentAngle = "045";
      flipX = true;
    }
  }

  public void faceToward(float fromX, float fromY, float toX, float toY) {
    float dx = toX - fromX;
    float dy = toY - fromY;
    if (dx == 0f && dy == 0f) {
      return;
    }
    float worldDy = -dy;
    computeAngleFlip(dx, worldDy);
  }
}
