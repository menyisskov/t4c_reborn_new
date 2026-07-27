package com.perso.T4C.helper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Binary serializer for editable sprite collision generation rules.
 */
public final class CollisionRuleBinaryIO {
    private static final byte[] MAGIC = new byte[] { 'T', '4', 'C', 'C', 'R', 'L' };
    private static final short VERSION = 2;

    private CollisionRuleBinaryIO() {
    }

    public static CollisionRules read(File file) throws IOException {
        try (DataInputStream in = new DataInputStream(BinaryIOUtils.openInputStream(file, 1 << 16))) {
            byte[] magic = new byte[MAGIC.length];
            in.readFully(magic);
            if (!Arrays.equals(magic, MAGIC)) {
                throw new IOException("Invalid collision rules file: wrong magic header");
            }
            short version = BinaryIOUtils.readShortLE(in);
            if (version < 1 || version > VERSION) {
                throw new IOException("Unsupported collision rules version: " + version);
            }

            CollisionRules rules = new CollisionRules();
            in.readBoolean();
            rules.defaultDecorCollision = false;
            rules.defaultCollisionValue = BinaryIOUtils.readIntLE(in);
            rules.ignoredSprites = readStringList(in);
            rules.exactSprites = new LinkedHashMap<>();
            int exactCount = BinaryIOUtils.readIntLE(in);
            for (int i = 0; i < exactCount; i++) {
                rules.exactSprites.put(readString(in), readRule(in, version));
            }
            rules.nameContainsRules = new ArrayList<>();
            int containsCount = BinaryIOUtils.readIntLE(in);
            for (int i = 0; i < containsCount; i++) {
                CollisionNameRule nameRule = new CollisionNameRule();
                nameRule.contains = readStringList(in);
                nameRule.rule = readRule(in, version);
                rules.nameContainsRules.add(nameRule);
            }
            return rules;
        }
    }

    public static void write(File file, CollisionRules rules) throws IOException {
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        CollisionRules safeRules = rules != null ? rules : new CollisionRules();
        try (DataOutputStream out = new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1 << 16))) {
            out.write(MAGIC);
            BinaryIOUtils.writeShortLE(out, VERSION);
            out.writeBoolean(safeRules.defaultDecorCollision);
            BinaryIOUtils.writeIntLE(out, safeRules.defaultCollisionValue);
            writeStringList(out, safeRules.ignoredSprites);

            Map<String, CollisionRule> exact = safeRules.exactSprites != null ? safeRules.exactSprites : Map.of();
            BinaryIOUtils.writeIntLE(out, exact.size());
            for (Map.Entry<String, CollisionRule> entry : exact.entrySet()) {
                writeString(out, entry.getKey());
                writeRule(out, entry.getValue());
            }

            List<CollisionNameRule> contains = safeRules.nameContainsRules != null ? safeRules.nameContainsRules : List.of();
            BinaryIOUtils.writeIntLE(out, contains.size());
            for (CollisionNameRule nameRule : contains) {
                writeStringList(out, nameRule != null ? nameRule.contains : null);
                writeRule(out, nameRule != null ? nameRule.rule : null);
            }
        }
    }

    private static CollisionRule readRule(DataInputStream in, short version) throws IOException {
        CollisionRule rule = new CollisionRule();
        rule.value = BinaryIOUtils.readIntLE(in);
        int tileCount = BinaryIOUtils.readIntLE(in);
        rule.tiles = new ArrayList<>(Math.max(0, tileCount));
        for (int i = 0; i < tileCount; i++) {
            rule.tiles.add(new int[] { BinaryIOUtils.readIntLE(in), BinaryIOUtils.readIntLE(in) });
        }
        rule.clearTiles = new ArrayList<>();
        if (version >= 2) {
            int clearTileCount = BinaryIOUtils.readIntLE(in);
            for (int i = 0; i < clearTileCount; i++) {
                rule.clearTiles.add(new int[] { BinaryIOUtils.readIntLE(in), BinaryIOUtils.readIntLE(in) });
            }
        }
        return rule;
    }

    private static void writeRule(DataOutputStream out, CollisionRule rule) throws IOException {
        CollisionRule safeRule = rule != null ? rule : new CollisionRule();
        BinaryIOUtils.writeIntLE(out, safeRule.value);
        List<int[]> tiles = safeRule.tiles != null ? safeRule.tiles : List.of();
        BinaryIOUtils.writeIntLE(out, tiles.size());
        for (int[] tile : tiles) {
            BinaryIOUtils.writeIntLE(out, tile != null && tile.length > 0 ? tile[0] : 0);
            BinaryIOUtils.writeIntLE(out, tile != null && tile.length > 1 ? tile[1] : 0);
        }
        List<int[]> clearTiles = safeRule.clearTiles != null ? safeRule.clearTiles : List.of();
        BinaryIOUtils.writeIntLE(out, clearTiles.size());
        for (int[] tile : clearTiles) {
            BinaryIOUtils.writeIntLE(out, tile != null && tile.length > 0 ? tile[0] : 0);
            BinaryIOUtils.writeIntLE(out, tile != null && tile.length > 1 ? tile[1] : 0);
        }
    }

    private static List<String> readStringList(DataInputStream in) throws IOException {
        int count = BinaryIOUtils.readIntLE(in);
        List<String> values = new ArrayList<>(Math.max(0, count));
        for (int i = 0; i < count; i++) {
            values.add(readString(in));
        }
        return values;
    }

    private static void writeStringList(DataOutputStream out, List<String> values) throws IOException {
        List<String> safeValues = values != null ? values : List.of();
        BinaryIOUtils.writeIntLE(out, safeValues.size());
        for (String value : safeValues) {
            writeString(out, value);
        }
    }

    private static String readString(DataInputStream in) throws IOException {
        int length = BinaryIOUtils.readIntLE(in);
        if (length < 0 || length > 1_000_000) {
            throw new IOException("Invalid string length: " + length);
        }
        byte[] bytes = in.readNBytes(length);
        if (bytes.length != length) {
            throw new IOException("Unexpected EOF while reading string");
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static void writeString(DataOutputStream out, String value) throws IOException {
        byte[] bytes = (value != null ? value : "").getBytes(StandardCharsets.UTF_8);
        BinaryIOUtils.writeIntLE(out, bytes.length);
        out.write(bytes);
    }

    public static final class CollisionRules {
        public boolean defaultDecorCollision = false;
        public int defaultCollisionValue = 1;
        public List<String> ignoredSprites = new ArrayList<>();
        public Map<String, CollisionRule> exactSprites = new LinkedHashMap<>();
        public List<CollisionNameRule> nameContainsRules = new ArrayList<>();
    }

    public static final class CollisionNameRule {
        public List<String> contains = new ArrayList<>();
        public CollisionRule rule = new CollisionRule();
    }

    public static final class CollisionRule {
        public int value = 1;
        public List<int[]> tiles = new ArrayList<>();
        public List<int[]> clearTiles = new ArrayList<>();
    }
}
