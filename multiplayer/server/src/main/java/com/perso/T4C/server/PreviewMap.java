package com.perso.T4C.server;

import static com.perso.T4C.config.GameConstants.*;

import com.perso.T4C.helper.BinaryIOUtils;
import com.perso.T4C.world.CollisionRules;
import com.perso.T4C.world.WorldCollision;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Path;

/** A bounded starting-area view of the real collision file; never loads desktop saves or assets. */
public final class PreviewMap {
  private final boolean[][] blocked;
  public final int originX;
  public final int originY;
  public final int width;
  public final int height;
  public final double spawnX;
  public final double spawnY;

  PreviewMap(boolean[][] blocked, int originX, int originY, int spawnX, int spawnY) {
    height = blocked.length;
    if (height == 0 || blocked[0].length == 0) throw new IllegalArgumentException("Empty map");
    width = blocked[0].length;
    this.blocked = new boolean[height][];
    for (int y = 0; y < height; y++) {
      if (blocked[y].length != width) throw new IllegalArgumentException("Nonrectangular map");
      this.blocked[y] = blocked[y].clone();
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
      for (int y = 0; y < cells.length; y++)
        for (int x = 0; x < cells[y].length; x++) {
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
              return new PreviewMap(cells, ox, oy, x, y);
      throw new IOException("No safe starting position in preview area");
    }
  }

  public boolean canStand(double x, double y) {
    return Double.isFinite(x) && Double.isFinite(y)
        && x >= 0 && y >= 0 && x < width && y < height && !blocked[(int) y][(int) x];
  }

  public boolean[][] cells() {
    boolean[][] copy = new boolean[height][];
    for (int y = 0; y < height; y++) copy[y] = blocked[y].clone();
    return copy;
  }
}
