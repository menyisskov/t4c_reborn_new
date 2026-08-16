package com.perso.T4C.item;

import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Rules and persistence-friendly accessors for per-instance equipment durability. */
public final class ItemDurabilityService {
    public static final double MAX = 100d;
    public static final double COMBAT_WEAR = 0.20d;
    public static final double DEATH_WEAR = 15d;
    private static final Set<BodyPart> ARMOR_SLOTS = Set.of(
            BodyPart.HEAD, BodyPart.BODY, BodyPart.LEGS, BodyPart.FEET,
            BodyPart.LEFT_HAND, BodyPart.RIGHT_HAND, BodyPart.SHIELD);

    private ItemDurabilityService() {}

    public static boolean isRepairable(ItemDefinition item) {
        if (item == null || item.getBodyPart() == null) return false;
        return item.getBodyPart() == BodyPart.WEAPON || item.getBodyPart() == BodyPart.WEAPON2
                || ARMOR_SLOTS.contains(item.getBodyPart());
    }

    public static void synchronize(Player player) {
        if (player == null) return;
        List<Double> durability = player.getInventoryDurability();
        while (durability.size() < player.getInventory().size()) durability.add(MAX);
        while (durability.size() > player.getInventory().size()) durability.remove(durability.size() - 1);
        for (int i = 0; i < durability.size(); i++) {
            Double value = durability.get(i);
            durability.set(i, clamp(value == null ? MAX : value));
        }
    }

    public static double inventory(Player player, int index) {
        synchronize(player);
        return player == null || index < 0 || index >= player.getInventoryDurability().size()
                ? MAX : player.getInventoryDurability().get(index);
    }

    public static double equipped(Player player, BodyPart slot) {
        if (player == null || slot == null) return MAX;
        return clamp(player.getEquippedDurability().getOrDefault(primarySlot(player, slot), MAX));
    }

    public static boolean isBroken(Player player, BodyPart slot) {
        String key = player == null ? null : player.getEquippedItems().get(slot);
        return isRepairable(ItemRegistry.findByKey(key)) && equipped(player, slot) <= 0d;
    }

    public static double damageEquipped(Player player, BodyPart slot, double amount) {
        if (player == null || slot == null || amount <= 0) return equipped(player, slot);
        BodyPart primary = primarySlot(player, slot);
        ItemDefinition item = ItemRegistry.findByKey(player.getEquippedItems().get(primary));
        if (!isRepairable(item)) return MAX;
        double value = clamp(equipped(player, primary) - amount);
        player.getEquippedDurability().put(primary, value);
        return value;
    }

    /** Wears one non-broken defensive item in deterministic slot order. */
    public static BodyPart wearArmorOnPhysicalHit(Player player) {
        if (player == null) return null;
        for (BodyPart slot : ARMOR_SLOTS) {
            String key = player.getEquippedItems().get(slot);
            ItemDefinition item = ItemRegistry.findByKey(key);
            if (item != null && isRepairable(item) && equipped(player, slot) > 0) {
                damageEquipped(player, slot, COMBAT_WEAR);
                return slot;
            }
        }
        return null;
    }

    public static long repairCost(ItemDefinition item, double durability) {
        if (!isRepairable(item) || durability >= MAX) return 0L;
        return Math.max(1L, (long) Math.ceil(Math.max(0L, item.getPrice()) * (MAX - clamp(durability)) / 100d * .20d));
    }

    public static long repairAllCost(Player player) {
        if (player == null) return 0L;
        synchronize(player);
        long total = 0L;
        for (int i = 0; i < player.getInventory().size(); i++)
            total += repairCost(ItemRegistry.findByKey(player.getInventory().get(i)), inventory(player, i));
        Set<BodyPart> seen = new LinkedHashSet<>();
        for (BodyPart slot : player.getEquippedItems().keySet()) {
            BodyPart primary = primarySlot(player, slot);
            if (seen.add(primary)) total += repairCost(ItemRegistry.findByKey(player.getEquippedItems().get(primary)), equipped(player, primary));
        }
        return total;
    }

    public static boolean repairAll(Player player) {
        long cost = repairAllCost(player);
        if (player == null || player.getGold() < cost) return false;
        player.setGold((int) Math.max(0L, (long) player.getGold() - cost));
        synchronize(player);
        for (int i = 0; i < player.getInventory().size(); i++)
            if (isRepairable(ItemRegistry.findByKey(player.getInventory().get(i)))) player.getInventoryDurability().set(i, MAX);
        for (BodyPart slot : new ArrayList<>(player.getEquippedDurability().keySet())) player.getEquippedDurability().put(slot, MAX);
        return true;
    }

    public static boolean repairInventory(Player player, int index) {
        if (player == null) return false;
        synchronize(player);
        if (index < 0 || index >= player.getInventory().size()) return false;
        ItemDefinition item = ItemRegistry.findByKey(player.getInventory().get(index));
        long cost = repairCost(item, inventory(player, index));
        if (cost <= 0 || player.getGold() < cost) return false;
        player.setGold((int) ((long) player.getGold() - cost));
        player.getInventoryDurability().set(index, MAX);
        return true;
    }

    public static boolean repairEquipped(Player player, BodyPart slot) {
        if (player == null || slot == null) return false;
        BodyPart primary = primarySlot(player, slot);
        ItemDefinition item = ItemRegistry.findByKey(player.getEquippedItems().get(primary));
        long cost = repairCost(item, equipped(player, primary));
        if (cost <= 0 || player.getGold() < cost) return false;
        player.setGold((int) ((long) player.getGold() - cost));
        player.getEquippedDurability().put(primary, MAX);
        return true;
    }

    /** Applies the fixed death wear once to every distinct equipped item instance. */
    public static void wearAllEquippedOnDeath(Player player) {
        if (player == null) return;
        Set<BodyPart> seen = new LinkedHashSet<>();
        for (BodyPart slot : new ArrayList<>(player.getEquippedItems().keySet())) {
            BodyPart primary = primarySlot(player, slot);
            if (seen.add(primary)) damageEquipped(player, primary, DEATH_WEAR);
        }
    }

    public static String format(double durability) {
        double value = clamp(durability);
        if (Math.abs(value - Math.rint(value)) < 0.000_001d) return Long.toString(Math.round(value));
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    public static BodyPart primarySlot(Player player, BodyPart slot) {
        if (player == null || slot == null) return slot;
        String key = player.getEquippedItems().get(slot);
        ItemDefinition item = ItemRegistry.findByKey(key);
        if (item != null && item.getSecondaryBodyPart() == slot
                && key.equals(player.getEquippedItems().get(item.getBodyPart()))) return item.getBodyPart();
        return slot;
    }

    private static double clamp(double value) { return Math.max(0d, Math.min(MAX, value)); }
}
