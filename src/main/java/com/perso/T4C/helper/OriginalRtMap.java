package com.perso.T4C.helper;

import com.badlogic.gdx.graphics.Pixmap;
import com.perso.T4C.config.Paths;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/** Reads the RT map rebuilt from the active game maps and creates the original 640x448 view. */
public final class OriginalRtMap {
    public static final int VIEW_WIDTH = 640;
    public static final int VIEW_HEIGHT = 448;

    private static final int WORLD_COUNT = 8;
    private static final int TILE_COLUMNS = 3072;
    private static final int TILE_ROWS = 3072;
    private static final int LEGACY_MAP_WIDTH = TILE_COLUMNS * 2;
    private static final int LEGACY_MAP_HEIGHT = TILE_ROWS;
    private static final int HIGH_RES_MAP_WIDTH = TILE_COLUMNS * 4;
    private static final int HIGH_RES_MAP_HEIGHT = TILE_ROWS * 2;
    private static final int PALETTE_BYTES = 256 * 3;

    private static int cachedWorld = -1;
    private static byte[] cachedMap;
    private static byte[] cachedPalette;
    private static int cachedMapWidth;
    private static int cachedMapHeight;
    private static int cachedPixelsPerTileX;
    private static int cachedPixelsPerTileY;

    private OriginalRtMap() {
    }

    public static Pixmap createView(int world, int playerTileX, int playerTileY) throws IOException {
        loadWorld(Math.max(0, Math.min(WORLD_COUNT - 1, world)));
        Pixmap result = new Pixmap(VIEW_WIDTH, VIEW_HEIGHT, Pixmap.Format.RGBA8888);
        result.setColor(0f, 0f, 0f, 1f);
        result.fill();

        int displayScale = cachedPixelsPerTileX >= 4 ? 1 : 2;
        int sourceStartX = playerTileX * cachedPixelsPerTileX - VIEW_WIDTH / (displayScale * 2);
        int sourceStartY = playerTileY * cachedPixelsPerTileY - VIEW_HEIGHT / (displayScale * 2);
        int sourceWidth = VIEW_WIDTH / displayScale;
        int sourceHeight = VIEW_HEIGHT / displayScale;
        for (int sy = 0; sy < sourceHeight; sy++) {
            int mapY = sourceStartY + sy;
            if (mapY < 0 || mapY >= cachedMapHeight) continue;
            for (int sx = 0; sx < sourceWidth; sx++) {
                int mapX = sourceStartX + sx;
                if (mapX < 0 || mapX >= cachedMapWidth) continue;
                int paletteIndex = (cachedMap[mapY * cachedMapWidth + mapX] & 0xff) * 3;
                int rgba = ((cachedPalette[paletteIndex] & 0xff) << 24)
                        | ((cachedPalette[paletteIndex + 1] & 0xff) << 16)
                        | ((cachedPalette[paletteIndex + 2] & 0xff) << 8)
                        | 0xff;
                int dx = sx * displayScale;
                int dy = sy * displayScale;
                for (int yy = 0; yy < displayScale; yy++) {
                    for (int xx = 0; xx < displayScale; xx++) result.drawPixel(dx + xx, dy + yy, rgba);
                }
            }
        }
        return result;
    }

    private static synchronized void loadWorld(int world) throws IOException {
        if (cachedWorld == world && cachedMap != null && cachedPalette != null) return;
        try (RandomAccessFile file = new RandomAccessFile(Paths.RT_MAP, "r")) {
            int uncompressedSize = readIntLE(file);
            int[] compressedSizes = new int[WORLD_COUNT];
            int[] offsets = new int[WORLD_COUNT];
            for (int i = 0; i < WORLD_COUNT; i++) compressedSizes[i] = readIntLE(file);
            for (int i = 0; i < WORLD_COUNT; i++) offsets[i] = readIntLE(file);
            int highResolutionBytes = HIGH_RES_MAP_WIDTH * HIGH_RES_MAP_HEIGHT;
            int legacyBytes = LEGACY_MAP_WIDTH * LEGACY_MAP_HEIGHT;
            int mapBytes;
            if (uncompressedSize >= highResolutionBytes + PALETTE_BYTES) {
                cachedMapWidth = HIGH_RES_MAP_WIDTH;
                cachedMapHeight = HIGH_RES_MAP_HEIGHT;
                cachedPixelsPerTileX = 4;
                cachedPixelsPerTileY = 2;
                mapBytes = highResolutionBytes;
            } else if (uncompressedSize >= legacyBytes + PALETTE_BYTES) {
                cachedMapWidth = LEGACY_MAP_WIDTH;
                cachedMapHeight = LEGACY_MAP_HEIGHT;
                cachedPixelsPerTileX = 2;
                cachedPixelsPerTileY = 1;
                mapBytes = legacyBytes;
            } else {
                throw new IOException("Invalid original RT map header");
            }
            if (compressedSizes[world] <= 0) throw new IOException("Missing RT map world " + world);
            byte[] compressed = new byte[compressedSizes[world]];
            file.seek(Integer.toUnsignedLong(offsets[world]));
            file.readFully(compressed);
            byte[] inflated = inflate(compressed, uncompressedSize);
            cachedMap = Arrays.copyOf(inflated, mapBytes);
            cachedPalette = Arrays.copyOfRange(inflated, mapBytes, mapBytes + PALETTE_BYTES);
            cachedWorld = world;
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
            if (!inflater.finished() || written < PALETTE_BYTES) {
                throw new IOException("Incomplete original RT map data");
            }
            return output;
        } catch (DataFormatException e) {
            throw new IOException("Invalid original RT map compression", e);
        } finally {
            inflater.end();
        }
    }

    private static int readIntLE(RandomAccessFile file) throws IOException {
        return Integer.reverseBytes(file.readInt());
    }
}
