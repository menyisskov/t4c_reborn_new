package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Binary serialization for the player XP progression table. */
public final class XpCurveBinaryIO {
    private static final byte[] MAGIC = "T4CXP".getBytes(StandardCharsets.US_ASCII);
    private static final short VERSION = 1;
    private static final int MAX_ENTRIES = 100_000;

    private XpCurveBinaryIO() {
    }

    public static List<XpCurve.Entry> read(File file) throws IOException, GameException {
        try (DataInputStream in = new DataInputStream(BinaryIOUtils.openInputStream(file, 1 << 16))) {
            byte[] magic = new byte[MAGIC.length];
            in.readFully(magic);
            if (!Arrays.equals(magic, MAGIC)) {
                throw new GameException("Invalid XP curve file: wrong magic header");
            }
            short version = BinaryIOUtils.readShortLE(in);
            if (version != VERSION) {
                throw new GameException("Unsupported XP curve version: " + version);
            }
            int count = BinaryIOUtils.readIntLE(in);
            if (count < 0 || count > MAX_ENTRIES) {
                throw new GameException("Invalid XP curve entry count: " + count);
            }
            List<XpCurve.Entry> entries = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                entries.add(new XpCurve.Entry(
                        BinaryIOUtils.readIntLE(in),
                        BinaryIOUtils.readIntLE(in),
                        BinaryIOUtils.readIntLE(in)));
            }
            return entries;
        }
    }

    public static void write(File file, List<XpCurve.Entry> entries) throws IOException {
        BinaryCatalogueIO.write(file, MAGIC, VERSION, entries, (out, entry) -> {
            BinaryIOUtils.writeIntLE(out, entry.getLevel());
            BinaryIOUtils.writeIntLE(out, entry.getXpToNextLevel());
            BinaryIOUtils.writeIntLE(out, entry.getTotalXp());
        });
    }
}
