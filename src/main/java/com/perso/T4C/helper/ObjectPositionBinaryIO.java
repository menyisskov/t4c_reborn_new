package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.objects.ObjectPos;

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

public final class ObjectPositionBinaryIO {
    private static final byte[] MAGIC = "T4COBJ".getBytes(StandardCharsets.US_ASCII);
    private static final short VERSION = 2;
    private static final int MAX_NAME_BYTES = 4096;

    private ObjectPositionBinaryIO() {
    }

    public static List<ObjectPos> read(File file) throws IOException, GameException {
        try (DataInputStream in = new DataInputStream(BinaryIOUtils.openInputStream(file, 1 << 16))) {
            byte[] magic = new byte[MAGIC.length];
            in.readFully(magic);
            if (!Arrays.equals(magic, MAGIC)) {
                throw new GameException("Invalid object positions binary file: wrong magic header");
            }
            short version = readShortLE(in);
            if (version != 1 && version != VERSION) {
                throw new GameException("Unsupported object positions binary version: " + version);
            }
            int count = readIntLE(in);
            if (count < 0) {
                throw new GameException("Invalid object position count: " + count);
            }

            List<ObjectPos> entries = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                String name = readString(in);
                int x = readIntLE(in);
                int y = readIntLE(in);
                int z = readIntLE(in);
                boolean mirror = version >= 2 && in.readBoolean();
                entries.add(new ObjectPos(name, x, y, z, mirror));
            }
            return entries;
        }
    }

    public static void write(File file, List<ObjectPos> entries) throws IOException {
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try (DataOutputStream out = new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1 << 16))) {
            out.write(MAGIC);
            writeShortLE(out, VERSION);
            writeIntLE(out, entries == null ? 0 : entries.size());
            if (entries == null) {
                return;
            }
            for (ObjectPos entry : entries) {
                writeString(out, entry == null ? "" : entry.name());
                writeIntLE(out, entry == null ? 0 : (int) entry.x());
                writeIntLE(out, entry == null ? 0 : (int) entry.y());
                writeIntLE(out, entry == null ? 0 : (int) entry.z());
                out.writeBoolean(entry != null && entry.mirror());
            }
        }
    }

    private static String readString(DataInputStream in) throws IOException, GameException {
        return BinaryIOUtils.readString(in, MAX_NAME_BYTES);
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
}
