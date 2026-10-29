package com.perso.T4C.server;

import static com.perso.T4C.config.GameConstants.*;

import com.perso.T4C.helper.BinaryIOUtils;
import com.perso.T4C.world.CollisionRules;
import com.perso.T4C.world.LineOfSightRules;
import com.perso.T4C.world.WorldCollision;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Path;

/** A bounded starting-area view of the real collision file; never loads desktop saves or assets. */
public final class PreviewMap {
  private final boolean[][] blocked;
  private final byte[][] terrain;
  /** Movement-blocking tiles without the tile-above fold: the shared grid rule asks per tile. */
  private final boolean[][] footBlocked;
  /** Authored portal source tiles players may stand on (offline "player passable"); may be null. */
  private final boolean[][] portalPassable;
  public final int originX;
  public final int originY;
  public final int width;
  public final int height;
  public final double spawnX;
  public final double spawnY;

  PreviewMap(boolean[][] blocked, int originX, int originY, int spawnX, int spawnY) {
    this(blocked, null, originX, originY, spawnX, spawnY);
  }

  PreviewMap(boolean[][] blocked, byte[][] terrain, int originX, int originY, int spawnX, int spawnY) {
    this(blocked, terrain, null, null, originX, originY, spawnX, spawnY);
  }

  /**
   * {@code footBlocked} is the per-tile static movement blocking (no above-foot fold; null means
   * "use blocked as given", which is exact for maps built by hand). {@code portalPassable} marks
   * portal source tiles the movement rule treats as passable.
   */
  PreviewMap(boolean[][] blocked, byte[][] terrain, boolean[][] footBlocked, boolean[][] portalPassable,
      int originX, int originY, int spawnX, int spawnY) {
    height = blocked.length;
    if (height == 0 || blocked[0].length == 0) throw new IllegalArgumentException("Empty map");
    width = blocked[0].length;
    this.blocked = new boolean[height][];
    if (terrain != null && terrain.length != height) throw new IllegalArgumentException("Invalid terrain");
    this.terrain = new byte[height][width];
    for (int y = 0; y < height; y++) {
      if (blocked[y].length != width) throw new IllegalArgumentException("Nonrectangular map");
      this.blocked[y] = blocked[y].clone();
      if (terrain != null) {
        if (terrain[y].length != width) throw new IllegalArgumentException("Invalid terrain");
        this.terrain[y] = terrain[y].clone();
      } else {
        for (int x = 0; x < width; x++) this.terrain[y][x] = (byte) (blocked[y][x] ? 1 : 0);
      }
    }
    this.footBlocked = new boolean[height][];
    for (int y = 0; y < height; y++) {
      boolean[] row = footBlocked == null ? blocked[y] : footBlocked[y];
      if (row.length != width) throw new IllegalArgumentException("Invalid foot grid");
      this.footBlocked[y] = row.clone();
    }
    if (portalPassable == null) {
      this.portalPassable = null;
    } else {
      this.portalPassable = new boolean[height][];
      for (int y = 0; y < height; y++) {
        if (portalPassable[y].length != width) throw new IllegalArgumentException("Invalid portal grid");
        this.portalPassable[y] = portalPassable[y].clone();
      }
    }
    this.originX = originX;
    this.originY = originY;
    this.spawnX = spawnX + 0.5;
    this.spawnY = spawnY + 0.5;
    if (!canStand(this.spawnX, this.spawnY)) throw new IllegalArgumentException("Blocked spawn");
  }

  public static PreviewMap load(Path path) throws IOException {
    try (var in = new DataInputStream(BinaryIOUtils.openInputStream(path.toFile(), 1 << 20))) {
      int w = BinaryIOUtils.readIntLE(in);
      int h = BinaryIOUtils.readIntLE(in);
      long size = (long) w * h;
      if (w <= 0 || h <= 0 || w > 32768 || h > 32768 || size > 64L * 1024 * 1024)
        throw new IOException("Collision map exceeds preview dimensions or memory limit");
      byte[] data = in.readNBytes((int) size);
      if (data.length != size) throw new IOException("Truncated collision map");
      var world = new WorldCollision(w, h,
          (x, y) -> CollisionRules.blocksMovement(data[y * w + x] & 255),
          (x, y) -> CollisionRules.blocksLineOfSight(data[y * w + x] & 255));
      int ox = NEW_CHARACTER_START_TILE_X - 40;
      int oy = NEW_CHARACTER_START_TILE_Y - 30;
      if (ox < 0 || oy < 0 || ox + 80 > w || oy + 60 > h)
        throw new IOException("Map does not contain the game's starting area");
      boolean[][] cells = new boolean[60][80];
      byte[][] terrain = new byte[60][80];
      boolean[][] foot = new boolean[60][80];
      for (int y = 0; y < cells.length; y++)
        for (int x = 0; x < cells[y].length; x++) {
          terrain[y][x] = data[(oy + y) * w + ox + x];
          foot[y][x] = !world.contains(ox + x, oy + y) || world.blocksMovement(ox + x, oy + y);
          // Include the existing above-foot clearance. Portals and sliding are not in this preview.
          for (int above = 0; above <= PLAYER_COLLISION_FOOTPRINT_ABOVE_TILES; above++)
            cells[y][x] |= !world.contains(ox + x, oy + y - above)
                || world.blocksMovement(ox + x, oy + y - above);
        }
      // The raw starting tile can sit on an offline portal exception. Pick the nearest clear tile.
      for (int radius = 0; radius < 20; radius++)
        for (int y = 30 - radius; y <= 30 + radius; y++)
          for (int x = 40 - radius; x <= 40 + radius; x++)
            if (Math.max(Math.abs(x - 40), Math.abs(y - 30)) == radius && !cells[y][x])
              return new PreviewMap(cells, terrain, foot, null, ox, oy, x, y);
      throw new IOException("No safe starting position in preview area");
    }
  }

  /** Local tile; tiles outside this map block. Static terrain only (no actors, no above-foot fold). */
  boolean blocksFoot(int tileX, int tileY) {
    return tileX < 0 || tileY < 0 || tileX >= width || tileY >= height || footBlocked[tileY][tileX];
  }

  /** Local tile; true for authored portal sources, which the offline rule lets players enter. */
  boolean portalPassable(int tileX, int tileY) {
    return portalPassable != null && tileX >= 0 && tileY >= 0 && tileX < width && tileY < height
        && portalPassable[tileY][tileX];
  }

  public boolean canStand(double x, double y) {
    return contains(x, y) && !blocked[(int) y][(int) x];
  }

  /**
   * Grid rule (single-player's): a player may rest on any tile the movement rule lets them enter, which looks at the static
   * foot collision (or an authored portal source), not at the stricter above-foot fold {@link #canStand} applies. The
   * wall-slide step can enter a tile whose tile above is blocked when the departure tile had the same block.
   */
  public boolean canRestOnGrid(double x, double y) {
    return contains(x, y) && restTile((int) x, (int) y);
  }

  private boolean restTile(int tileX, int tileY) {
    return tileX >= 0 && tileY >= 0 && tileX < width && tileY < height
        && (!footBlocked[tileY][tileX] || portalPassable(tileX, tileY));
  }

  private static final double LATTICE_EPSILON = 2e-3;

  /**
   * True when a position (local tile units) lies where the grid rule can put a player: at the centre of a tile it may rest
   * on, or on the straight glide between the centres of two adjacent tiles (diagonals included) that it may rest on.
   * Anything else, for example walking along a row boundary or through a blocked tile, is not reachable by the rule.
   */
  public boolean onGridPath(double x, double y) {
    if (!contains(x, y)) return false;
    double ex = x - .5, ey = y - .5;
    int bx = (int) Math.floor(ex), by = (int) Math.floor(ey);
    double fx = ex - bx, fy = ey - by;
    boolean atX0 = fx < LATTICE_EPSILON, atX1 = fx > 1 - LATTICE_EPSILON;
    boolean atY0 = fy < LATTICE_EPSILON, atY1 = fy > 1 - LATTICE_EPSILON;
    if ((atX0 || atX1) && (atY0 || atY1)) return restTile(bx + (atX1 ? 1 : 0), by + (atY1 ? 1 : 0));
    if (atX0 || atX1) {
      int tx = bx + (atX1 ? 1 : 0);
      return restTile(tx, by) && restTile(tx, by + 1);
    }
    if (atY0 || atY1) {
      int ty = by + (atY1 ? 1 : 0);
      return restTile(bx, ty) && restTile(bx + 1, ty);
    }
    // The midpoint of a diagonal lies on both diagonals of the square between four tile centres.
    if (Math.abs(fx - fy) < LATTICE_EPSILON && restTile(bx, by) && restTile(bx + 1, by + 1)) return true;
    return Math.abs(fx - (1 - fy)) < LATTICE_EPSILON && restTile(bx + 1, by) && restTile(bx, by + 1);
  }

  private boolean contains(double x, double y) {
    return Double.isFinite(x) && Double.isFinite(y)
        && x >= 0 && y >= 0 && x < width && y < height;
  }

  /** Local tile coordinates only. Outside/invalid positions never authorize combat. */
  boolean combatEndpointsSafe(double fromX, double fromY, double toX, double toY, boolean pvp) {
    if (!contains(fromX, fromY) || !contains(toX, toY)) return false;
    int from = terrain[(int) fromY][(int) fromX] & 255;
    int to = terrain[(int) toY][(int) toX] & 255;
    if (pvp ? CollisionRules.isPvpSafeHaven(from) || CollisionRules.isPvpSafeHaven(to)
        : CollisionRules.isSafeHaven(from) || CollisionRules.isSafeHaven(to)) return false;
    return true;
  }

  /** Local tile coordinates only. Outside/invalid positions never authorize combat. */
  public boolean allowsCombat(double fromX, double fromY, double toX, double toY, boolean pvp) {
    return combatEndpointsSafe(fromX, fromY, toX, toY, pvp)
        && hasLineOfSight(fromX, fromY, toX, toY);
  }

  /**
   * Documented flag: the line-of-sight rule spells must use so online casting matches single-player
   * (stage 1c wires it). Melee and creature attacks keep the stricter {@link #hasLineOfSight}.
   */
  public static final LineOfSightRules.Mode SPELL_LINE_OF_SIGHT = LineOfSightRules.Mode.SPELL_SAMPLED;

  /** Local tile coordinates. Runs the named rule; see {@link LineOfSightRules.Mode}. */
  public boolean hasLineOfSight(LineOfSightRules.Mode mode, double fromX, double fromY, double toX, double toY) {
    return switch (mode) {
      case SPELL_SAMPLED -> hasSpellLineOfSight(fromX, fromY, toX, toY);
      case SERVER_SUPERCOVER -> hasLineOfSight(fromX, fromY, toX, toY);
    };
  }

  /**
   * Single-player's spell line of sight over this map: the shared sampled pixel walk on world
   * pixels (global tile x 32, y 16), so the same two positions give the same answer as offline.
   * Endpoints are not tested, matching offline. Outside this map nothing is visible.
   */
  public boolean hasSpellLineOfSight(double fromX, double fromY, double toX, double toY) {
    if (!contains(fromX, fromY) || !contains(toX, toY)) return false;
    return LineOfSightRules.sampled(
        (float) ((fromX + originX) * GRID_W), (float) ((fromY + originY) * GRID_H),
        (float) ((toX + originX) * GRID_W), (float) ((toY + originY) * GRID_H), GRID_W, GRID_H,
        (px, py) -> blocksSight((int) (px / GRID_W) - originX, (int) (py / GRID_H) - originY));
  }

  /**
   * Server supercover ray: endpoints and corner-adjacent walls block attacks. Movement's wall-foot
   * exception never opens sight. This deliberately does not replace offline sampled targeting.
   */
  public boolean hasLineOfSight(double fromX, double fromY, double toX, double toY) {
    if (!contains(fromX, fromY) || !contains(toX, toY)) return false;
    if (blockedEndpoint(fromX, fromY) || blockedEndpoint(toX, toY)) return false;
    int x = (int) fromX, y = (int) fromY;
    double dx = toX - fromX, dy = toY - fromY;
    int stepX = Double.compare(dx, 0), stepY = Double.compare(dy, 0);
    double deltaX = dx == 0 ? Double.POSITIVE_INFINITY : Math.abs(1 / dx);
    double deltaY = dy == 0 ? Double.POSITIVE_INFINITY : Math.abs(1 / dy);
    double nextX = dx == 0 ? Double.POSITIVE_INFINITY
        : (stepX > 0 ? x + 1 - fromX : fromX - x) * deltaX;
    double nextY = dy == 0 ? Double.POSITIVE_INFINITY
        : (stepY > 0 ? y + 1 - fromY : fromY - y) * deltaY;
    for (int remaining = width + height + 2; remaining > 0; remaining--) {
      if (blocksSight(x, y)) return false;
      // A ray travelling exactly along a grid edge touches both columns/rows.
      if (dx == 0 && fromX == Math.floor(fromX) && blocksSight(x - 1, y)) return false;
      if (dy == 0 && fromY == Math.floor(fromY) && blocksSight(x, y - 1)) return false;
      if (Math.min(nextX, nextY) > 1) return true;
      if (Math.abs(nextX - nextY) < 1e-10) {
        if (blocksSight(x + stepX, y) || blocksSight(x, y + stepY)) return false;
        x += stepX; y += stepY; nextX += deltaX; nextY += deltaY;
      } else if (nextX < nextY) {
        x += stepX; nextX += deltaX;
      } else {
        y += stepY; nextY += deltaY;
      }
    }
    return false;
  }

  private boolean blocksSight(int x, int y) {
    return x < 0 || y < 0 || x >= width || y >= height
        || CollisionRules.blocksLineOfSight(terrain[y][x] & 255);
  }

  private boolean blockedEndpoint(double x, double y) {
    int tx = (int) x, ty = (int) y;
    return blocksSight(tx, ty)
        || (x == tx && blocksSight(tx - 1, ty))
        || (y == ty && blocksSight(tx, ty - 1))
        || (x == tx && y == ty && blocksSight(tx - 1, ty - 1));
  }

  public boolean[][] cells() {
    boolean[][] copy = new boolean[height][];
    for (int y = 0; y < height; y++) copy[y] = blocked[y].clone();
    return copy;
  }
}
