package com.perso.T4C.tools;

import com.perso.T4C.config.MapDefinition;
import com.perso.T4C.helper.BinaryIOUtils;
import com.perso.T4C.helper.CollisionMapIO;
import lombok.Getter;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Imports the original four-bit collision grids from an encrypted T4C
 * Worlds.WDA file into editable t4c_reborn .colbin files.
 */
public final class WdaCollisionImporter {
    private static final long WDA_MARKER = 68775L;
    private static final int WORLDS_WDA_TYPE = 1;
    private static final int MAX_ENTRIES = 100_000;
    private static final int MAX_STRING_BYTES = 1 << 20;

    private WdaCollisionImporter() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: WdaCollisionImporter <T4C Worlds.WDA>");
        }
        List<ImportedWorld> worlds = importFile(new File(args[0]), true);
        for (ImportedWorld world : worlds) {
            System.out.printf("Imported WDA world %d (%s): %dx%d -> %s%n",
                    world.id, world.name, world.width, world.height,
                    world.output == null ? "(not mapped)" : world.output.getPath());
        }
    }

    public static List<ImportedWorld> importFile(File source, boolean writeMappedWorlds) throws IOException {
        List<ImportedWorld> worlds = read(source);
        if (writeMappedWorlds) {
            for (ImportedWorld world : worlds) {
                MapDefinition definition = definitionForWorld(world.id);
                if (definition == null) {
                    continue;
                }
                world.output = new File(definition.getCollisionPath());
                CollisionMapIO.write(world.output, world.width, world.height, world.data);
            }
        }
        return worlds;
    }

    public static List<ImportedWorld> read(File source) throws IOException {
        if (source == null || !source.isFile()) {
            throw new IOException("WDA file not found: " + source);
        }
        try (DataInputStream in = new DataInputStream(new WdaDecryptInputStream(
                new BufferedInputStream(new FileInputStream(source), 1 << 20)))) {
            long marker = readUnsignedIntLE(in);
            int type = in.readUnsignedByte();
            if (marker != WDA_MARKER || type != WORLDS_WDA_TYPE) {
                throw new IOException("Invalid T4C Worlds.WDA header: marker=" + marker + ", type=" + type);
            }
            skipSpells(in);
            int worldCount = checkedCount(readUnsignedIntLE(in), "world");
            List<ImportedWorld> worlds = new ArrayList<>(worldCount);
            for (int i = 0; i < worldCount; i++) {
                int id = readUnsignedShortLE(in);
                String name = readString(in);
                int width = readUnsignedShortLE(in);
                int height = readUnsignedShortLE(in);
                if (width <= 0 || height <= 0 || (width & 1) != 0) {
                    throw new IOException("Invalid dimensions for WDA world " + id + ": " + width + "x" + height);
                }
                int tileCount;
                try {
                    tileCount = Math.multiplyExact(width, height);
                } catch (ArithmeticException e) {
                    throw new IOException("WDA world " + id + " is too large", e);
                }
                byte[] packed = in.readNBytes(tileCount / 2);
                if (packed.length != tileCount / 2) {
                    throw new EOFException("Incomplete collision grid for WDA world " + id);
                }
                worlds.add(new ImportedWorld(id, name, width, height,
                        unpackCollisionGrid(packed, width, height), null));
            }
            return worlds;
        }
    }

    private static void skipSpells(DataInputStream in) throws IOException {
        int spellCount = checkedCount(readUnsignedIntLE(in), "spell");
        for (int i = 0; i < spellCount; i++) {
            skipBytes(in, 4); // spell ID
            for (int string = 0; string < 6; string++) {
                skipString(in); // name, exhausts, duration, frequency
            }
            skipBytes(in, 4); // element
            skipString(in); // mana cost
            skipBytes(in, 8L * 4L); // area, visuals, target, attack and requirements
            skipBytes(in, 3); // line of sight, PVP check, attack spell
            skipBytes(in, 4); // icon
            skipString(in); // success percentage
            skipString(in); // description

            int effectCount = checkedCount(readUnsignedIntLE(in), "spell effect");
            for (int effect = 0; effect < effectCount; effect++) {
                skipBytes(in, 4); // effect structure ID
                int parameterCount = checkedCount(readUnsignedIntLE(in), "spell parameter");
                for (int parameter = 0; parameter < parameterCount; parameter++) {
                    skipBytes(in, 4); // parameter ID
                    skipString(in);
                }
            }
            int requirementCount = checkedCount(readUnsignedIntLE(in), "spell requirement");
            skipBytes(in, requirementCount * 4L);
        }
    }

    private static byte[] unpackCollisionGrid(byte[] packed, int width, int height) {
        byte[] unpacked = new byte[width * height];
        for (int y = 0; y < height; y++) {
            int packedRow = y * (width / 2);
            int outputRow = y * width;
            for (int x = 0; x < width; x += 2) {
                int value = packed[packedRow + x / 2] & 0xFF;
                unpacked[outputRow + x] = (byte) ((value >>> 4) & 0x0F);
                unpacked[outputRow + x + 1] = (byte) (value & 0x0F);
            }
        }
        return unpacked;
    }

    private static int checkedCount(long value, String label) throws IOException {
        if (value < 0 || value > MAX_ENTRIES) {
            throw new IOException("Invalid " + label + " count: " + value);
        }
        return (int) value;
    }

    private static String readString(DataInputStream in) throws IOException {
        int length = checkedStringLength(readUnsignedIntLE(in));
        byte[] value = in.readNBytes(length);
        if (value.length != length) {
            throw new EOFException("Incomplete WDA string");
        }
        return new String(value, StandardCharsets.ISO_8859_1);
    }

    private static void skipString(DataInputStream in) throws IOException {
        skipBytes(in, checkedStringLength(readUnsignedIntLE(in)));
    }

    private static int checkedStringLength(long value) throws IOException {
        if (value < 0 || value > MAX_STRING_BYTES) {
            throw new IOException("Invalid WDA string length: " + value);
        }
        return (int) value;
    }

    private static void skipBytes(DataInputStream in, long count) throws IOException {
        if (count < 0) {
            throw new IOException("Invalid WDA skip length: " + count);
        }
        in.skipNBytes(count);
    }

    private static int readUnsignedShortLE(DataInputStream in) throws IOException {
        return BinaryIOUtils.readShortLE(in) & 0xFFFF;
    }

    private static long readUnsignedIntLE(DataInputStream in) throws IOException {
        return BinaryIOUtils.readIntLE(in) & 0xFFFFFFFFL;
    }

    private static MapDefinition definitionForWorld(int id) {
        for (MapDefinition definition : MapDefinition.values()) {
            if (definition.getZ() == id) {
                return definition;
            }
        }
        return null;
    }

    @Getter
    public static final class ImportedWorld {
        private final int id;
        private final String name;
        private final int width;
        private final int height;
        private final byte[] data;
        private File output;

        private ImportedWorld(int id, String name, int width, int height, byte[] data, File output) {
            this.id = id;
            this.name = name;
            this.width = width;
            this.height = height;
            this.data = data;
            this.output = output;
        }
    }

    /**
     * Vircom stream XOR used by WDAFile: a deterministic 3418-byte key repeated
     * at absolute file offsets.
     */
    private static final class WdaDecryptInputStream extends FilterInputStream {
        private static final byte[] KEY = createKey();
        private long offset;

        private WdaDecryptInputStream(InputStream in) {
            super(in);
        }

        @Override
        public int read() throws IOException {
            int encrypted = super.read();
            if (encrypted < 0) {
                return -1;
            }
            int decrypted = encrypted ^ (KEY[(int) (offset % KEY.length)] & 0xFF);
            offset++;
            return decrypted;
        }

        @Override
        public int read(byte[] target, int off, int len) throws IOException {
            int read = super.read(target, off, len);
            if (read <= 0) {
                return read;
            }
            for (int i = 0; i < read; i++) {
                target[off + i] ^= KEY[(int) ((offset + i) % KEY.length)];
            }
            offset += read;
            return read;
        }

        @Override
        public long skip(long count) throws IOException {
            if (count <= 0) {
                return 0;
            }
            byte[] discard = new byte[(int) Math.min(8192L, count)];
            long skipped = 0;
            while (skipped < count) {
                int read = read(discard, 0, (int) Math.min(discard.length, count - skipped));
                if (read < 0) {
                    break;
                }
                skipped += read;
            }
            return skipped;
        }

        private static byte[] createKey() {
            byte[] key = new byte[3418];
            long seed = 23422L;
            int lastValue = 0;
            for (int i = 0; i < key.length; i++) {
                seed = seed * 725472321L + 1L;
                int value = (int) ((seed >>> 16) & 0xFF);
                if (value == lastValue) {
                    seed = seed * 725472321L + 1L;
                    value = (int) ((seed >>> 16) & 0xFF);
                }
                lastValue = value;
                key[i] = (byte) value;
            }
            return key;
        }
    }
}
