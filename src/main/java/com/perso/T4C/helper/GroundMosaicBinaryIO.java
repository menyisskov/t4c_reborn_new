package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Binary serializer for the legacy ground mosaic catalog, the tiling patterns declared by
 * {@code mosaic_x} / {@code mosaic_y} in the original .Map sprite mappings.
 *
 * <p>Each definition tiles a ground over a {@code width x height} block: X selects the outer
 * block and Y the frame inside it, matching the frame order of the original map format.
 * Frames come in two shapes — an explicit list of {@code width * height} names, or a single
 * coordinate template such as {@code "64kNormalGrass (&x, &y)"} where {@code &x} / {@code &y}
 * are substituted at lookup time.
 *
 * <p>Layout, wrapped in the shared {@code T4CBIN} deflate container:
 * <pre>
 * "T4CGMO"  6 bytes ASCII magic
 * version   int16 LE
 * count     int32 LE
 * count x { id string, width int32 LE, height int32 LE,
 *           frameCount int32 LE, frameCount x frame string }
 * </pre>
 */
public final class GroundMosaicBinaryIO {
    private static final byte[] MAGIC = "T4CGMO".getBytes(StandardCharsets.US_ASCII);
    private static final short VERSION = 1;
    private static final int MAX_STRING_BYTES = 4096;

    private GroundMosaicBinaryIO() {
    }

    public static List<Definition> read(File file) throws IOException, GameException {
        List<Definition> definitions = BinaryCatalogueIO.read(file, MAGIC, "ground mosaics binary",
                version -> {
                    if (version != VERSION) {
                        throw new GameException("Unsupported ground mosaics binary version: " + version);
                    }
                },
                (in, version) -> {
                    String id = BinaryIOUtils.readString(in, MAX_STRING_BYTES).trim();
                    int width = BinaryIOUtils.readIntLE(in);
                    int height = BinaryIOUtils.readIntLE(in);
                    int frameCount = BinaryIOUtils.readIntLE(in);
                    if (frameCount < 0) {
                        throw new GameException("Invalid frame count for ground mosaic " + id + ": " + frameCount);
                    }
                    List<String> frames = new ArrayList<>(frameCount);
                    for (int f = 0; f < frameCount; f++) {
                        frames.add(BinaryIOUtils.readString(in, MAX_STRING_BYTES).trim());
                    }
                    return new Definition(id, width, height, List.copyOf(frames));
                });
        return List.copyOf(definitions);
    }

    public static void write(File file, List<Definition> definitions) throws IOException {
        BinaryCatalogueIO.write(file, MAGIC, VERSION, definitions, (out, definition) -> {
            BinaryIOUtils.writeString(out, definition.id());
            BinaryIOUtils.writeIntLE(out, definition.width());
            BinaryIOUtils.writeIntLE(out, definition.height());
            List<String> frames = definition.frames();
            BinaryIOUtils.writeIntLE(out, frames.size());
            for (String frame : frames) {
                BinaryIOUtils.writeString(out, frame != null ? frame.trim() : "");
            }
        });
    }

    /**
     * One tiling pattern. {@code frames} holds either {@code width * height} explicit names or
     * a single {@code &x}/{@code &y} coordinate template.
     */
    public record Definition(String id, int width, int height, List<String> frames) {
    }
}
