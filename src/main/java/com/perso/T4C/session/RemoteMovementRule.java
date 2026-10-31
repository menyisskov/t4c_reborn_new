package com.perso.T4C.session;

import static com.perso.T4C.config.GameConstants.PLAYER_SPEED;

import com.perso.T4C.helper.CollisionManager;
import com.perso.T4C.player.Player;
import com.perso.T4C.world.MovementReconciler;
import com.perso.T4C.world.MovementRules;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * The shared single-player grid rule ({@link MovementRules.Grid}) as a replay step for
 * {@link MovementReconciler}: from a pixel position and one frame of input, reserve the next tile
 * and glide toward it with the frame's budget, as many tiles as the budget pays for. The server
 * runs the same rule (T4C-0277), so a replay from its position lands where it will.
 *
 * The active reservation survives frame boundaries and is restored from each authoritative
 * snapshot before pending inputs are replayed, just as offline and server movement retain it.
 */
final class RemoteMovementRule implements MovementReconciler.Rule {
  private static final int MAX_TILES_PER_FRAME = 256;

  private final MovementRules.Terrain terrain;
  private final BooleanSupplier collisions;
  private final Supplier<Player> player;
  private MovementRules.Reservation held;

  RemoteMovementRule(
      MovementRules.Terrain terrain, BooleanSupplier collisions, Supplier<Player> player) {
    this.terrain = terrain;
    this.collisions = collisions;
    this.player = player;
  }

  @Override
  public MovementReconciler.Position apply(
      MovementReconciler.Position from, MovementReconciler.Input input) {
    Player p = player.get();
    float multiplier = p == null ? 1f : p.getEffectiveSpeedMultiplier();
    float budget = PLAYER_SPEED * (float) Math.max(0d, input.dt()) * multiplier;
    float width = p == null || p.getMapPixelWidth() <= 0 ? Float.MAX_VALUE : p.getMapPixelWidth();
    float height =
        p == null || p.getMapPixelHeight() <= 0 ? Float.MAX_VALUE : p.getMapPixelHeight();
    boolean check = collisions.getAsBoolean();
    float x = (float) from.x();
    float y = (float) from.y();
    for (int i = 0; i < MAX_TILES_PER_FRAME; i++) {
      if (held == null) {
        if (input.dx() == 0 && input.dy() == 0) break;
        held = MovementRules.Grid.reserve(terrain, x, y, input.dx(), input.dy(), check, width, height);
        if (held == null) break;
      }
      if (budget < MovementRules.Grid.POSITION_EPSILON) break;
      MovementRules.Grid.Advance advance = MovementRules.Grid.advance(x, y, held, budget);
      x = advance.x();
      y = advance.y();
      budget -= advance.spent();
      if (advance.arrived()) held = null;
      if (!advance.arrived()) break;
    }
    return new MovementReconciler.Position(x, y);
  }

  @Override
  public void restore(MovementRules.Reservation reservation) {
    held = reservation;
  }

  MovementRules.Reservation currentReservation() {
    return held;
  }

  /** The desktop collision singleton as the rule's terrain (the same four questions as offline). */
  static MovementRules.Terrain desktopTerrain() {
    return new MovementRules.Terrain() {
      @Override
      public boolean hasCollision(float worldX, float worldY) {
        return CollisionManager.getInstance().hasCollision(worldX, worldY);
      }

      @Override
      public boolean hasPlayerCollision(float worldX, float worldY) {
        return CollisionManager.getInstance().hasPlayerCollision(worldX, worldY);
      }

      @Override
      public boolean canMoveForPlayer(float fromX, float fromY, float toX, float toY) {
        return CollisionManager.getInstance().canMoveForPlayer(fromX, fromY, toX, toY);
      }

      @Override
      public boolean isPlayerPassableTile(int gridX, int gridY) {
        return CollisionManager.getInstance().isPlayerPassableTile(gridX, gridY);
      }
    };
  }
}
