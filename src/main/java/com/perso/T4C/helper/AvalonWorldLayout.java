package com.perso.T4C.helper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.BitSet;

/** Shared coordinate change and exact ownership of the land moved out of the old world. */
public final class AvalonWorldLayout {
  public static final int SHIFT_X = 2700;
  public static final int WORLD_LAYOUT_VERSION = 1;
  private static final int LEGACY_WIDTH = 3072;
  private static final int LEGACY_HEIGHT = 3072;
  private static final Path MASK_PATH = Path.of("assets/maps/worldmap/avalon-legacy-tiles.bin");
  private static volatile BitSet legacyTiles;

  private AvalonWorldLayout() {}

  public static boolean isLegacyAvalonTile(int x, int y) {
    if (x < 0 || y < 0 || x >= LEGACY_WIDTH || y >= LEGACY_HEIGHT) return false;
    return legacyTiles().get(y * LEGACY_WIDTH + x);
  }

  private static BitSet legacyTiles() {
    BitSet tiles = legacyTiles;
    if (tiles == null) {
      synchronized (AvalonWorldLayout.class) {
        tiles = legacyTiles;
        if (tiles == null) {
          try {
            tiles = decodeLegacyMask(Files.readAllBytes(MASK_PATH));
          } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException(
                "Cannot migrate Avalon saves: invalid or missing " + MASK_PATH, e);
          }
          legacyTiles = tiles;
        }
      }
    }
    return tiles;
  }

  static BitSet decodeLegacyMask(byte[] bytes) {
    byte[] magic = "T4CAVAL1".getBytes(StandardCharsets.US_ASCII);
    if (bytes.length < 16 || !Arrays.equals(magic, Arrays.copyOf(bytes, 8))) {
      throw new IllegalArgumentException("Invalid Avalon ownership mask header");
    }
    ByteBuffer header = ByteBuffer.wrap(bytes);
    if (header.getInt(8) != LEGACY_WIDTH
        || header.getInt(12) != LEGACY_HEIGHT
        || bytes.length > 16 + (LEGACY_WIDTH * LEGACY_HEIGHT + 7) / 8) {
      throw new IllegalArgumentException("Invalid Avalon ownership mask dimensions or length");
    }
    BitSet tiles = BitSet.valueOf(Arrays.copyOfRange(bytes, 16, bytes.length));
    if (tiles.isEmpty())
      throw new IllegalArgumentException("Avalon ownership mask must not be empty");
    return tiles;
  }
}
