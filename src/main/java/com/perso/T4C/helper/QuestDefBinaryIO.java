package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.quest.QuestDef;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Binary persistence for the data-driven quest catalogue. */
public final class QuestDefBinaryIO {
    private static final byte[] MAGIC = "T4CQST".getBytes(StandardCharsets.US_ASCII);
    private static final short VERSION = 2;
    private static final int MAX_STRING_BYTES = 16_384;

    private QuestDefBinaryIO() {
    }

    public static List<QuestDef> read(File file) throws IOException, GameException {
        return BinaryCatalogueIO.read(file, MAGIC, "quest definition",
                version -> {
                    if (version != 1 && version != VERSION) {
                        throw new GameException("Unsupported quest definition version: " + version
                                + " (expected " + VERSION + ")");
                    }
                },
                QuestDefBinaryIO::readDefinition);
    }

    public static void write(File file, List<QuestDef> definitions) throws IOException {
        BinaryCatalogueIO.write(file, MAGIC, VERSION, definitions, QuestDefBinaryIO::writeDefinition);
    }

    private static QuestDef readDefinition(DataInputStream in, short version) throws IOException {
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
                readString(in),
                version >= 2 ? emptyToNull(readString(in)) : null
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
        writeString(out, definition.getActivationFlag());
    }

    private static String readString(DataInputStream in) throws IOException {
        return BinaryIOUtils.readString(in, MAX_STRING_BYTES);
    }

    private static void writeString(DataOutputStream out, String value) throws IOException {
        BinaryIOUtils.writeString(out, value == null ? "" : value);
    }

    private static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }
}
