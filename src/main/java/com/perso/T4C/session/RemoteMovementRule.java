package com.perso.T4C.session;

import static com.perso.T4C.config.GameConstants.PLAYER_SPEED;

import com.perso.T4C.helper.CollisionManager;
import com.perso.T4C.player.Player;
import com.perso.T4C.world.MovementReconciler;
import com.perso.T4C.world.MovementRules;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * The shared single-player grid rule ({@link MovementRules.Grid}) as a stateless replay step for
 * {@link MovementReconciler}: from a pixel position and one frame of input, reserve the next tile
 * and glide toward it with the frame's budget, as many tiles as the budget pays for. The server
 * runs the same rule (T4C-0277), so a replay from its position lands where it will.
 *
 * <p>Statelessness is the one approximation: a glide interrupted at a frame boundary is not
 * remembered, so the next frame reserves again from the middle of the tile. In the held direction
 * that is the same target, so the path is the same; after a reversal the real movement finishes
 * its reserved tile first and the replay turns at once. The difference is below one tile and the
 * reconciler's tolerance absorbs it.
 */
final class RemoteMovementRule implements MovementReconciler.Rule {
  private static final int MAX_TILES_PER_FRAME = 8;

  private final MovementRules.Terrain terrain;
  private final BooleanSupplier collisions;
  private final Supplier<Player> player;

  RemoteMovementRule(
      MovementRules.Terrain terrain, BooleanSupplier collisions, Supplier<Player> player) {
    this.terrain = terrain;
    this.collisions = collisions;
    this.player = player;
  }

  @Override
  public MovementReconciler.Position apply(
      MovementReconciler.Position from, MovementReconciler.Input input) {
    if (input.dx() == 0 && input.dy() == 0) return from;
    Player p = player.get();
    float multiplier = p == null ? 1f : p.getEffectiveSpeedMultiplier();
    float budget = PLAYER_SPEED * (float) Math.max(0d, input.dt()) * multiplier;
    float width = p == null || p.getMapPixelWidth() <= 0 ? Float.MAX_VALUE : p.getMapPixelWidth();
    float height =
        p == null || p.getMapPixelHeight() <= 0 ? Float.MAX_VALUE : p.getMapPixelHeight();
    boolean check = collisions.getAsBoolean();
    float x = (float) from.x();
    float y = (float) from.y();
    for (int i = 0; i < MAX_TILES_PER_FRAME && budget >= MovementRules.Grid.POSITION_EPSILON; i++) {
      MovementRules.Reservation step =
          MovementRules.Grid.reserve(terrain, x, y, input.dx(), input.dy(), check, width, height);
      if (step == null) break;
      MovementRules.Grid.Advance advance = MovementRules.Grid.advance(x, y, step, budget);
      x = advance.x();
      y = advance.y();
      budget -= advance.spent();
      if (!advance.arrived()) break;
    }
    return new MovementReconciler.Position(x, y);
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
