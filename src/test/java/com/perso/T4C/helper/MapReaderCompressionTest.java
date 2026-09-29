package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MapReaderCompressionTest {
  private static final byte[] COMPRESSED_MAGIC = "T4CBIN".getBytes(StandardCharsets.US_ASCII);
  private static final byte[] RAW_MAGIC = "T4CMAP".getBytes(StandardCharsets.US_ASCII);
  private static final int WIDTH = 64;
  private static final int HEIGHT = 64;
  private static final String TILE_A = "Ground_Water (1, 1)";
  private static final String TILE_B = "Grass";

  private static void writeIntLE(DataOutputStream out, int value) throws Exception {
    out.writeByte(value & 0xFF);
    out.writeByte((value >>> 8) & 0xFF);
    out.writeByte((value >>> 16) & 0xFF);
    out.writeByte((value >>> 24) & 0xFF);
  }

  private static byte[] rawV6Payload() throws Exception {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(buffer);
    out.write(RAW_MAGIC);
    out.writeByte(6);
    out.writeByte(0);
    writeIntLE(out, WIDTH);
    writeIntLE(out, HEIGHT);
    writeIntLE(out, 2);
    for (String name : new String[] {TILE_A, TILE_B}) {
      byte[] bytes = name.getBytes(StandardCharsets.UTF_8);
      writeIntLE(out, bytes.length);
      out.write(bytes);
    }
    int total = WIDTH * HEIGHT;
    writeIntLE(out, total);
    for (int i = 0; i < total; i++) {
      writeIntLE(out, i);
      writeIntLE(out, 1);
      writeIntLE(out, i % 8 == 0 ? 1 : 2);
    }
    writeIntLE(out, 0);
    writeIntLE(out, 0);
    writeIntLE(out, 0);
    out.flush();
    return buffer.toByteArray();
  }

  private static File writeRawMap(Path dir, String name) throws Exception {
    File file = dir.resolve(name).toFile();
    Files.write(file.toPath(), rawV6Payload());
    return file;
  }

  private static File writeCompressedMap(Path dir, String name) throws Exception {
    File file = dir.resolve(name).toFile();
    try (OutputStream out = BinaryIOUtils.openOutputStream(file, 1 << 16)) {
      out.write(rawV6Payload());
    }
    return file;
  }

  private static byte[] head(File file, int n) throws Exception {
    try (InputStream in = Files.newInputStream(file.toPath())) {
      return in.readNBytes(n);
    }
  }

  private static void assertMapContent(File map) throws Exception {
    try (MapReader reader = new MapReader(map, true)) {
      assertEquals(WIDTH, reader.getWidth());
      assertEquals(HEIGHT, reader.getHeight());
      assertEquals(TILE_A, reader.getSpriteName(0, 0));
      assertEquals(TILE_B, reader.getSpriteName(1, 0));
      assertEquals(TILE_A, reader.getSpriteName(0, 1));
    }
  }

  @Test
  void expansionPreservesCoordinatesMetadataAndRoundTripsBothLayers(@TempDir Path dir)
      throws Exception {
    File source = writeRawMap(dir, "source.mapbin");
    File output = dir.resolve("expanded.mapbin").toFile();
    try (MapReader original = new MapReader(source)) {
      original.setDecorSpriteName(7, 3, "Test Wall");
      original.setScale(7, 3, 1.25f, 0.75f);
      original.setOffset(7, 3, -4, -96);
      original.setZOrder(7, 3, 2);
      try (MapReader expanded = original.expandedCopy(96, 80, TILE_A)) {
        for (int y = 0; y < HEIGHT; y++) {
          for (int x = 0; x < WIDTH; x++) {
            assertEquals(original.getGroundSpriteName(x, y), expanded.getGroundSpriteName(x, y));
            assertEquals(original.getDecorSpriteName(x, y), expanded.getDecorSpriteName(x, y));
            assertEquals(original.getOffsetY(x, y), expanded.getOffsetY(x, y));
          }
        }
        assertEquals(TILE_A, expanded.getGroundSpriteName(95, 79));
        assertEquals(1f, expanded.getScaleX(95, 79));
        expanded.writeCompact(output);
      }
      assertEquals(WIDTH, original.getWidth(), "expansion must not mutate its source");
      org.junit.jupiter.api.Assertions.assertThrows(
          IllegalArgumentException.class, () -> original.expandedCopy(WIDTH - 1, HEIGHT, TILE_A));
    }
    try (MapReader reloaded = new MapReader(output)) {
      assertEquals(96, reloaded.getWidth());
      assertEquals(80, reloaded.getHeight());
      assertEquals("Test Wall", reloaded.getDecorSpriteName(7, 3));
      assertEquals(1.25f, reloaded.getScaleX(7, 3));
      assertEquals(0.75f, reloaded.getScaleY(7, 3));
      assertEquals(-4f, reloaded.getOffsetX(7, 3));
      assertEquals(-96f, reloaded.getOffsetY(7, 3));
      assertEquals(2, reloaded.getZOrder(7, 3));
      assertEquals(TILE_A, reloaded.getGroundSpriteName(0, HEIGHT));
    }
  }

  @Test
  void compressedMapLoadsThroughTheReader(@TempDir Path dir) throws Exception {
    File map = writeCompressedMap(dir, "world.mapbin");
    assertArrayEquals(
        COMPRESSED_MAGIC, head(map, COMPRESSED_MAGIC.length), "fixture should be wrapped");
    assertMapContent(map);
  }

  @Test
  void legacyUncompressedMapStillLoads(@TempDir Path dir) throws Exception {
    File map = writeRawMap(dir, "legacy.mapbin");
    assertArrayEquals(RAW_MAGIC, head(map, RAW_MAGIC.length), "fixture should be raw");
    assertMapContent(map);
  }

  @Test
  void bothFormsYieldIdenticalContent(@TempDir Path dir) throws Exception {
    File raw = writeRawMap(dir, "raw.mapbin");
    File compressed = writeCompressedMap(dir, "compressed.mapbin");
    try (MapReader a = new MapReader(raw, true);
        MapReader b = new MapReader(compressed, true)) {
      for (int y = 0; y < HEIGHT; y++) {
        for (int x = 0; x < WIDTH; x++) {
          assertEquals(a.getSpriteName(x, y), b.getSpriteName(x, y), "tile " + x + "," + y);
        }
      }
    }
  }

  @Test
  void compressionShrinksARepetitiveMap(@TempDir Path dir) throws Exception {
    File raw = writeRawMap(dir, "raw.mapbin");
    File compressed = writeCompressedMap(dir, "compressed.mapbin");
    long rawSize = Files.size(raw.toPath());
    long compressedSize = Files.size(compressed.toPath());
    assertTrue(
        compressedSize < rawSize,
        "compressed " + compressedSize + " should be smaller than raw " + rawSize);
  }

  @Test
  void containerAndPayloadMagicsAreDistinguishable() {
    assertEquals(COMPRESSED_MAGIC.length, RAW_MAGIC.length);
    assertTrue(!Arrays.equals(COMPRESSED_MAGIC, RAW_MAGIC));
  }
}
