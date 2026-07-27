package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.NpcDef;
import com.perso.T4C.player.BodyPart;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Binary persistence for the reset, non-branching NPC dialogue format. */
public final class NpcDefBinaryIO {
    private static final byte[] MAGIC = "T4CNPC".getBytes(StandardCharsets.US_ASCII);
    private static final short VERSION = 10;
    private static final int MAX_STRING_BYTES = 16_384;

    private NpcDefBinaryIO() {
    }

    public static List<NpcDef> read(File file) throws IOException, GameException {
        try (DataInputStream in = new DataInputStream(BinaryIOUtils.openInputStream(file, 1 << 16))) {
            byte[] magic = new byte[MAGIC.length];
            in.readFully(magic);
            if (!Arrays.equals(magic, MAGIC)) {
                throw new GameException("Invalid NPC definition file: wrong magic header");
            }
            short version = BinaryIOUtils.readShortLE(in);
            if (version != VERSION) {
                throw new GameException("Unsupported NPC definition version: " + version
                        + " (expected " + VERSION + ")");
            }
            int count = checkedCount(BinaryIOUtils.readIntLE(in), "definition");
            List<NpcDef> defs = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                defs.add(readDef(in));
            }
            return defs;
        }
    }

    public static void write(File file, List<NpcDef> defs) throws IOException {
        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();
        try (DataOutputStream out = new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1 << 16))) {
            out.write(MAGIC);
            BinaryIOUtils.writeShortLE(out, VERSION);
            List<NpcDef> safeDefs = defs == null ? List.of() : defs;
            BinaryIOUtils.writeIntLE(out, safeDefs.size());
            for (NpcDef def : safeDefs) writeDef(out, def);
        }
    }

    private static NpcDef readDef(DataInputStream in) throws IOException, GameException {
        String name = readString(in);
        String displayName = readString(in);
        int partCount = checkedCount(BinaryIOUtils.readIntLE(in), "part");
        List<NpcDef.Part> parts = new ArrayList<>(partCount);
        for (int i = 0; i < partCount; i++) {
            String bodyPart = readString(in);
            String sprite = readString(in);
            try {
                parts.add(new NpcDef.Part(BodyPart.valueOf(bodyPart), sprite));
            } catch (IllegalArgumentException e) {
                throw new GameException("Unknown NPC body part: " + bodyPart);
            }
        }
        String spriteBase = emptyToNull(readString(in));
        int patrolRadius = BinaryIOUtils.readIntLE(in);
        int shoutCount = checkedCount(BinaryIOUtils.readIntLE(in), "flee shout");
        List<String> shouts = new ArrayList<>(shoutCount);
        for (int i = 0; i < shoutCount; i++) shouts.add(readString(in));
        String welcome = readString(in);
        int topicCount = checkedCount(BinaryIOUtils.readIntLE(in), "dialogue topic");
        List<NpcDef.DialogTopic> topics = new ArrayList<>(topicCount);
        for (int i = 0; i < topicCount; i++) topics.add(readTopic(in));
        return new NpcDef(name, displayName, parts, spriteBase, patrolRadius, shouts, welcome, topics);
    }

    private static NpcDef.DialogTopic readTopic(DataInputStream in) throws IOException, GameException {
        int keywordCount = checkedCount(BinaryIOUtils.readIntLE(in), "keyword");
        List<String> keywords = new ArrayList<>(keywordCount);
        for (int i = 0; i < keywordCount; i++) keywords.add(readString(in));
        String response = readString(in);
        int actionCount = checkedCount(BinaryIOUtils.readIntLE(in), "action");
        List<NpcDef.Action> actions = new ArrayList<>(actionCount);
        for (int i = 0; i < actionCount; i++) {
            String rawType = readString(in);
            ActionType type;
            try {
                type = ActionType.valueOf(rawType);
            } catch (IllegalArgumentException e) {
                throw new GameException("Unknown NPC dialogue action: " + rawType);
            }
            int targetCount = checkedCount(BinaryIOUtils.readIntLE(in), "action target");
            List<String> targets = new ArrayList<>(targetCount);
            for (int j = 0; j < targetCount; j++) targets.add(readString(in));
            actions.add(new NpcDef.Action(type, targets));
        }
        return new NpcDef.DialogTopic(keywords, emptyToNull(response), actions);
    }

    private static void writeDef(DataOutputStream out, NpcDef def) throws IOException {
        writeString(out, def.getName());
        writeString(out, I18n.placeholderFor("npc", def.getName(), def.getDisplayName()));
        BinaryIOUtils.writeIntLE(out, def.getParts().size());
        for (NpcDef.Part part : def.getParts()) {
            writeString(out, part.getBodyPart().name());
            writeString(out, part.getSpriteBase());
        }
        writeString(out, def.getSpriteBase());
        BinaryIOUtils.writeIntLE(out, def.getPatrolRadiusTiles());
        BinaryIOUtils.writeIntLE(out, def.getFleeShouts().size());
        for (int i = 0; i < def.getFleeShouts().size(); i++) {
            writeString(out, I18n.placeholderForKey("npc.flee_shout."
                    + I18n.normalizedKey(def.getName()) + "." + i, def.getFleeShouts().get(i)));
        }
        writeString(out, I18n.placeholderForKey("npc.welcome."
                + I18n.normalizedKey(def.getName()), def.getWelcomeText()));
        BinaryIOUtils.writeIntLE(out, def.getTopics().size());
        for (int i = 0; i < def.getTopics().size(); i++) writeTopic(out, def, i, def.getTopics().get(i));
    }

    private static void writeTopic(DataOutputStream out, NpcDef def, int topicIndex,
                                   NpcDef.DialogTopic topic) throws IOException {
        BinaryIOUtils.writeIntLE(out, topic.getKeywords().size());
        for (int i = 0; i < topic.getKeywords().size(); i++) {
            writeString(out, I18n.placeholderForKey("npc.topic_keyword."
                    + I18n.normalizedKey(def.getName()) + "." + topicIndex + "." + i,
                    topic.getKeywords().get(i)));
        }
        writeString(out, I18n.placeholderForKey("npc.topic."
                + I18n.normalizedKey(def.getName()) + "." + topicIndex, topic.getResponse()));
        BinaryIOUtils.writeIntLE(out, topic.getActions().size());
        for (NpcDef.Action action : topic.getActions()) {
            writeString(out, action.getType().name());
            BinaryIOUtils.writeIntLE(out, action.getTargets().size());
            for (String target : action.getTargets()) writeString(out, target);
        }
    }

    private static int checkedCount(int count, String label) throws GameException {
        if (count < 0) throw new GameException("Invalid NPC " + label + " count: " + count);
        return count;
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
