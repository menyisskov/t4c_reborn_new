package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.BodyPart;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Binary serialization for {@link ItemDefinition} lists.
 *
 * <p>Version history:
 * <ul>
 *   <li>v1 — original fields + 8-byte legacy padding</li>
 *   <li>v2 — removed legacy padding</li>
 *   <li>v3 — added attackSpeed</li>
 *   <li>v4 — added T4C fields: numId, structure, appearanceId, dmgFormula, atkDelay,
 *             radiance, nbCharges, canSummon, lockName, lockDiff, signText,
 *             containerGold, globalRespawn, localRespawn, spells[]</li>
 * </ul>
 */
public final class ItemDefBinaryIO {
    private static final byte[] MAGIC = "T4CITEM".getBytes(StandardCharsets.US_ASCII);
    private static final short VERSION = 6;
    private static final int MAX_STRING_BYTES = 16384;

    private ItemDefBinaryIO() {
    }

    public static List<ItemDefinition> read(File file) throws IOException, GameException {
        try (DataInputStream in = new DataInputStream(BinaryIOUtils.openInputStream(file, 1 << 16))) {
            byte[] magic = new byte[MAGIC.length];
            in.readFully(magic);
            if (!Arrays.equals(magic, MAGIC)) {
                throw new GameException("Invalid item definition file: wrong magic header");
            }
            short version = BinaryIOUtils.readShortLE(in);
            if (version < 1 || version > VERSION) {
                throw new GameException("Unsupported item definition version: " + version);
            }
            int count = BinaryIOUtils.readIntLE(in);
            if (count < 0) {
                throw new GameException("Invalid item definition count: " + count);
            }
            List<ItemDefinition> defs = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                defs.add(readDef(in, version));
            }
            return defs;
        }
    }

    public static void write(File file, List<ItemDefinition> defs) throws IOException {
        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();
        try (DataOutputStream out = new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1 << 16))) {
            out.write(MAGIC);
            BinaryIOUtils.writeShortLE(out, VERSION);
            BinaryIOUtils.writeIntLE(out, defs == null ? 0 : defs.size());
            if (defs == null) return;
            for (ItemDefinition def : defs) writeDef(out, def);
        }
    }

    private static ItemDefinition readDef(DataInputStream in, short version) throws IOException {
        String key = readString(in);
        String name = I18n.english(readString(in));
        if (version == 1) BinaryIOUtils.readLongLE(in); // legacy padding
        BodyPart bodyPart = readBodyPart(in);
        String appearanceEquippedPrimary = readString(in);
        BodyPart secondaryBodyPart = readBodyPart(in);
        String appearanceEquippedSecondary = readString(in);
        String appearanceInventory = readString(in);
        long price = BinaryIOUtils.readLongLE(in);
        long weight = BinaryIOUtils.readLongLE(in);
        double armorClass = Double.longBitsToDouble(BinaryIOUtils.readLongLE(in));
        long dodgeLost = BinaryIOUtils.readLongLE(in);
        long minEnd = BinaryIOUtils.readLongLE(in);
        long reqAttack = BinaryIOUtils.readLongLE(in);
        long reqStr = BinaryIOUtils.readLongLE(in);
        long reqAgi = BinaryIOUtils.readLongLE(in);
        long minInt = BinaryIOUtils.readLongLE(in);
        long minWis = BinaryIOUtils.readLongLE(in);
        double attackSpeed = version >= 3 ? Double.longBitsToDouble(BinaryIOUtils.readLongLE(in)) : 1.0d;
        boolean unique = in.readBoolean();
        boolean bow = in.readBoolean();
        boolean unlimitedUse = in.readBoolean();

        // v4 fields
        int numId = 0, structure = 0, appearanceId = 0;
        String dmgFormula = null, atkDelay = null, lockName = null, signText = null;
        int radiance = 0, nbCharges = 0, lockDiff = 0, containerGold = 0, globalRespawn = 0, localRespawn = 0;
        boolean canSummon = false;
        List<ItemDefinition.ItemSpell> spells = Collections.emptyList();
        List<ItemDefinition.ItemBoost> boosts = Collections.emptyList();

        if (version >= 4) {
            numId = BinaryIOUtils.readIntLE(in);
            structure = BinaryIOUtils.readIntLE(in);
            appearanceId = BinaryIOUtils.readIntLE(in);
            dmgFormula = emptyToNull(readString(in));
            atkDelay = emptyToNull(readString(in));
            radiance = BinaryIOUtils.readIntLE(in);
            nbCharges = BinaryIOUtils.readIntLE(in);
            canSummon = in.readBoolean();
            lockName = emptyToNull(readString(in));
            lockDiff = BinaryIOUtils.readIntLE(in);
            signText = emptyToNull(I18n.english(readString(in)));
            containerGold = BinaryIOUtils.readIntLE(in);
            globalRespawn = BinaryIOUtils.readIntLE(in);
            localRespawn = BinaryIOUtils.readIntLE(in);
            int spellCount = BinaryIOUtils.readIntLE(in);
            if (spellCount > 0) {
                spells = new ArrayList<>(spellCount);
                for (int i = 0; i < spellCount; i++) {
                    int spellId = BinaryIOUtils.readIntLE(in);
                    int level = BinaryIOUtils.readIntLE(in);
                    int chance = BinaryIOUtils.readIntLE(in);
                    spells.add(new ItemDefinition.ItemSpell(spellId, level, chance));
                }
            }
        }
        if (version >= 5) {
            int boostCount = BinaryIOUtils.readIntLE(in);
            if (boostCount > 0) {
                boosts = new ArrayList<>(boostCount);
                for (int i = 0; i < boostCount; i++) {
                    boosts.add(new ItemDefinition.ItemBoost(BinaryIOUtils.readIntLE(in),
                            BinaryIOUtils.readIntLE(in), readString(in),
                            BinaryIOUtils.readIntLE(in), BinaryIOUtils.readIntLE(in)));
                }
            }
        }
        List<ItemDefinition.ContainerLootGroup> containerLootGroups = Collections.emptyList();
        if (version >= 6) {
            int groupCount = BinaryIOUtils.readIntLE(in);
            if (groupCount > 0) {
                containerLootGroups = new ArrayList<>(groupCount);
                for (int group = 0; group < groupCount; group++) {
                    int itemCount = BinaryIOUtils.readIntLE(in);
                    List<String> items = new ArrayList<>(Math.max(0, itemCount));
                    for (int item = 0; item < itemCount; item++) items.add(readString(in));
                    containerLootGroups.add(new ItemDefinition.ContainerLootGroup(items));
                }
            }
        }

        return new ItemDefinition(
                key, name,
                bodyPart, emptyToNull(appearanceEquippedPrimary),
                secondaryBodyPart, emptyToNull(appearanceEquippedSecondary),
                emptyToNull(appearanceInventory),
                price, weight, armorClass,
                dodgeLost, minEnd, reqAttack, reqStr, reqAgi, minInt, minWis,
                attackSpeed, unique, bow, unlimitedUse,
                numId, structure, appearanceId,
                dmgFormula, atkDelay,
                radiance, nbCharges, canSummon,
                lockName, lockDiff, signText,
                containerGold, globalRespawn, localRespawn,
                spells, boosts, containerLootGroups);
    }

    private static void writeDef(DataOutputStream out, ItemDefinition def) throws IOException {
        writeString(out, def == null ? "" : def.getKey());
        writeString(out, def == null ? "" : I18n.placeholderFor("item", def.getKey(), def.getName()));
        writeBodyPart(out, def == null ? null : def.getBodyPart());
        writeString(out, def == null ? "" : def.getAppearanceEquippedPrimary());
        writeBodyPart(out, def == null ? null : def.getSecondaryBodyPart());
        writeString(out, def == null ? "" : def.getAppearanceEquippedSecondary());
        writeString(out, def == null ? "" : def.getAppearanceInventory());
        BinaryIOUtils.writeLongLE(out, def == null ? 0L : def.getPrice());
        BinaryIOUtils.writeLongLE(out, def == null ? 0L : def.getWeight());
        BinaryIOUtils.writeLongLE(out, Double.doubleToLongBits(def == null ? 0d : def.getArmorClass()));
        BinaryIOUtils.writeLongLE(out, def == null ? 0L : def.getDodgeLost());
        BinaryIOUtils.writeLongLE(out, def == null ? 0L : def.getMinEnd());
        BinaryIOUtils.writeLongLE(out, def == null ? 0L : def.getReqAttack());
        BinaryIOUtils.writeLongLE(out, def == null ? 0L : def.getReqStr());
        BinaryIOUtils.writeLongLE(out, def == null ? 0L : def.getReqAgi());
        BinaryIOUtils.writeLongLE(out, def == null ? 0L : def.getMinInt());
        BinaryIOUtils.writeLongLE(out, def == null ? 0L : def.getMinWis());
        BinaryIOUtils.writeLongLE(out, Double.doubleToLongBits(def == null ? 1.0d : def.getAttackSpeed()));
        out.writeBoolean(def != null && def.isUnique());
        out.writeBoolean(def != null && def.isBow());
        out.writeBoolean(def != null && def.isUnlimitedUse());

        // v4 fields
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getNumId());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getStructure());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getAppearanceId());
        writeString(out, def == null ? "" : def.getDmgFormula());
        writeString(out, def == null ? "" : def.getAtkDelay());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getRadiance());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getNbCharges());
        out.writeBoolean(def != null && def.isCanSummon());
        writeString(out, def == null ? "" : def.getLockName());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getLockDiff());
        writeString(out, def == null ? "" : I18n.placeholderFor("item.sign", def.getKey(), def.getSignText()));
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getContainerGold());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getGlobalRespawn());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getLocalRespawn());
        List<ItemDefinition.ItemSpell> spells = def == null ? null : def.getSpells();
        BinaryIOUtils.writeIntLE(out, spells == null ? 0 : spells.size());
        if (spells != null) {
            for (ItemDefinition.ItemSpell s : spells) {
                BinaryIOUtils.writeIntLE(out, s == null ? 0 : s.getSpellId());
                BinaryIOUtils.writeIntLE(out, s == null ? 0 : s.getLevel());
                BinaryIOUtils.writeIntLE(out, s == null ? 0 : s.getChance());
            }
        }
        List<ItemDefinition.ItemBoost> boosts = def == null ? null : def.getBoosts();
        BinaryIOUtils.writeIntLE(out, boosts == null ? 0 : boosts.size());
        if (boosts != null) {
            for (ItemDefinition.ItemBoost boost : boosts) {
                BinaryIOUtils.writeIntLE(out, boost == null ? 0 : boost.getBoostId());
                BinaryIOUtils.writeIntLE(out, boost == null ? 0 : boost.getStatId());
                writeString(out, boost == null ? "0" : boost.getExpression());
                BinaryIOUtils.writeIntLE(out, boost == null ? 0 : boost.getMinWis());
                BinaryIOUtils.writeIntLE(out, boost == null ? 0 : boost.getMinInt());
            }
        }
        List<ItemDefinition.ContainerLootGroup> groups = def == null ? null : def.getContainerLootGroups();
        BinaryIOUtils.writeIntLE(out, groups == null ? 0 : groups.size());
        if (groups != null) for (ItemDefinition.ContainerLootGroup group : groups) {
            List<String> items = group == null ? null : group.getItemKeys();
            BinaryIOUtils.writeIntLE(out, items == null ? 0 : items.size());
            if (items != null) for (String item : items) writeString(out, item);
        }
    }


    private static BodyPart readBodyPart(DataInputStream in) throws IOException {
        String value = readString(in);
        if (value == null || value.isBlank()) return null;
        try { return BodyPart.valueOf(value); } catch (IllegalArgumentException ignored) { return null; }
    }

    private static void writeBodyPart(DataOutputStream out, BodyPart value) throws IOException {
        writeString(out, value == null ? "" : value.name());
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
