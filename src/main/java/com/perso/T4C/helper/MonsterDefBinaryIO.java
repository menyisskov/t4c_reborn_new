package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.MonsterDef;
import com.perso.T4C.i18n.I18n;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Binary serialization for {@link MonsterDef} lists. Mirrors {@link SpellBinaryIO}.
 *
 * <p>Version history:
 * <ul>
 *   <li>v1 — original fields (health, mana, xp, damage, respawn, sounds, gold, loot, flags)</li>
 *   <li>v2 — adds T4C stats (str/end/agi/intel/will/wis/luck, resists, level, dodge, ac,
 *             appearance, equipment, aggro, clan, speed, canAttack, attacks[])</li>
 *   <li>v3 — adds tameable, tameMaxLevel</li>
 *   <li>v4 — adds spawnAliases[]</li>
 *   <li>v5 — adds sourceEvents map</li>
 * </ul>
 */
public final class MonsterDefBinaryIO {
    private static final byte[] MAGIC = "T4CMON".getBytes(StandardCharsets.US_ASCII);
    private static final short VERSION = 5;
    private static final int MAX_STRING_BYTES = 16384;
    private static final int RESISTS_COUNT = 12;

    private MonsterDefBinaryIO() {
    }

    public static List<MonsterDef> read(File file) throws IOException, GameException {
        return BinaryCatalogueIO.read(file, MAGIC, "monster definition",
                version -> {
                    if (version < 1 || version > VERSION) {
                        throw new GameException("Unsupported monster definition version: " + version);
                    }
                },
                MonsterDefBinaryIO::readDef);
    }

    public static void write(File file, List<MonsterDef> defs) throws IOException {
        BinaryCatalogueIO.write(file, MAGIC, VERSION, defs, MonsterDefBinaryIO::writeDef);
    }

    private static MonsterDef readDef(DataInputStream in, short version) throws IOException, GameException {
        // v1 fields
        String name = readString(in);
        String displayName = readString(in);
        int health = BinaryIOUtils.readIntLE(in);
        int mana = BinaryIOUtils.readIntLE(in);
        int xpPerHit = BinaryIOUtils.readIntLE(in);
        int xpOnDeath = BinaryIOUtils.readIntLE(in);
        int hitDamageMin = BinaryIOUtils.readIntLE(in);
        int hitDamageMax = BinaryIOUtils.readIntLE(in);
        long respawnTime = BinaryIOUtils.readLongLE(in);
        String walkPattern = readString(in);
        String attackPattern = readString(in);
        String deathPattern = readString(in);
        String soundAttack = readString(in);
        String soundDeath = readString(in);
        String soundHit = readString(in);
        int goldMin = BinaryIOUtils.readIntLE(in);
        int goldMax = BinaryIOUtils.readIntLE(in);
        int lootCount = BinaryIOUtils.readIntLE(in);
        if (lootCount < 0) {
            throw new GameException("Invalid loot drop count: " + lootCount);
        }
        List<MonsterDef.LootDrop> loot = new ArrayList<>(lootCount);
        for (int i = 0; i < lootCount; i++) {
            String item = readString(in);
            float chance = Float.intBitsToFloat(BinaryIOUtils.readIntLE(in));
            loot.add(new MonsterDef.LootDrop(item, chance));
        }
        boolean v1Aggressive = in.readBoolean();
        boolean animateWhileStationary = in.readBoolean();
        float stationaryAnimationPauseSeconds = Float.intBitsToFloat(BinaryIOUtils.readIntLE(in));

        // v2 fields — defaults used when reading a v1 file
        int str = 0, end = 0, agi = 0, intel = 0, will = 0, wis = 0, luck = 0;
        int[] resists = new int[RESISTS_COUNT];
        int level = 1, dodge = 0, acMin = 0, acMax = 0, appearance = 0;
        int itemBody = 0, itemFeet = 0, itemHands = 0, itemHead = 0;
        int itemLegs = 0, itemWeapon = 0, itemShield = 0, itemBack = 0;
        int aggro = v1Aggressive ? 50 : 0;
        int clan = 0, speed = 0;
        boolean canAttack = true;
        List<MonsterDef.Attack> attacks = new ArrayList<>();

        if (version >= 2) {
            str   = BinaryIOUtils.readIntLE(in);
            end   = BinaryIOUtils.readIntLE(in);
            agi   = BinaryIOUtils.readIntLE(in);
            intel = BinaryIOUtils.readIntLE(in);
            will  = BinaryIOUtils.readIntLE(in);
            wis   = BinaryIOUtils.readIntLE(in);
            luck  = BinaryIOUtils.readIntLE(in);
            int resistCount = BinaryIOUtils.readIntLE(in);
            resists = new int[resistCount];
            for (int i = 0; i < resistCount; i++) {
                resists[i] = BinaryIOUtils.readIntLE(in);
            }
            level      = BinaryIOUtils.readIntLE(in);
            dodge      = BinaryIOUtils.readIntLE(in);
            acMin      = BinaryIOUtils.readIntLE(in);
            acMax      = BinaryIOUtils.readIntLE(in);
            appearance = BinaryIOUtils.readIntLE(in);
            itemBody   = BinaryIOUtils.readIntLE(in);
            itemFeet   = BinaryIOUtils.readIntLE(in);
            itemHands  = BinaryIOUtils.readIntLE(in);
            itemHead   = BinaryIOUtils.readIntLE(in);
            itemLegs   = BinaryIOUtils.readIntLE(in);
            itemWeapon = BinaryIOUtils.readIntLE(in);
            itemShield = BinaryIOUtils.readIntLE(in);
            itemBack   = BinaryIOUtils.readIntLE(in);
            aggro      = BinaryIOUtils.readIntLE(in);
            clan       = BinaryIOUtils.readIntLE(in);
            speed      = BinaryIOUtils.readIntLE(in);
            canAttack  = in.readBoolean();
            int attackCount = BinaryIOUtils.readIntLE(in);
            if (attackCount < 0) {
                throw new GameException("Invalid attack count: " + attackCount);
            }
            for (int i = 0; i < attackCount; i++) {
                String atkName = readString(in);
                int v1 = BinaryIOUtils.readIntLE(in);
                int v2 = BinaryIOUtils.readIntLE(in);
                int v3 = BinaryIOUtils.readIntLE(in);
                int v4 = BinaryIOUtils.readIntLE(in);
                int v5 = BinaryIOUtils.readIntLE(in);
                attacks.add(new MonsterDef.Attack(atkName, v1, v2, v3, v4, v5));
            }
        }

        boolean tameable = false;
        int tameMaxLevel = 0;
        if (version >= 3) {
            tameable = in.readBoolean();
            tameMaxLevel = BinaryIOUtils.readIntLE(in);
        }

        List<String> spawnAliases = new ArrayList<>();
        if (version >= 4) {
            int aliasCount = BinaryIOUtils.readIntLE(in);
            if (aliasCount < 0) {
                throw new GameException("Invalid spawn alias count: " + aliasCount);
            }
            for (int i = 0; i < aliasCount; i++) {
                spawnAliases.add(readString(in));
            }
        }
        java.util.Map<String, String> sourceEvents = new java.util.LinkedHashMap<>();
        if (version >= 5) {
            int eventCount = BinaryIOUtils.readIntLE(in);
            if (eventCount < 0) throw new GameException("Invalid monster event count: " + eventCount);
            for (int i = 0; i < eventCount; i++) sourceEvents.put(readString(in), readString(in));
        }

        return new MonsterDef(name, displayName, health, mana, xpPerHit, xpOnDeath,
                hitDamageMin, hitDamageMax, respawnTime,
                walkPattern, emptyToNull(attackPattern), emptyToNull(deathPattern),
                emptyToNull(soundAttack), emptyToNull(soundDeath), emptyToNull(soundHit),
                goldMin, goldMax, loot, animateWhileStationary, stationaryAnimationPauseSeconds,
                str, end, agi, intel, will, wis, luck, resists,
                level, dodge, acMin, acMax, appearance,
                itemBody, itemFeet, itemHands, itemHead, itemLegs, itemWeapon, itemShield, itemBack,
                aggro, clan, speed, canAttack, attacks, tameable, tameMaxLevel, spawnAliases, sourceEvents);
    }

    private static void writeDef(DataOutputStream out, MonsterDef def) throws IOException {
        writeString(out, def == null ? "" : def.getName());
        writeString(out, def == null ? "" : I18n.placeholderFor("monster", def.getName(), def.getDisplayName()));
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getHealth());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getMana());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getXpPerHit());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getXpOnDeath());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getHitDamageMin());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getHitDamageMax());
        BinaryIOUtils.writeLongLE(out, def == null ? 0L : def.getRespawnTime());
        writeString(out, def == null ? "" : def.getWalkPattern());
        writeString(out, def == null ? "" : def.getAttackPattern());
        writeString(out, def == null ? "" : def.getDeathPattern());
        writeString(out, def == null ? "" : def.getSoundAttack());
        writeString(out, def == null ? "" : def.getSoundDeath());
        writeString(out, def == null ? "" : def.getSoundHit());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getGoldMin());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getGoldMax());
        List<MonsterDef.LootDrop> loot = def == null ? null : def.getLoot();
        BinaryIOUtils.writeIntLE(out, loot == null ? 0 : loot.size());
        if (loot != null) {
            for (MonsterDef.LootDrop drop : loot) {
                writeString(out, drop == null ? "" : drop.getItem());
                BinaryIOUtils.writeIntLE(out, Float.floatToIntBits(drop == null ? 0f : drop.getChance()));
            }
        }
        // v1 aggressive boolean: derive from aggro for backward compat
        out.writeBoolean(def == null || def.isDefaultAggressive());
        out.writeBoolean(def != null && def.isAnimateWhileStationary());
        BinaryIOUtils.writeIntLE(out, Float.floatToIntBits(def == null ? 0f : def.getStationaryAnimationPauseSeconds()));

        // v2 fields
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getStr());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getEnd());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getAgi());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getIntel());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getWill());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getWis());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getLuck());
        int[] resists = def == null ? null : def.getResists();
        BinaryIOUtils.writeIntLE(out, resists == null ? 0 : resists.length);
        if (resists != null) {
            for (int r : resists) {
                BinaryIOUtils.writeIntLE(out, r);
            }
        }
        BinaryIOUtils.writeIntLE(out, def == null ? 1 : def.getLevel());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getDodge());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getAcMin());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getAcMax());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getAppearance());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getItemBody());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getItemFeet());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getItemHands());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getItemHead());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getItemLegs());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getItemWeapon());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getItemShield());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getItemBack());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getAggro());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getClan());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getSpeed());
        out.writeBoolean(def == null || def.isCanAttack());
        List<MonsterDef.Attack> attacks = def == null ? null : def.getAttacks();
        BinaryIOUtils.writeIntLE(out, attacks == null ? 0 : attacks.size());
        if (attacks != null) {
            for (MonsterDef.Attack atk : attacks) {
                writeString(out, atk == null ? "" : atk.getName());
                BinaryIOUtils.writeIntLE(out, atk == null ? 0 : atk.getValue1());
                BinaryIOUtils.writeIntLE(out, atk == null ? 0 : atk.getValue2());
                BinaryIOUtils.writeIntLE(out, atk == null ? 0 : atk.getValue3());
                BinaryIOUtils.writeIntLE(out, atk == null ? 0 : atk.getValue4());
                BinaryIOUtils.writeIntLE(out, atk == null ? 0 : atk.getValue5());
            }
        }
        out.writeBoolean(def != null && def.isTameable());
        BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.getTameMaxLevel());

        List<String> spawnAliases = def == null ? null : def.getSpawnAliases();
        BinaryIOUtils.writeIntLE(out, spawnAliases == null ? 0 : spawnAliases.size());
        if (spawnAliases != null) {
            for (String alias : spawnAliases) {
                writeString(out, alias == null ? "" : alias);
            }
        }
        java.util.Map<String, String> sourceEvents = def == null ? null : def.getSourceEvents();
        BinaryIOUtils.writeIntLE(out, sourceEvents == null ? 0 : sourceEvents.size());
        if (sourceEvents != null) for (var event : sourceEvents.entrySet()) {
            writeString(out, event.getKey());
            writeString(out, event.getValue());
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
