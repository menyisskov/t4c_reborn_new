package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
/**
 * Class representing DecorLayerRuleBinaryIO.
 */

public final class DecorLayerRuleBinaryIO {
    private static final byte[] MAGIC = "T4CDLR".getBytes(StandardCharsets.US_ASCII);
    private static final short VERSION = 1;

    private DecorLayerRuleBinaryIO() {
    }

    public static Set<String> read(File file) throws IOException, GameException {
        try (DataInputStream in = new DataInputStream(BinaryIOUtils.openInputStream(file, 1 << 16))) {
            byte[] magic = new byte[MAGIC.length];
            in.readFully(magic);
            if (!Arrays.equals(magic, MAGIC)) {
                throw new GameException("Invalid decor layer rules binary file: wrong magic header");
            }
            short version = readShortLE(in);
            if (version != VERSION) {
                throw new GameException("Unsupported decor layer rules binary version: " + version);
            }
            int count = readIntLE(in);
            if (count < 0) {
                throw new GameException("Invalid decor layer rule count: " + count);
            }
            Set<String> names = new LinkedHashSet<>(count);
            for (int i = 0; i < count; i++) {
                String name = BinaryIOUtils.readString(in, 4096).trim();
                if (!name.isEmpty()) {
                    names.add(name);
                }
            }
            return names;
        }
    }

    public static void write(File file, Set<String> names) throws IOException {
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try (DataOutputStream out = new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1 << 16))) {
            out.write(MAGIC);
            writeShortLE(out, VERSION);
            writeIntLE(out, names != null ? names.size() : 0);
            if (names == null) {
                return;
            }
            for (String name : names) {
                BinaryIOUtils.writeString(out, name != null ? name.trim() : "");
            }
        }
    }

    private static short readShortLE(DataInputStream in) throws IOException {
        return BinaryIOUtils.readShortLE(in);
    }

    private static int readIntLE(DataInputStream in) throws IOException {
        return BinaryIOUtils.readIntLE(in);
    }

    private static void writeShortLE(DataOutputStream out, short value) throws IOException {
        BinaryIOUtils.writeShortLE(out, value);
    }

    private static void writeIntLE(DataOutputStream out, int value) throws IOException {
        BinaryIOUtils.writeIntLE(out, value);
    }
}
