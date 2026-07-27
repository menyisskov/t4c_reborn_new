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
 * Class representing MusicZoneBinaryIO.
 */

public final class MusicZoneBinaryIO {
    private static final byte[] MAGIC = "T4CMUZ".getBytes(StandardCharsets.US_ASCII);
    private static final short VERSION = 1;

    private MusicZoneBinaryIO() {
    }

    public static List<Entry> read(File file) throws IOException, GameException {
        try (DataInputStream in = new DataInputStream(BinaryIOUtils.openInputStream(file, 1 << 20))) {
            byte[] magic = new byte[MAGIC.length];
            in.readFully(magic);
            if (!Arrays.equals(magic, MAGIC)) {
                throw new GameException("Invalid music zone binary file: wrong magic header");
            }
            short version = readShortLE(in);
            if (version != VERSION) {
                throw new GameException("Unsupported music zone binary version: " + version);
            }
            int count = readIntLE(in);
            if (count < 0) {
                throw new GameException("Invalid music zone count: " + count);
            }

            List<Entry> entries = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                Entry entry = new Entry();
                entry.name = readString(in);
                entry.x1 = readIntLE(in);
                entry.y1 = readIntLE(in);
                entry.x2 = readIntLE(in);
                entry.y2 = readIntLE(in);
                entry.music = readString(in);
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
        try (DataOutputStream out = new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1 << 20))) {
            out.write(MAGIC);
            writeShortLE(out, VERSION);
            writeIntLE(out, entries != null ? entries.size() : 0);
            if (entries == null) {
                return;
            }
            for (Entry entry : entries) {
                writeString(out, entry.name);
                writeIntLE(out, entry.x1);
                writeIntLE(out, entry.y1);
                writeIntLE(out, entry.x2);
                writeIntLE(out, entry.y2);
                writeString(out, entry.music);
            }
        }
    }

    private static String readString(DataInputStream in) throws IOException {
        return BinaryIOUtils.readString(in, 1_000_000);
    }

    private static void writeString(DataOutputStream out, String value) throws IOException {
        BinaryIOUtils.writeString(out, value);
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
        public String name;
        public int x1;
        public int y1;
        public int x2;
        public int y2;
        public String music;
    }
}
