package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.npc.CompanionDef;
import com.perso.T4C.npc.CompanionSpellTrigger;
import com.perso.T4C.player.BodyPart;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Binary persistence for ally companion definitions. */
public final class CompanionDefBinaryIO {
    private static final byte[] MAGIC = "T4CCMP".getBytes(StandardCharsets.US_ASCII);
    private static final short VERSION = 1;
    private static final int MAX_STRING_BYTES = 16_384;

    private CompanionDefBinaryIO() {
    }

    public static List<CompanionDef> read(File file) throws IOException, GameException {
        return BinaryCatalogueIO.read(file, MAGIC, "companion definition",
                version -> {
                    if (version != VERSION) {
                        throw new GameException("Unsupported companion definition version: " + version
                                + " (expected " + VERSION + ")");
                    }
                },
                (in, version) -> readDef(in));
    }

    public static void write(File file, List<CompanionDef> defs) throws IOException {
        BinaryCatalogueIO.write(file, MAGIC, VERSION, defs, CompanionDefBinaryIO::writeDef);
    }

    private static CompanionDef readDef(DataInputStream in) throws IOException, GameException {
        String id = readString(in);
        String displayName = readString(in);

        int partCount = checkedCount(BinaryIOUtils.readIntLE(in), "part");
        List<CompanionDef.Part> parts = new ArrayList<>(partCount);
        for (int i = 0; i < partCount; i++) {
            String bodyPart = readString(in);
            String sprite = readString(in);
            try {
                parts.add(new CompanionDef.Part(BodyPart.valueOf(bodyPart), sprite));
            } catch (IllegalArgumentException e) {
                throw new GameException("Unknown companion body part: " + bodyPart, e);
            }
        }
        String spriteBase = emptyToNull(readString(in));

        int baseHp = BinaryIOUtils.readIntLE(in);
        float hpPerLevel = readFloat(in);
        int damageMin = BinaryIOUtils.readIntLE(in);
        int damageMax = BinaryIOUtils.readIntLE(in);
        float damagePerLevel = readFloat(in);
        float attackCooldown = readFloat(in);
        float speed = readFloat(in);

        int spellCount = checkedCount(BinaryIOUtils.readIntLE(in), "spell");
        List<CompanionDef.SpellEntry> spells = new ArrayList<>(spellCount);
        for (int i = 0; i < spellCount; i++) {
            spells.add(readSpell(in));
        }
        return new CompanionDef(id, displayName, parts, spriteBase, baseHp, hpPerLevel,
                damageMin, damageMax, damagePerLevel, attackCooldown, speed, spells);
    }

    private static CompanionDef.SpellEntry readSpell(DataInputStream in) throws IOException, GameException {
        String spellKey = readString(in);
        String trigger = readString(in);
        CompanionSpellTrigger parsedTrigger;
        try {
            parsedTrigger = CompanionSpellTrigger.valueOf(trigger);
        } catch (IllegalArgumentException e) {
            throw new GameException("Unknown companion spell trigger: " + trigger, e);
        }
        int priority = BinaryIOUtils.readIntLE(in);
        float cooldownSeconds = readFloat(in);
        float healthThreshold = readFloat(in);
        int minDamage = BinaryIOUtils.readIntLE(in);
        int maxDamage = BinaryIOUtils.readIntLE(in);
        float damagePerLevel = readFloat(in);
        float rangeTiles = readFloat(in);
        return new CompanionDef.SpellEntry(spellKey, parsedTrigger, priority, cooldownSeconds,
                healthThreshold, minDamage, maxDamage, damagePerLevel, rangeTiles);
    }

    private static void writeDef(DataOutputStream out, CompanionDef def) throws IOException {
        writeString(out, def.getId());
        // Display names live in lang.json, exactly as NPC definitions do.
        writeString(out, I18n.placeholderFor("companion", def.getId(), def.getDisplayName()));

        BinaryIOUtils.writeIntLE(out, def.getParts().size());
        for (CompanionDef.Part part : def.getParts()) {
            writeString(out, part.getBodyPart().name());
            writeString(out, part.getSpriteBase());
        }
        writeString(out, def.getSpriteBase());

        BinaryIOUtils.writeIntLE(out, def.getBaseHp());
        writeFloat(out, def.getHpPerLevel());
        BinaryIOUtils.writeIntLE(out, def.getDamageMin());
        BinaryIOUtils.writeIntLE(out, def.getDamageMax());
        writeFloat(out, def.getDamagePerLevel());
        writeFloat(out, def.getAttackCooldown());
        writeFloat(out, def.getSpeed());

        BinaryIOUtils.writeIntLE(out, def.getSpells().size());
        for (CompanionDef.SpellEntry spell : def.getSpells()) {
            writeString(out, spell.getSpellKey());
            writeString(out, spell.getTrigger().name());
            BinaryIOUtils.writeIntLE(out, spell.getPriority());
            writeFloat(out, spell.getCooldownSeconds());
            writeFloat(out, spell.getHealthThreshold());
            BinaryIOUtils.writeIntLE(out, spell.getMinDamage());
            BinaryIOUtils.writeIntLE(out, spell.getMaxDamage());
            writeFloat(out, spell.getDamagePerLevel());
            writeFloat(out, spell.getRangeTiles());
        }
    }

    /** BinaryIOUtils has no float helper; store the IEEE-754 bits little-endian. */
    private static float readFloat(DataInputStream in) throws IOException {
        return Float.intBitsToFloat(BinaryIOUtils.readIntLE(in));
    }

    private static void writeFloat(DataOutputStream out, float value) throws IOException {
        BinaryIOUtils.writeIntLE(out, Float.floatToIntBits(value));
    }

    private static int checkedCount(int count, String label) throws GameException {
        if (count < 0) throw new GameException("Invalid companion " + label + " count: " + count);
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
