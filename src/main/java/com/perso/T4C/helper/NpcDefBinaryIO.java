package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.KeywordActionType;
import com.perso.T4C.npc.NpcDef;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.BodyPart;
import lombok.extern.slf4j.Slf4j;

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
 * Binary serialization for {@link NpcDef} lists. Mirrors {@link SpellBinaryIO}.
 */
@Slf4j
public final class NpcDefBinaryIO {
    private static final byte[] MAGIC = "T4CNPC".getBytes(StandardCharsets.US_ASCII);
    private static final short VERSION = 5;
    private static final int MAX_STRING_BYTES = 16384;

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
            if (version < 1 || version > VERSION) {
                throw new GameException("Unsupported NPC definition version: " + version);
            }
            int count = BinaryIOUtils.readIntLE(in);
            if (count < 0) {
                throw new GameException("Invalid NPC definition count: " + count);
            }
            List<NpcDef> defs = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                defs.add(readDef(in, version));
            }
            return defs;
        }
    }

    public static void write(File file, List<NpcDef> defs) throws IOException {
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try (DataOutputStream out = new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1 << 16))) {
            out.write(MAGIC);
            BinaryIOUtils.writeShortLE(out, VERSION);
            BinaryIOUtils.writeIntLE(out, defs == null ? 0 : defs.size());
            if (defs == null) {
                return;
            }
            for (NpcDef def : defs) {
                writeDef(out, def);
            }
        }
    }

    private static NpcDef readDef(DataInputStream in, short version) throws IOException, GameException {
        String name = readString(in);
        String displayName = I18n.english(readString(in));
        int partCount = BinaryIOUtils.readIntLE(in);
        if (partCount < 0) {
            throw new GameException("Invalid NPC part count: " + partCount);
        }
        List<NpcDef.Part> parts = new ArrayList<>(partCount);
        for (int i = 0; i < partCount; i++) {
            String bodyPartName = readString(in);
            String spriteBase = readString(in);
            BodyPart bodyPart = parseBodyPart(bodyPartName);
            if (bodyPart != null) {
                parts.add(new NpcDef.Part(bodyPart, spriteBase));
            } else {
                log.warn("Skipping unknown NPC body part '{}' for NPC '{}'", bodyPartName, name);
            }
        }
        String spriteBase = version >= 3 ? readString(in) : "";
        String dialogText = I18n.english(readString(in));
        String dialogKeyword = I18n.english(readString(in));
        KeywordActionType action = parseAction(readString(in));
        int actionParam1 = BinaryIOUtils.readIntLE(in);
        int actionParam2 = BinaryIOUtils.readIntLE(in);
        int patrolRadiusTiles = BinaryIOUtils.readIntLE(in);
        List<NpcDef.TaughtSpell> taughtSpells = new ArrayList<>();
        if (version >= 2) {
            int spellCount = BinaryIOUtils.readIntLE(in);
            if (spellCount < 0) {
                throw new GameException("Invalid NPC taught spell count: " + spellCount);
            }
            for (int i = 0; i < spellCount; i++) {
                String spellName = readString(in);
                int price = BinaryIOUtils.readIntLE(in);
                if (spellName != null && !spellName.isEmpty()) {
                    taughtSpells.add(new NpcDef.TaughtSpell(spellName, price));
                }
            }
        }
        List<NpcDef.ShopItem> shopItems = new ArrayList<>();
        List<NpcDef.TrainableStat> trainableStats = new ArrayList<>();
        if (version >= 4) {
            int shopCount = BinaryIOUtils.readIntLE(in);
            if (shopCount < 0) {
                throw new GameException("Invalid NPC shop item count: " + shopCount);
            }
            for (int i = 0; i < shopCount; i++) {
                String itemKey = readString(in);
                long price = BinaryIOUtils.readLongLE(in);
                if (itemKey != null && !itemKey.isEmpty()) {
                    shopItems.add(new NpcDef.ShopItem(itemKey, price));
                }
            }
            int trainCount = BinaryIOUtils.readIntLE(in);
            if (trainCount < 0) {
                throw new GameException("Invalid NPC trainable stat count: " + trainCount);
            }
            for (int i = 0; i < trainCount; i++) {
                String statId = readString(in);
                int costPerPoint = BinaryIOUtils.readIntLE(in);
                int maxPoints = BinaryIOUtils.readIntLE(in);
                if (statId != null && !statId.isEmpty()) {
                    trainableStats.add(new NpcDef.TrainableStat(statId, costPerPoint, maxPoints));
                }
            }
        }
        List<String> fleeShouts = new ArrayList<>();
        if (version >= 5) {
            int shoutCount = BinaryIOUtils.readIntLE(in);
            if (shoutCount < 0) {
                throw new GameException("Invalid NPC flee shout count: " + shoutCount);
            }
            for (int i = 0; i < shoutCount; i++) {
                String shout = I18n.english(readString(in));
                if (shout != null && !shout.isEmpty()) {
                    fleeShouts.add(shout);
                }
            }
        }
        return new NpcDef(name, displayName, parts, emptyToNull(spriteBase), emptyToNull(dialogText), emptyToNull(dialogKeyword),
                action, actionParam1, actionParam2, patrolRadiusTiles, taughtSpells, shopItems, trainableStats, fleeShouts);
    }

    private static void writeDef(DataOutputStream out, NpcDef def) throws IOException {
        writeString(out, def == null ? "" : def.getName());
        writeString(out, def == null ? "" : I18n.placeholderFor("npc", def.getName(), def.getDisplayName()));
        List<NpcDef.Part> parts = def == null ? null : def.getParts();
        BinaryIOUtils.writeIntLE(out, parts == null ? 0 : parts.size());
        if (parts != null) {
            for (NpcDef.Part part : parts) {
                writeString(out, part == null || part.getBodyPart() == null ? "" : part.getBodyPart().name());
                writeString(out, part == null ? "" : part.getSpriteBase());
            }
        }
        writeString(out, def == null ? "" : def.getSpriteBase());
        writeString(out, def == null ? "" : I18n.placeholderFor("npc.dialog", def.getName(), def.getDialogText()));
        writeString(out, def == null ? "" : I18n.placeholderFor("npc.keyword", def.getName(), def.getDialogKeyword()));
        KeywordActionType action = def == null || def.getAction() == null ? KeywordActionType.NONE : def.getAction();
        writeString(out, action.name());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getActionParam1());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getActionParam2());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getPatrolRadiusTiles());
        List<NpcDef.TaughtSpell> taughtSpells = def == null ? null : def.getTaughtSpells();
        BinaryIOUtils.writeIntLE(out, taughtSpells == null ? 0 : taughtSpells.size());
        if (taughtSpells != null) {
            for (NpcDef.TaughtSpell spell : taughtSpells) {
                writeString(out, spell == null ? "" : spell.getSpellName());
                BinaryIOUtils.writeIntLE(out, spell == null ? 0 : spell.getPrice());
            }
        }
        List<NpcDef.ShopItem> shopItems = def == null ? null : def.getShopItems();
        BinaryIOUtils.writeIntLE(out, shopItems == null ? 0 : shopItems.size());
        if (shopItems != null) {
            for (NpcDef.ShopItem item : shopItems) {
                writeString(out, item == null ? "" : item.getItemKey());
                BinaryIOUtils.writeLongLE(out, item == null ? 0L : item.getPrice());
            }
        }
        List<NpcDef.TrainableStat> trainableStats = def == null ? null : def.getTrainableStats();
        BinaryIOUtils.writeIntLE(out, trainableStats == null ? 0 : trainableStats.size());
        if (trainableStats != null) {
            for (NpcDef.TrainableStat stat : trainableStats) {
                writeString(out, stat == null ? "" : stat.getStatId());
                BinaryIOUtils.writeIntLE(out, stat == null ? 0 : stat.getCostPerPoint());
                BinaryIOUtils.writeIntLE(out, stat == null ? 0 : stat.getMaxPoints());
            }
        }
        List<String> fleeShouts = def == null ? null : def.getFleeShouts();
        BinaryIOUtils.writeIntLE(out, fleeShouts == null ? 0 : fleeShouts.size());
        if (fleeShouts != null) {
            for (int i = 0; i < fleeShouts.size(); i++) {
                // Each shout gets its own indexed key, since a placeholder must
                // round-trip back to this exact line and not to a sibling.
                String key = "npc.flee_shout." + I18n.normalizedKey(def.getName()) + "." + i;
                writeString(out, I18n.placeholderForKey(key, fleeShouts.get(i)));
            }
        }
    }

    private static BodyPart parseBodyPart(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        try {
            return BodyPart.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static KeywordActionType parseAction(String name) {
        if (name == null || name.isEmpty()) {
            return KeywordActionType.NONE;
        }
        try {
            return KeywordActionType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return KeywordActionType.NONE;
        }
    }

    private static String readString(DataInputStream in) throws IOException {
        return BinaryIOUtils.readString(in, MAX_STRING_BYTES);
    }

    private static void writeString(DataOutputStream out, String value) throws IOException {
        BinaryIOUtils.writeString(out, value);
    }

    private static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }
}
