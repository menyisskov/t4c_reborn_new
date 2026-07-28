package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.quest.QuestDef;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Binary persistence for the data-driven quest catalogue. */
public final class QuestDefBinaryIO {
    private static final byte[] MAGIC = "T4CQST".getBytes(StandardCharsets.US_ASCII);
    private static final short VERSION = 1;
    private static final int MAX_STRING_BYTES = 16_384;

    private QuestDefBinaryIO() {
    }

    public static List<QuestDef> read(File file) throws IOException, GameException {
        try (DataInputStream in = new DataInputStream(BinaryIOUtils.openInputStream(file, 1 << 16))) {
            byte[] magic = new byte[MAGIC.length];
            in.readFully(magic);
            if (!Arrays.equals(magic, MAGIC)) {
                throw new GameException("Invalid quest definition file: wrong magic header");
            }
            short version = BinaryIOUtils.readShortLE(in);
            if (version != VERSION) {
                throw new GameException("Unsupported quest definition version: " + version
                        + " (expected " + VERSION + ")");
            }
            int count = checkedCount(BinaryIOUtils.readIntLE(in), "definition");
            List<QuestDef> definitions = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                definitions.add(readDefinition(in));
            }
            return definitions;
        }
    }

    public static void write(File file, List<QuestDef> definitions) throws IOException {
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try (DataOutputStream out = new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1 << 16))) {
            out.write(MAGIC);
            BinaryIOUtils.writeShortLE(out, VERSION);
            List<QuestDef> safeDefinitions = definitions == null ? List.of() : definitions;
            BinaryIOUtils.writeIntLE(out, safeDefinitions.size());
            for (QuestDef definition : safeDefinitions) {
                writeDefinition(out, definition);
            }
        }
    }

    private static QuestDef readDefinition(DataInputStream in) throws IOException {
        return new QuestDef(
                readString(in),
                readString(in),
                readString(in),
                readString(in),
                BinaryIOUtils.readIntLE(in),
                BinaryIOUtils.readIntLE(in),
                BinaryIOUtils.readIntLE(in),
                BinaryIOUtils.readIntLE(in),
                BinaryIOUtils.readIntLE(in),
                BinaryIOUtils.readIntLE(in),
                BinaryIOUtils.readIntLE(in),
                readString(in),
                readString(in),
                readString(in)
        );
    }

    private static void writeDefinition(DataOutputStream out, QuestDef definition) throws IOException {
        String normalizedId = I18n.normalizedKey(definition.getId());
        writeString(out, definition.getId());
        writeString(out, I18n.placeholderForKey("quest." + normalizedId + ".title", definition.getTitle()));
        writeString(out, definition.getGiverNpc());
        writeString(out, definition.getTargetMonster());
        BinaryIOUtils.writeIntLE(out, definition.getRequiredKills());
        BinaryIOUtils.writeIntLE(out, definition.getTargetWorldZ());
        BinaryIOUtils.writeIntLE(out, definition.getAreaCenterX());
        BinaryIOUtils.writeIntLE(out, definition.getAreaCenterY());
        BinaryIOUtils.writeIntLE(out, definition.getAreaRadiusTiles());
        BinaryIOUtils.writeIntLE(out, definition.getRewardGold());
        BinaryIOUtils.writeIntLE(out, definition.getRewardXp());
        writeString(out, I18n.placeholderForKey("quest." + normalizedId + ".offer",
                definition.getOfferText()));
        writeString(out, I18n.placeholderForKey("quest." + normalizedId + ".completion",
                definition.getCompletionText()));
        writeString(out, I18n.placeholderForKey("quest." + normalizedId + ".completed",
                definition.getCompletedText()));
    }

    private static int checkedCount(int count, String label) throws GameException {
        if (count < 0) {
            throw new GameException("Invalid quest " + label + " count: " + count);
        }
        return count;
    }

    private static String readString(DataInputStream in) throws IOException {
        return BinaryIOUtils.readString(in, MAX_STRING_BYTES);
    }

    private static void writeString(DataOutputStream out, String value) throws IOException {
        BinaryIOUtils.writeString(out, value == null ? "" : value);
    }
}
