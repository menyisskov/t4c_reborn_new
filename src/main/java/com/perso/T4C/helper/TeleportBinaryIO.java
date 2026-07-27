package com.perso.T4C.helper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.perso.T4C.exception.GameException;
/**
 * Class representing TeleportBinaryIO.
 */

public final class TeleportBinaryIO {
    private static final byte[] MAGIC = "T4CTLP".getBytes(StandardCharsets.US_ASCII);
    private static final short VERSION = 1;

    private TeleportBinaryIO() {
    }

    public static List<Entry> read(File file) throws IOException, GameException {
        try (DataInputStream in = new DataInputStream(BinaryIOUtils.openInputStream(file, 1 << 16))) {
            byte[] magic = new byte[MAGIC.length];
            in.readFully(magic);
            if (!Arrays.equals(magic, MAGIC)) {
                throw new GameException("Invalid teleport binary file: wrong magic header");
            }
            short version = readShortLE(in);
            if (version != VERSION) {
                throw new GameException("Unsupported teleport binary version: " + version);
            }
            int count = readIntLE(in);
            if (count < 0) {
                throw new GameException("Invalid teleport count: " + count);
            }

            List<Entry> entries = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                Entry entry = new Entry();
                entry.id = readIntLE(in);
                entry.sourceZ = readIntLE(in);
                entry.sourceX = readIntLE(in);
                entry.sourceY = readIntLE(in);
                entry.targetZ = readIntLE(in);
                entry.targetX = readIntLE(in);
                entry.targetY = readIntLE(in);
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
                writeIntLE(out, entry.id);
                writeIntLE(out, entry.sourceZ);
                writeIntLE(out, entry.sourceX);
                writeIntLE(out, entry.sourceY);
                writeIntLE(out, entry.targetZ);
                writeIntLE(out, entry.targetX);
                writeIntLE(out, entry.targetY);
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
        public int id;
        public int sourceZ;
        public int sourceX;
        public int sourceY;
        public int targetZ;
        public int targetX;
        public int targetY;
    }
}
