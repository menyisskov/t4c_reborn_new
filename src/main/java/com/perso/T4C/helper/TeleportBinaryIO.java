package com.perso.T4C.helper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
        return BinaryCatalogueIO.read(file, MAGIC, "teleport binary",
                version -> {
                    if (version != VERSION) {
                        throw new GameException("Unsupported teleport binary version: " + version);
                    }
                },
                (in, version) -> {
                    Entry entry = new Entry();
                    entry.id = readIntLE(in);
                    entry.sourceZ = readIntLE(in);
                    entry.sourceX = readIntLE(in);
                    entry.sourceY = readIntLE(in);
                    entry.targetZ = readIntLE(in);
                    entry.targetX = readIntLE(in);
                    entry.targetY = readIntLE(in);
                    return entry;
                });
    }

    public static void write(File file, List<Entry> entries) throws IOException {
        BinaryCatalogueIO.write(file, MAGIC, VERSION, entries, (out, entry) -> {
            writeIntLE(out, entry.id);
            writeIntLE(out, entry.sourceZ);
            writeIntLE(out, entry.sourceX);
            writeIntLE(out, entry.sourceY);
            writeIntLE(out, entry.targetZ);
            writeIntLE(out, entry.targetX);
            writeIntLE(out, entry.targetY);
        });
    }

    private static int readIntLE(DataInputStream in) throws IOException {
        return BinaryIOUtils.readIntLE(in);
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
