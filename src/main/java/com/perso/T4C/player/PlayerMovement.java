package com.perso.T4C.player;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.PLAYER_COLLISION_FOOTPRINT_ABOVE_TILES;
import static com.perso.T4C.config.GameConstants.PLAYER_COLLISION_FOOTPRINT_BELOW_TILES;
import static com.perso.T4C.config.GameConstants.PLAYER_COLLISION_FOOTPRINT_HORIZONTAL_TILES;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.config.NativeTimingProfile;
import com.perso.T4C.helper.CollisionManager;
import com.perso.T4C.model.Coordinates;
import com.perso.T4C.world.MovementRules;
import java.util.ArrayDeque;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class PlayerMovement {
  private static final Logger log = LoggerFactory.getLogger(PlayerMovement.class);
  private static final long BLOCK_LOG_COOLDOWN_MS = 500L;
  private static final int LOADED_POSITION_RECOVERY_RADIUS = 32;
  private static final float POSITION_EPSILON = MovementRules.Grid.POSITION_EPSILON;
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
  @Getter(AccessLevel.NONE)
  private CollisionTerrain terrain;

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

  /**
   * Online puppets only: shows the walk or the rest pose of a player another client controls.
   * Nothing in single-player calls it.
   */
  public void puppetMoving(boolean moving) {
    this.moving = moving;
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
    float travelBudget =
        GameConstants.PLAYER_SPEED
            * Math.max(0f, delta)
            * player.getEffectiveSpeedMultiplier();
    boolean advancedThisUpdate = false;
    int safetyIterations = 0;
    while (true) {
      if (++safetyIterations > 256) {
        moving = advancedThisUpdate;
        return;
      }
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
      CollisionManager collisionManager = CollisionManager.getInstance();
      boolean checkCollisions =
          player.isPlayerCollisionsEnabled() && collisionManager.isInitialized();
      MovementRules.Reservation reservation =
          MovementRules.Grid.reserve(
              terrain(collisionManager),
              pos.getX(),
              pos.getY(),
              inputX,
              inputY,
              checkCollisions,
              player.getMapPixelWidth(),
              player.getMapPixelHeight());
      if (reservation != null) {
        reserveGridStep(pos, reservation);
      } else {
        maybeLogBlockedMove(
            collisionManager,
            MovementRules.Grid.reservationTarget(pos.getX(), GRID_W, inputX),
            MovementRules.Grid.reservationTarget(pos.getY(), GRID_H, inputY));
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

  private void reserveGridStep(Coordinates pos, MovementRules.Reservation reservation) {
    activeGridStep = true;
    activeStepTargetX = reservation.targetX();
    activeStepTargetY = reservation.targetY();
    activeStepMoveX = reservation.moveX();
    activeStepMoveY = reservation.moveY();
    activeStepExpectedX = pos.getX();
    activeStepExpectedY = pos.getY();
    activeStepExpectedZ = pos.getZ();
  }

  private CollisionTerrain terrain(CollisionManager collisionManager) {
    if (terrain == null || terrain.collisionManager != collisionManager) {
      terrain = new CollisionTerrain(collisionManager);
    }
    return terrain;
  }

  /** Lets the shared movement rule ask the desktop collision singleton its four questions. */
  private static final class CollisionTerrain implements MovementRules.Terrain {
    private final CollisionManager collisionManager;

    CollisionTerrain(CollisionManager collisionManager) {
      this.collisionManager = collisionManager;
    }

    @Override
    public boolean hasCollision(float worldX, float worldY) {
      return collisionManager.hasCollision(worldX, worldY);
    }

    @Override
    public boolean hasPlayerCollision(float worldX, float worldY) {
      return collisionManager.hasPlayerCollision(worldX, worldY);
    }

    @Override
    public boolean canMoveForPlayer(float fromX, float fromY, float toX, float toY) {
      return collisionManager.canMoveForPlayer(fromX, fromY, toX, toY);
    }

    @Override
    public boolean isPlayerPassableTile(int gridX, int gridY) {
      return collisionManager.isPlayerPassableTile(gridX, gridY);
    }
  }

  private float continueActiveGridStep(Player player, Coordinates pos, float travelBudget) {
    computeAngleFlip(activeStepMoveX, -activeStepMoveY);
    MovementRules.Reservation reservation =
        new MovementRules.Reservation(
            activeStepTargetX, activeStepTargetY, activeStepMoveX, activeStepMoveY);
    MovementRules.Grid.Advance advance =
        MovementRules.Grid.advance(pos.getX(), pos.getY(), reservation, travelBudget);
    player.setWorldPosition(advance.x(), advance.y(), pos.getZ());
    moving = true;
    activeStepExpectedX = advance.x();
    activeStepExpectedY = advance.y();
    activeStepExpectedZ = pos.getZ();
    if (advance.arrived()) {
      clearActiveGridStep();
      discardReachedPathNodes(player.getCoordinates());
    }
    return Math.max(0f, travelBudget - advance.spent());
  }

  private boolean isAtExpectedActiveStepPosition(Coordinates pos) {
    return Math.abs(pos.getX() - activeStepExpectedX) < POSITION_EPSILON
        && Math.abs(pos.getY() - activeStepExpectedY) < POSITION_EPSILON
        && pos.getZ() == activeStepExpectedZ;
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
