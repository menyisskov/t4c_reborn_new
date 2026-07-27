package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.render.ObjectMapping;
import com.perso.T4C.i18n.I18n;

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

/**
 * Binary serializer for editable render object definitions.
 */
public final class ObjectMappingsBinaryIO {
    private static final byte[] MAGIC = "T4COBJ".getBytes(StandardCharsets.US_ASCII);
    private static final short VERSION = 3;
    private static final int MAX_STRING_LENGTH = 4096;

    private ObjectMappingsBinaryIO() {
    }

    public static List<Entry> read(File file) throws IOException, GameException {
        try (DataInputStream in = new DataInputStream(BinaryIOUtils.openInputStream(file, 1 << 16))) {
            byte[] magic = new byte[MAGIC.length];
            in.readFully(magic);
            if (!Arrays.equals(magic, MAGIC)) {
                throw new GameException("Invalid object mappings file: wrong magic header");
            }
            short version = readShortLE(in);
            if (version > VERSION) {
                throw new GameException("Unsupported object mappings version: " + version);
            }
            int count = readIntLE(in);
            if (count < 0) {
                throw new GameException("Invalid object mapping count: " + count);
            }

            List<Entry> entries = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                Entry entry = new Entry();
                entry.logicalName = readString(in);
                entry.mapping = new ObjectMapping(
                        readIntLE(in),
                        readString(in),
                        in.readBoolean(),
                        in.readBoolean(),
                        readString(in),
                        readString(in),
                        in.readBoolean(),
                        version >= 2 ? readString(in) : "",
                        version >= 3 ? readIntLE(in) : 0);
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
                ObjectMapping mapping = entry != null ? entry.mapping : null;
                writeString(out, entry != null ? entry.logicalName : "");
                writeIntLE(out, mapping != null ? mapping.id : 0);
                writeString(out, mapping != null ? mapping.sprite : "");
                out.writeBoolean(mapping != null && mapping.clickAnimate);
                out.writeBoolean(mapping != null && mapping.mirror);
                writeString(out, mapping != null ? mapping.animateSound : "");
                writeString(out, mapping != null ? mapping.reverseAnimateSound : "");
                out.writeBoolean(mapping != null && mapping.alwaysBehindEntities);
                writeString(out, mapping != null ? I18n.placeholderFor("object", entry.logicalName, mapping.displayName) : "");
                writeIntLE(out, mapping != null ? mapping.depthTileOffsetY : 0);
            }
        }
    }

    private static String readString(DataInputStream in) throws IOException {
        return BinaryIOUtils.readString(in, MAX_STRING_LENGTH);
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
        public String logicalName;
        public ObjectMapping mapping;

        public Entry() {
        }

        public Entry(String logicalName, ObjectMapping mapping) {
            this.logicalName = logicalName;
            this.mapping = mapping;
        }
    }
}
