package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.objects.ObjectPos;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class ObjectPositionBinaryIO {
    private static final byte[] MAGIC = "T4COBJ".getBytes(StandardCharsets.US_ASCII);
    private static final short VERSION = 2;
    private static final int MAX_NAME_BYTES = 4096;

    private ObjectPositionBinaryIO() {
    }

    public static List<ObjectPos> read(File file) throws IOException, GameException {
        return BinaryCatalogueIO.read(file, MAGIC, "object positions binary",
                version -> {
                    if (version != 1 && version != VERSION) {
                        throw new GameException("Unsupported object positions binary version: " + version);
                    }
                },
                (in, version) -> {
                    String name = readString(in);
                    int x = readIntLE(in);
                    int y = readIntLE(in);
                    int z = readIntLE(in);
                    boolean mirror = version >= 2 && in.readBoolean();
                    return new ObjectPos(name, x, y, z, mirror);
                });
    }

    public static void write(File file, List<ObjectPos> entries) throws IOException {
        BinaryCatalogueIO.write(file, MAGIC, VERSION, entries, (out, entry) -> {
            writeString(out, entry == null ? "" : entry.name());
            writeIntLE(out, entry == null ? 0 : (int) entry.x());
            writeIntLE(out, entry == null ? 0 : (int) entry.y());
            writeIntLE(out, entry == null ? 0 : (int) entry.z());
            out.writeBoolean(entry != null && entry.mirror());
        });
    }

    private static String readString(DataInputStream in) throws IOException, GameException {
        return BinaryIOUtils.readString(in, MAX_NAME_BYTES);
    }

    private static void writeString(DataOutputStream out, String value) throws IOException {
        BinaryIOUtils.writeString(out, value);
    }

    private static int readIntLE(DataInputStream in) throws IOException {
        return BinaryIOUtils.readIntLE(in);
    }

    private static void writeIntLE(DataOutputStream out, int value) throws IOException {
        BinaryIOUtils.writeIntLE(out, value);
    }
}
