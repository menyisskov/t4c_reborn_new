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

  /**
   * A claimed destination tile and the unit direction used to reach it. The target is kept in region-local
   * <i>absolute</i> pixels (whole multiples of a tile, exact in a float up to 16 million) so it stays valid when the
   * tick's float frame moves; {@link #step} shifts it into the frame and back.
   */
  static final class Glide {
    private MovementRules.Reservation reservation;

    boolean active() {
      return reservation != null;
    }

    MovementRules.Reservation reservation() {
      return reservation;
    }

    void clear() {
      reservation = null;
    }
  }

  /**
   * The float frame of one tick: a whole number of tiles subtracted from the position before the shared rule runs in
   * float pixels, so a player at x = 5,845 steps with the precision of one at x = 200 (a float pixel is 0.016 px at
   * 187,000 px, 0.0005 px at 6,400 px). Block 0 has no shift, so every region narrower than {@link #FRAME_TILES}
   * (all non-streamed ones) steps bit-for-bit as before. Elsewhere the origin sits {@link #FRAME_MARGIN_TILES} tiles
   * before the block so a position that moves backwards across the block edge inside a tick keeps a positive local
   * coordinate (the rule truncates a pixel to its tile).
   */
  static final int FRAME_TILES = 256;
  static final int FRAME_MARGIN_TILES = 16;

  static int frameOrigin(double tile) {
    int block = (int) Math.floor(tile / FRAME_TILES);
    return block <= 0 ? 0 : block * FRAME_TILES - FRAME_MARGIN_TILES;
  }

  /** The shared rule's four terrain questions, answered from the hosted crop. */
  static final class Terrain implements MovementRules.Terrain {
    private final PreviewMap map;
    private final int offsetX;
    private final int offsetY;

    Terrain(PreviewMap map) {
      this(map, 0, 0);
    }

    private Terrain(PreviewMap map, int offsetX, int offsetY) {
      this.map = map;
      this.offsetX = offsetX;
      this.offsetY = offsetY;
    }

    /** The same questions asked in a frame whose origin is the given whole number of tiles into the map. */
    Terrain framed(int originTileX, int originTileY) {
      return originTileX == offsetX && originTileY == offsetY ? this : new Terrain(map, originTileX, originTileY);
    }

    @Override
    public boolean hasCollision(float worldX, float worldY) {
      return map.blocksFoot((int) (worldX / GRID_W) + offsetX, (int) (worldY / GRID_H) + offsetY);
    }

    @Override
    public boolean hasPlayerCollision(float worldX, float worldY) {
      int tileX = (int) (worldX / GRID_W) + offsetX;
      int tileY = (int) (worldY / GRID_H) + offsetY;
      return !map.portalPassable(tileX, tileY) && map.blocksFoot(tileX, tileY);
    }

    /** Players only ever claim one adjacent tile, so the offline sampled path check reduces to this. */
    @Override
    public boolean canMoveForPlayer(float fromX, float fromY, float toX, float toY) {
      return !hasPlayerCollision(toX, toY);
    }

    @Override
    public boolean isPlayerPassableTile(int gridX, int gridY) {
      return map.portalPassable(gridX + offsetX, gridY + offsetY);
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
    // The rule runs in float pixels; shift the frame so they stay small (see FRAME_TILES). The position is converted
    // from tile doubles once, the reservation (absolute) is shifted in and out, and the result shifts back in double.
    int ox = frameOrigin(x);
    int oy = frameOrigin(y);
    float shiftX = (float) ox * GRID_W;
    float shiftY = (float) oy * GRID_H;
    Terrain framed = terrain.framed(ox, oy);
    float px = pixelX(x - ox);
    float py = pixelY(y - oy);
    float mapWidth = (map.width - ox) * (float) GRID_W;
    float mapHeight = (map.height - oy) * (float) GRID_H;
    float budget = BUDGET_PER_TICK;
    MovementRules.Reservation held = glide.reservation;
    MovementRules.Reservation local = held == null ? null : shifted(held, -shiftX, -shiftY);
    for (int guard = 0; guard < 256; guard++) {
      if (local != null) {
        MovementRules.Grid.Advance advance = MovementRules.Grid.advance(px, py, local, budget);
        px = advance.x();
        py = advance.y();
        budget = Math.max(0f, budget - advance.spent());
        if (advance.arrived()) {
          local = null;
        }
        if (local != null || budget < EPSILON) {
          break;
        }
        continue;
      }
      if (dx == 0 && dy == 0) {
        break;
      }
      local = MovementRules.Grid.reserve(framed, px, py, dx, dy, true, mapWidth, mapHeight);
      if (local == null || budget < EPSILON) {
        break;
      }
    }
    glide.reservation = local == null ? null : shifted(local, shiftX, shiftY);
    return new Moved(tileX(px) + ox, tileY(py) + oy);
  }

  private static MovementRules.Reservation shifted(MovementRules.Reservation r, float byX, float byY) {
    return new MovementRules.Reservation(r.targetX() + byX, r.targetY() + byY, r.moveX(), r.moveY());
  }
}
