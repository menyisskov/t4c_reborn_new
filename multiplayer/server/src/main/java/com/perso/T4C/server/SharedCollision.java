package com.perso.T4C.server;

import static com.perso.T4C.config.GameConstants.PLAYER_COLLISION_FOOTPRINT_ABOVE_TILES;

import com.perso.T4C.helper.BinaryIOUtils;
import com.perso.T4C.online.model.PortalContent;
import com.perso.T4C.world.CollisionRules;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * One world's collision, loaded once and shared read-only by every {@link PreviewMap} window over
 * it (WORLD_STREAMING_DESIGN.md 3.2). One byte per tile: the low four bits index the terrain code
 * (a world has at most 16 distinct codes; the shipped ones have at most 9), and three flag bits
 * hold the answers {@link RegionMapLoader} computes per tile: the static foot rule, the foot rule
 * folded with the tiles above (the stricter {@code canStand} rule, with authored portal sources
 * cleared as offline permits), and "authored portal source players may enter". Coordinates are
 * absolute world tiles. Immutable after construction, so any thread may read it.
 */
final class SharedCollision {
  private static final int INDEX_MASK = 0x0F;
  private static final int FOOT = 1 << 4;
  private static final int BLOCKED = 1 << 5;
  private static final int PORTAL = 1 << 6;
  static final int MAX_TERRAIN_CODES = 16;

  private final int world;
  private final int width;
  private final int height;
  private final byte[] cells;
  private final int[] palette;

  private SharedCollision(int world, int width, int height, byte[] cells, int[] palette) {
    this.world = world;
    this.width = width;
    this.height = height;
    this.cells = cells;
    this.palette = palette;
  }

  int world() { return world; }
  int width() { return width; }
  int height() { return height; }

  /** Bytes held by the grid itself (one per tile), not counting the object header or palette. */
  long gridBytes() { return cells.length; }

  /** The raw terrain code of an absolute tile (0-255); the tile must be inside the world. */
  int terrain(int x, int y) { return palette[cells[y * width + x] & INDEX_MASK]; }

  /** Static movement blocking of the tile itself (no above-foot fold). */
  boolean foot(int x, int y) { return (cells[y * width + x] & FOOT) != 0; }

  /** Movement blocking including the tiles above the foot, minus enabled portal sources. */
  boolean blocked(int x, int y) { return (cells[y * width + x] & BLOCKED) != 0; }

  /** True for an enabled authored portal source tile, which offline lets players enter. */
  boolean portal(int x, int y) { return (cells[y * width + x] & PORTAL) != 0; }

  /** Reads the T4CBIN envelope from {@code path}; portals of other worlds are ignored. */
  static SharedCollision load(Path path, int world, List<PortalContent.Portal> enabledPortals) throws IOException {
    try (var in = new DataInputStream(BinaryIOUtils.openInputStream(path.toFile(), 1 << 20))) {
      int width = BinaryIOUtils.readIntLE(in), height = BinaryIOUtils.readIntLE(in);
      long size = (long) width * height;
      if (width <= 0 || height <= 0 || width > 32768 || height > 32768 || size > 64L * 1024 * 1024)
        throw new IOException("Collision map exceeds supported dimensions");
      var data = in.readNBytes((int) size);
      if (data.length != size) throw new IOException("Truncated collision map");
      return build(world, width, height, data, enabledPortals);
    }
  }

  /** Builds from raw terrain codes (row-major). The array is not retained. */
  static SharedCollision build(int world, int width, int height, byte[] data, List<PortalContent.Portal> enabledPortals)
      throws IOException {
    if (data.length != (long) width * height) throw new IOException("Truncated collision map");
    var index = new int[256];
    java.util.Arrays.fill(index, -1);
    var palette = new int[MAX_TERRAIN_CODES];
    int codes = 0;
    var cells = new byte[data.length];
    var blocks = new boolean[256];
    for (int code = 0; code < 256; code++) blocks[code] = CollisionRules.blocksMovement(code);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int at = y * width + x, code = data[at] & 255;
        if (index[code] < 0) {
          if (codes == MAX_TERRAIN_CODES) throw new IOException("Collision map has more than 16 terrain codes");
          palette[codes] = code;
          index[code] = codes++;
        }
        int bits = index[code];
        if (blocks[code]) bits |= FOOT;
        boolean blocked = false;
        for (int above = 0; above <= PLAYER_COLLISION_FOOTPRINT_ABOVE_TILES && !blocked; above++)
          blocked = y - above < 0 || blocks[data[(y - above) * width + x] & 255];
        if (blocked) bits |= BLOCKED;
        cells[at] = (byte) bits;
      }
    }
    // Offline permits players on authored portal sources, while sight keeps the original terrain.
    for (var portal : enabledPortals) {
      if (portal.sourceWorld() != world || portal.sourceX() < 0 || portal.sourceY() < 0
          || portal.sourceX() >= width || portal.sourceY() >= height) continue;
      int at = portal.sourceY() * width + portal.sourceX();
      cells[at] = (byte) ((cells[at] & ~BLOCKED) | PORTAL);
    }
    return new SharedCollision(world, width, height, cells, palette);
  }
}
