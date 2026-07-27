package com.perso.T4C.item;

import lombok.Getter;

import com.perso.T4C.player.BodyPart;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Locale;

/**
 * Data-driven definition of an item type. Persisted in {@code assets/items/items.bin}
 * via {@link com.perso.T4C.helper.ItemDefBinaryIO}.
 */
@Getter
public class ItemDefinition {
    // --- v1-v3 fields ---
    private final String key;
    private final String name;
    private final BodyPart bodyPart;
    private final String appearanceEquippedPrimary;
    private final BodyPart secondaryBodyPart;
    private final String appearanceEquippedSecondary;
    private final String appearanceInventory;
    private final long price;
    private final long weight;
    private final double armorClass;
    private final long dodgeLost;
    private final long minEnd;
    private final long reqAttack;
    private final long reqStr;
    private final long reqAgi;
    private final long minInt;
    private final long minWis;
    private final double attackSpeed;
    private final boolean unique;
    private final boolean isBow;
    private final boolean unlimitedUse;

    // --- v4 fields (T4C original data) ---
    private final int numId;
    private final int structure;
    private final int appearanceId;
    private final String dmgFormula;
    private final String atkDelay;
    private final int radiance;
    private final int nbCharges;
    private final boolean canSummon;
    private final String lockName;
    private final int lockDiff;
    private final String signText;
    private final int containerGold;
    private final int globalRespawn;
    private final int localRespawn;
    /** Spells associated with this item (charges, scrolls…). Never null. */
    private final List<ItemSpell> spells;
    /** Attribute/skill boosts activated while this item is equipped. */
    private final List<ItemBoost> boosts;
    /** GoN container tables; one item is drawn from one randomly selected group. */
    private final List<ContainerLootGroup> containerLootGroups;

    /** Full constructor (v4). */
    public ItemDefinition(
            String key, String name,
            BodyPart bodyPart, String appearanceEquippedPrimary,
            BodyPart secondaryBodyPart, String appearanceEquippedSecondary,
            String appearanceInventory,
            long price, long weight, double armorClass,
            long dodgeLost, long minEnd,
            long reqAttack, long reqStr, long reqAgi,
            long minInt, long minWis,
            double attackSpeed,
            boolean unique, boolean isBow, boolean unlimitedUse,
            int numId, int structure, int appearanceId,
            String dmgFormula, String atkDelay,
            int radiance, int nbCharges, boolean canSummon,
            String lockName, int lockDiff,
            String signText, int containerGold,
            int globalRespawn, int localRespawn,
            List<ItemSpell> spells) {
        this(key, name, bodyPart, appearanceEquippedPrimary, secondaryBodyPart,
                appearanceEquippedSecondary, appearanceInventory, price, weight, armorClass,
                dodgeLost, minEnd, reqAttack, reqStr, reqAgi, minInt, minWis, attackSpeed,
                unique, isBow, unlimitedUse, numId, structure, appearanceId, dmgFormula,
                atkDelay, radiance, nbCharges, canSummon, lockName, lockDiff, signText,
                containerGold, globalRespawn, localRespawn, spells, Collections.emptyList());
    }

    public ItemDefinition(
            String key, String name,
            BodyPart bodyPart, String appearanceEquippedPrimary,
            BodyPart secondaryBodyPart, String appearanceEquippedSecondary,
            String appearanceInventory,
            long price, long weight, double armorClass,
            long dodgeLost, long minEnd,
            long reqAttack, long reqStr, long reqAgi,
            long minInt, long minWis, double attackSpeed,
            boolean unique, boolean isBow, boolean unlimitedUse,
            int numId, int structure, int appearanceId,
            String dmgFormula, String atkDelay, int radiance, int nbCharges,
            boolean canSummon, String lockName, int lockDiff, String signText,
            int containerGold, int globalRespawn, int localRespawn,
            List<ItemSpell> spells, List<ItemBoost> boosts) {
        this(key, name, bodyPart, appearanceEquippedPrimary, secondaryBodyPart,
                appearanceEquippedSecondary, appearanceInventory, price, weight, armorClass,
                dodgeLost, minEnd, reqAttack, reqStr, reqAgi, minInt, minWis, attackSpeed,
                unique, isBow, unlimitedUse, numId, structure, appearanceId, dmgFormula, atkDelay,
                radiance, nbCharges, canSummon, lockName, lockDiff, signText, containerGold,
                globalRespawn, localRespawn, spells, boosts, Collections.emptyList());
    }

    /** Full constructor including the GoN container loot tables. */
    public ItemDefinition(
            String key, String name,
            BodyPart bodyPart, String appearanceEquippedPrimary,
            BodyPart secondaryBodyPart, String appearanceEquippedSecondary,
            String appearanceInventory,
            long price, long weight, double armorClass,
            long dodgeLost, long minEnd,
            long reqAttack, long reqStr, long reqAgi,
            long minInt, long minWis, double attackSpeed,
            boolean unique, boolean isBow, boolean unlimitedUse,
            int numId, int structure, int appearanceId,
            String dmgFormula, String atkDelay, int radiance, int nbCharges,
            boolean canSummon, String lockName, int lockDiff, String signText,
            int containerGold, int globalRespawn, int localRespawn,
            List<ItemSpell> spells, List<ItemBoost> boosts, List<ContainerLootGroup> containerLootGroups) {
        this.key = normalizeKey(key);
        this.name = name;
        this.bodyPart = bodyPart;
        this.appearanceEquippedPrimary = appearanceEquippedPrimary;
        this.secondaryBodyPart = secondaryBodyPart;
        this.appearanceEquippedSecondary = appearanceEquippedSecondary;
        this.appearanceInventory = appearanceInventory;
        this.price = price;
        this.weight = weight;
        this.armorClass = armorClass;
        this.dodgeLost = dodgeLost;
        this.minEnd = minEnd;
        this.reqAttack = reqAttack;
        this.reqStr = reqStr;
        this.reqAgi = reqAgi;
        this.minInt = minInt;
        this.minWis = minWis;
        this.attackSpeed = attackSpeed;
        this.unique = unique;
        this.isBow = isBow;
        this.unlimitedUse = unlimitedUse;
        this.numId = numId;
        this.structure = structure;
        this.appearanceId = appearanceId;
        this.dmgFormula = dmgFormula;
        this.atkDelay = atkDelay;
        this.radiance = radiance;
        this.nbCharges = nbCharges;
        this.canSummon = canSummon;
        this.lockName = lockName;
        this.lockDiff = lockDiff;
        this.signText = signText;
        this.containerGold = containerGold;
        this.globalRespawn = globalRespawn;
        this.localRespawn = localRespawn;
        this.spells = spells != null ? spells : Collections.emptyList();
        this.boosts = boosts != null ? List.copyOf(boosts) : Collections.emptyList();
        this.containerLootGroups = containerLootGroups != null ? List.copyOf(containerLootGroups) : Collections.emptyList();
    }

    /** Canonical item identity: namespace + lowercase alphanumeric name. */
    public static String normalizeKey(String value) {
        if (value == null || value.isBlank()) return value;
        String raw = value.startsWith("item.") ? value.substring(5) : value;
        String normalized = raw.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
        return "item." + normalized;
    }

    /** Legacy v1-v3 constructor (v4 fields default to 0/null/empty). */
    public ItemDefinition(
            String key, String name,
            BodyPart bodyPart, String appearanceEquippedPrimary,
            BodyPart secondaryBodyPart, String appearanceEquippedSecondary,
            String appearanceInventory,
            long price, long weight, double armorClass,
            long dodgeLost, long minEnd,
            long reqAttack, long reqStr, long reqAgi,
            long minInt, long minWis,
            double attackSpeed,
            boolean unique, boolean isBow, boolean unlimitedUse) {
        this(key, name, bodyPart, appearanceEquippedPrimary,
                secondaryBodyPart, appearanceEquippedSecondary, appearanceInventory,
                price, weight, armorClass, dodgeLost, minEnd,
                reqAttack, reqStr, reqAgi, minInt, minWis,
                attackSpeed, unique, isBow, unlimitedUse,
                0, 0, 0, null, null, 0, 0, false,
                null, 0, null, 0, 0, 0, Collections.emptyList());
    }

    /** Convenience constructor without key and attackSpeed. */
    public ItemDefinition(
            String name,
            BodyPart bodyPart, String appearanceEquippedPrimary,
            BodyPart secondaryBodyPart, String appearanceEquippedSecondary,
            String appearanceInventory,
            long price, long weight, double armorClass,
            long dodgeLost, long minEnd,
            long reqAttack, long reqStr, long reqAgi,
            long minInt, long minWis,
            boolean unique, boolean isBow, boolean unlimitedUse) {
        this(null, name, bodyPart, appearanceEquippedPrimary,
                secondaryBodyPart, appearanceEquippedSecondary, appearanceInventory,
                price, weight, armorClass, dodgeLost, minEnd,
                reqAttack, reqStr, reqAgi, minInt, minWis,
                1.0d, unique, isBow, unlimitedUse);
    }

    /** Convenience constructor without secondary body part. */
    public ItemDefinition(
            String name,
            BodyPart bodyPart, String appearanceEquippedPrimary,
            String appearanceInventory,
            long price, long weight, double armorClass,
            long dodgeLost, long minEnd,
            long reqAttack, long reqStr, long reqAgi,
            long minInt, long minWis,
            boolean unique, boolean isBow, boolean unlimitedUse) {
        this(name, bodyPart, appearanceEquippedPrimary,
                null, null, appearanceInventory,
                price, weight, armorClass, dodgeLost, minEnd,
                reqAttack, reqStr, reqAgi, minInt, minWis,
                unique, isBow, unlimitedUse);
    }

    public String getAppearanceEquippedFor(BodyPart part) {
        if (part != null && part == secondaryBodyPart
                && appearanceEquippedSecondary != null
                && !appearanceEquippedSecondary.isEmpty()) {
            return appearanceEquippedSecondary;
        }
        return appearanceEquippedPrimary;
    }

    public static ItemDefinition get(String name) {
        return ItemRegistry.findByKey(name);
    }

    public static Map<String, ItemDefinition> all() {
        return ItemRegistry.allByKey();
    }

    /** A spell associated with this item (scroll, charged item…). */
    @Getter
    public static final class ItemSpell {
        private final int spellId;
        private final int level;
        private final int chance;

        public ItemSpell(int spellId, int level, int chance) {
            this.spellId = spellId;
            this.level = level;
            this.chance = chance;
        }
    }

    @Getter
    public static final class ContainerLootGroup {
        private final List<String> itemKeys;

        public ContainerLootGroup(List<String> itemKeys) {
            this.itemKeys = itemKeys == null ? Collections.emptyList() : List.copyOf(itemKeys);
        }
    }

    /** C++ OBJECT_BOOST: id, affected stat, formula and per-boost INT/WIS gates. */
    @Getter
    public static final class ItemBoost {
        private final int boostId;
        private final int statId;
        private final String expression;
        private final int minWis;
        private final int minInt;

        public ItemBoost(int boostId, int statId, String expression, int minWis, int minInt) {
            this.boostId = boostId;
            this.statId = statId;
            this.expression = expression == null ? "0" : expression;
            this.minWis = Math.max(0, minWis);
            this.minInt = Math.max(0, minInt);
        }
    }
}
