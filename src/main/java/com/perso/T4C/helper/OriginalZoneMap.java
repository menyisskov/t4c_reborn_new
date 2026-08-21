package com.perso.T4C.helper;

import com.perso.T4C.config.Paths;
import com.perso.T4C.i18n.I18n;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import lombok.extern.slf4j.Slf4j;

/** Native Zone_Map.dat lookup used by MapZoneDisplay. */
@Slf4j
public final class OriginalZoneMap {
  public static final int EMPTY_ZONE = 255;
  private static final int WORLD_COUNT = 8;
  private static final int TILE_COLUMNS = 3072;
  private static final int TILE_ROWS = 3072;

  private static int cachedWorld = -1;
  private static byte[] cachedGrid;

  private OriginalZoneMap() {}

  public static int zoneId(int world, int tileX, int tileY) {
    byte[] grid = grid(world);
    if (grid == null || tileX < 0 || tileY < 0 || tileX >= TILE_COLUMNS || tileY >= TILE_ROWS) {
      return EMPTY_ZONE;
    }
    return grid[tileY * TILE_COLUMNS + tileX] & 0xff;
  }

  public static String displayName(int world, int tileX, int tileY) {
    int id = zoneId(world, tileX, tileY);
    if (id == EMPTY_ZONE) return null;
    String key = "zone." + world + "." + id;
    String name = I18n.key(key);
    if (name == null || name.isBlank() || name.equals(key)) return null;
    return name;
  }

  private static synchronized byte[] grid(int world) {
    int clamped = Math.max(0, Math.min(WORLD_COUNT - 1, world));
    if (cachedWorld == clamped && cachedGrid != null) return cachedGrid;
    try (RandomAccessFile file = new RandomAccessFile(Paths.ZONE_MAP, "r")) {
      int uncompressedSize = readIntLE(file);
      int[] compressedSizes = new int[WORLD_COUNT];
      int[] offsets = new int[WORLD_COUNT];
      for (int i = 0; i < WORLD_COUNT; i++) compressedSizes[i] = readIntLE(file);
      for (int i = 0; i < WORLD_COUNT; i++) offsets[i] = readIntLE(file);
      if (compressedSizes[clamped] <= 0) {
        cachedWorld = clamped;
        cachedGrid = new byte[0];
        return cachedGrid;
      }
      byte[] compressed = new byte[compressedSizes[clamped]];
      file.seek(Integer.toUnsignedLong(offsets[clamped]));
      file.readFully(compressed);
      byte[] inflated = inflate(compressed, uncompressedSize);
      int gridBytes = TILE_COLUMNS * TILE_ROWS;
      cachedGrid = new byte[gridBytes];
      System.arraycopy(inflated, 0, cachedGrid, 0, Math.min(gridBytes, inflated.length));
      cachedWorld = clamped;
      return cachedGrid;
    } catch (IOException e) {
      log.warn("Failed to load native zone map", e);
      cachedWorld = clamped;
      cachedGrid = new byte[0];
      return cachedGrid;
    }
  }

  private static byte[] inflate(byte[] compressed, int expectedSize) throws IOException {
    byte[] output = new byte[expectedSize];
    Inflater inflater = new Inflater();
    inflater.setInput(compressed);
    try {
      int written = 0;
      while (!inflater.finished() && written < output.length) {
        int count = inflater.inflate(output, written, output.length - written);
        if (count == 0 && (inflater.needsInput() || inflater.needsDictionary())) break;
        written += count;
      }
      return output;
    } catch (DataFormatException e) {
      throw new IOException("Invalid zone map compression", e);
    } finally {
      inflater.end();
    }
  }

  private static int readIntLE(RandomAccessFile file) throws IOException {
    return Integer.reverseBytes(file.readInt());
  }
}
