package com.perso.T4C.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the {@code .mapbin} compressed container: maps are written through the {@code T4CBIN}
 * Deflate wrapper to stay under the Git/LFS size budget, and both compressed and legacy raw files
 * must keep loading.
 */
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

    /**
     * Builds a raw v6 payload: two dictionary entries and alternating runs, i.e. the repetitive
     * shape real maps have. Empty override sections keep it minimal.
     */
    private static byte[] rawV6Payload() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(buffer);
        out.write(RAW_MAGIC);
        out.writeByte(6);
        out.writeByte(0);
        writeIntLE(out, WIDTH);
        writeIntLE(out, HEIGHT);

        writeIntLE(out, 2);
        for (String name : new String[] { TILE_A, TILE_B }) {
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

        writeIntLE(out, 0); // scale overrides
        writeIntLE(out, 0); // offset overrides
        writeIntLE(out, 0); // z-order overrides
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
    void compressedMapLoadsThroughTheReader(@TempDir Path dir) throws Exception {
        File map = writeCompressedMap(dir, "world.mapbin");

        assertArrayEquals(COMPRESSED_MAGIC, head(map, COMPRESSED_MAGIC.length), "fixture should be wrapped");
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

        try (MapReader a = new MapReader(raw, true); MapReader b = new MapReader(compressed, true)) {
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
        assertTrue(compressedSize < rawSize,
                "compressed " + compressedSize + " should be smaller than raw " + rawSize);
    }

    @Test
    void containerAndPayloadMagicsAreDistinguishable() {
        assertEquals(COMPRESSED_MAGIC.length, RAW_MAGIC.length);
        assertTrue(!Arrays.equals(COMPRESSED_MAGIC, RAW_MAGIC));
    }
}
