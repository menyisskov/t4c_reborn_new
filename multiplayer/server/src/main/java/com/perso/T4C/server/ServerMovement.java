package com.perso.T4C.server;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.PLAYER_SPEED;

import com.perso.T4C.world.MovementRules;

/**
 * The server's player step under the single-player rule ({@link MovementRules.Mode#GRID_RESERVATION}).
 *
 * <p><b>Convention.</b> The wire, the snapshots and the database keep tile-centre doubles in the
 * hosted crop (a player resting on tile (3,5) is at 3.5, 5.5). The shared rule works in world
 * pixels where a position is a tile's top-left anchor. The conversion is one fixed, exact offset:
 * {@code pixelX = (x - 0.5) * 32}, {@code pixelY = (y - 0.5) * 16}, and back. A resting player is
 * therefore always at a tile centre, exactly where the old loop left spawns, respawns and
 * transfers, so existing positions need no migration. Only a position saved mid-glide (the old
 * rule could stop anywhere; this one can be persisted between tiles by a checkpoint) is off the
 * centre, and {@link #anchor} snaps it to the centre of the tile it stands on, which was legal
 * under both rules.
 */
final class ServerMovement {
  /** System property that picks the rule: {@code grid} (default, single-player's) or {@code swept}. */
  static final String PROPERTY = "t4c.server.movement";
  /** Pixels one tick may cover (horizontal equivalent); derived from the shared player speed. */
  static final float BUDGET_PER_TICK = PLAYER_SPEED / PreviewWorld.TICK_RATE;

  private static final float EPSILON = 0.001f;
  private static final double CENTRE_TOLERANCE = 1e-9;

  private ServerMovement() {}

  static MovementRules.Mode defaultMode() {
    String value = System.getProperty(PROPERTY, "grid").trim().toLowerCase(java.util.Locale.ROOT);
    return switch (value) {
      case "grid", "grid_reservation", "" -> MovementRules.Mode.GRID_RESERVATION;
      case "swept", "swept_substep" -> MovementRules.Mode.SWEPT_SUBSTEP;
      default -> throw new IllegalStateException("Unknown " + PROPERTY + ": " + value);
    };
  }

  static float pixelX(double x) {
    return (float) ((x - 0.5) * GRID_W);
  }

  static float pixelY(double y) {
    return (float) ((y - 0.5) * GRID_H);
  }

  static double tileX(float pixelX) {
    return pixelX / GRID_W + 0.5;
  }

  static double tileY(float pixelY) {
    return pixelY / GRID_H + 0.5;
  }

  /** The centre of the tile a position stands on (the legal anchor a mid-tile position snaps to). */
  static double anchor(double coordinate) {
    double centre = Math.floor(coordinate) + 0.5;
    return Math.abs(coordinate - centre) <= CENTRE_TOLERANCE ? coordinate : centre;
  }

  /** A claimed destination tile (local pixels) and the unit direction used to reach it. */
  static final class Glide {
    private MovementRules.Reservation reservation;

    boolean active() {
      return reservation != null;
    }

    void clear() {
      reservation = null;
    }
  }

  /** The shared rule's four terrain questions, answered from the hosted crop. */
  static final class Terrain implements MovementRules.Terrain {
    private final PreviewMap map;

    Terrain(PreviewMap map) {
      this.map = map;
    }

    @Override
    public boolean hasCollision(float worldX, float worldY) {
      return map.blocksFoot((int) (worldX / GRID_W), (int) (worldY / GRID_H));
    }

    @Override
    public boolean hasPlayerCollision(float worldX, float worldY) {
      int tileX = (int) (worldX / GRID_W);
      int tileY = (int) (worldY / GRID_H);
      return !map.portalPassable(tileX, tileY) && map.blocksFoot(tileX, tileY);
    }

    /** Players only ever claim one adjacent tile, so the offline sampled path check reduces to this. */
    @Override
    public boolean canMoveForPlayer(float fromX, float fromY, float toX, float toY) {
      return !hasPlayerCollision(toX, toY);
    }

    @Override
    public boolean isPlayerPassableTile(int gridX, int gridY) {
      return map.portalPassable(gridX, gridY);
    }
  }

  /** One player's position in tile-centre doubles after the tick. */
  record Moved(double x, double y) {}

  /**
   * One server tick of the grid rule: finish the reserved tile, then (input still held) reserve the
   * next, until the tick's distance budget is spent. Mirrors {@code PlayerMovement.move} with the
   * server's fixed delta; unlike the old loop a released key does not stop the player mid-tile.
   */
  static Moved step(PreviewMap map, Terrain terrain, Glide glide, double x, double y, int dx, int dy) {
    float px = pixelX(x);
    float py = pixelY(y);
    float mapWidth = map.width * (float) GRID_W;
    float mapHeight = map.height * (float) GRID_H;
    float budget = BUDGET_PER_TICK;
    for (int guard = 0; guard < 256; guard++) {
      if (glide.reservation != null) {
        MovementRules.Grid.Advance advance = MovementRules.Grid.advance(px, py, glide.reservation, budget);
        px = advance.x();
        py = advance.y();
        budget = Math.max(0f, budget - advance.spent());
        if (advance.arrived()) {
          glide.reservation = null;
        }
        if (glide.reservation != null || budget < EPSILON) {
          break;
        }
        continue;
      }
      if (dx == 0 && dy == 0) {
        break;
      }
      glide.reservation =
          MovementRules.Grid.reserve(terrain, px, py, dx, dy, true, mapWidth, mapHeight);
      if (glide.reservation == null || budget < EPSILON) {
        break;
      }
    }
    return new Moved(tileX(px), tileY(py));
  }
}
