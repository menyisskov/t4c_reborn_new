package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.MonsterClan;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Binary serializer for directed monster clan hostility relations.
 */
public final class ClanRelationsBinaryIO {
    private static final byte[] MAGIC = "T4CCLN".getBytes(StandardCharsets.US_ASCII);
    private static final short VERSION = 1;

    private ClanRelationsBinaryIO() {
    }

    public static List<Entry> read(File file) throws IOException, GameException {
        try (DataInputStream in = new DataInputStream(BinaryIOUtils.openInputStream(file, 1 << 16))) {
            byte[] magic = new byte[MAGIC.length];
            in.readFully(magic);
            if (!Arrays.equals(magic, MAGIC)) {
                throw new GameException("Invalid clan relations file: wrong magic header");
            }
            short version = readShortLE(in);
            if (version != VERSION) {
                throw new GameException("Unsupported clan relations version: " + version);
            }
            int count = readIntLE(in);
            if (count < 0) {
                throw new GameException("Invalid clan relation count: " + count);
            }

            List<Entry> entries = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                Entry entry = new Entry();
                entry.source = readClan(in);
                entry.target = readClan(in);
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
                writeString(out, entry != null && entry.source != null ? entry.source.name() : MonsterClan.NEUTRAL.name());
                writeString(out, entry != null && entry.target != null ? entry.target.name() : MonsterClan.NEUTRAL.name());
            }
        }
    }

    private static MonsterClan readClan(DataInputStream in) throws IOException {
        String value = readString(in);
        try {
            return MonsterClan.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IOException("Unknown monster clan: " + value, e);
        }
    }

    private static String readString(DataInputStream in) throws IOException {
        return BinaryIOUtils.readString(in, 4096);
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

    public static final class Entry {
        public MonsterClan source;
        public MonsterClan target;

        public Entry() {
        }

        public Entry(MonsterClan source, MonsterClan target) {
            this.source = source;
            this.target = target;
        }
    }
}
