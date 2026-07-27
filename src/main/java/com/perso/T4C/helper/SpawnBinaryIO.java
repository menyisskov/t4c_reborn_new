package com.perso.T4C.helper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.perso.T4C.exception.GameException;
/**
 * Class representing SpawnBinaryIO.
 */

public final class SpawnBinaryIO {
    private static final byte[] MAGIC = "T4CSPN".getBytes(StandardCharsets.US_ASCII);
    private static final short VERSION = 1;
    private static final int FLAG_STATIONARY = 1;
    private static final int FLAG_AGGRESSIVE = 1 << 1;

    private SpawnBinaryIO() {
    }

    public static List<Entry> read(File file) throws IOException, GameException {
        try (DataInputStream in = new DataInputStream(BinaryIOUtils.openInputStream(file, 1 << 16))) {
            byte[] magic = new byte[MAGIC.length];
            in.readFully(magic);
            if (!Arrays.equals(magic, MAGIC)) {
                throw new GameException("Invalid spawn binary file: wrong magic header");
            }
            short version = readShortLE(in);
            if (version != VERSION) {
                throw new GameException("Unsupported spawn binary version: " + version);
            }
            int count = readIntLE(in);
            if (count < 0) {
                throw new GameException("Invalid spawn count: " + count);
            }

            List<Entry> entries = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                Entry entry = new Entry();
                entry.type = BinaryIOUtils.readString(in, 4096);
                entry.x = readIntLE(in);
                entry.y = readIntLE(in);
                entry.z = readIntLE(in);
                int flags = in.readUnsignedByte();
                entry.stationary = (flags & FLAG_STATIONARY) != 0;
                entry.aggressive = (flags & FLAG_AGGRESSIVE) != 0;
                entries.add(entry);
            }
            return entries;
        }
    }

    public static void write(File file, List<Entry> entries) throws IOException {
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try (DataOutputStream out = new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1 << 16))) {
            out.write(MAGIC);
            writeShortLE(out, VERSION);
            writeIntLE(out, entries != null ? entries.size() : 0);
            if (entries == null) {
                return;
            }
            for (Entry entry : entries) {
                BinaryIOUtils.writeString(out, entry.type);
                writeIntLE(out, entry.x);
                writeIntLE(out, entry.y);
                writeIntLE(out, entry.z);
                int flags = 0;
                if (entry.stationary) {
                    flags |= FLAG_STATIONARY;
                }
                if (entry.aggressive) {
                    flags |= FLAG_AGGRESSIVE;
                }
                out.writeByte(flags);
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
/**
 * Class representing Entry.
 */

    public static final class Entry {
        public String type;
        public int x;
        public int y;
        public int z;
        public boolean stationary;
        public boolean aggressive;
    }
}
