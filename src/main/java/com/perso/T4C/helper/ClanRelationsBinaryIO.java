package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.MonsterClan;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
        return BinaryCatalogueIO.read(file, MAGIC, "clan relations",
                version -> {
                    if (version != VERSION) {
                        throw new GameException("Unsupported clan relations version: " + version);
                    }
                },
                (in, version) -> {
                    Entry entry = new Entry();
                    entry.source = readClan(in);
                    entry.target = readClan(in);
                    return entry;
                });
    }

    public static void write(File file, List<Entry> entries) throws IOException {
        BinaryCatalogueIO.write(file, MAGIC, VERSION, entries, (out, entry) -> {
            writeString(out, entry != null && entry.source != null ? entry.source.name() : MonsterClan.NEUTRAL.name());
            writeString(out, entry != null && entry.target != null ? entry.target.name() : MonsterClan.NEUTRAL.name());
        });
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
