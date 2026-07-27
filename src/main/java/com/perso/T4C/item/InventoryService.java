package com.perso.T4C.item;

import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.random.RandomGenerator;

/**
 * Authoritative inventory and equipment rules. The existing string inventory
 * remains the persisted representation; duplicate keys are item stacks.
 */
public final class InventoryService {
    public enum Failure {
        NONE, UNKNOWN_ITEM, TOO_HEAVY, UNIQUE_ITEM, WRONG_SLOT,
        REQUIREMENTS_NOT_MET, ITEM_NOT_OWNED, INVENTORY_EMPTY
    }

    public record Result(boolean success, Failure failure, String itemKey) {
        public static Result success(String itemKey) {
            return new Result(true, Failure.NONE, itemKey);
        }

        public static Result failure(Failure failure, String itemKey) {
            return new Result(false, failure, itemKey);
        }
    }

    private InventoryService() {
    }

    /** Original T4C carrying capacity: STR * 500 / (100 + STR). */
    public static long maximumWeight(Player player) {
        if (player == null) return 0L;
        long strength = Math.max(0, player.getStrength());
        return strength * 500L / (100L + strength);
    }

    public static long currentWeight(Player player) {
        if (player == null) return 0L;
        long total = 0L;
        for (String key : player.getInventory()) {
            ItemDefinition definition = ItemRegistry.findByKey(key);
            if (definition != null) total += Math.max(0L, definition.getWeight());
        }
        Set<BodyPart> counted = new HashSet<>();
        for (Map.Entry<BodyPart, String> entry : player.getEquippedItems().entrySet()) {
            BodyPart slot = entry.getKey();
            ItemDefinition definition = ItemRegistry.findByKey(entry.getValue());
            if (definition == null || isMirroredSecondarySlot(player, slot, entry.getValue(), definition)) continue;
            if (counted.add(slot)) total += Math.max(0L, definition.getWeight());
        }
        return total;
    }

    public static int count(Player player, String itemKey) {
        if (player == null || itemKey == null) return 0;
        int count = 0;
        for (String key : player.getInventory()) if (itemKey.equals(key)) count++;
        for (String key : player.getEquippedItems().values()) if (itemKey.equals(key)) count++;
        return count;
    }

    public static Result add(Player player, String itemKey) {
        return add(player, itemKey, -1);
    }

    public static Result add(Player player, String itemKey, int remainingCharges) {
        ItemDefinition definition = ItemRegistry.findByKey(itemKey);
        if (player == null || definition == null) return Result.failure(Failure.UNKNOWN_ITEM, itemKey);
        if (definition.isUnique() && count(player, itemKey) > 0) return Result.failure(Failure.UNIQUE_ITEM, itemKey);
        if (currentWeight(player) + Math.max(0L, definition.getWeight()) > maximumWeight(player)) {
            return Result.failure(Failure.TOO_HEAVY, itemKey);
        }
        player.getInventory().add(itemKey);
        if (!definition.isUnlimitedUse() && definition.getNbCharges() > 0) {
            int charges = remainingCharges < 0 ? definition.getNbCharges()
                    : Math.max(0, Math.min(definition.getNbCharges(), remainingCharges));
            player.getItemCharges().merge(itemKey, charges, Integer::sum);
        }
        return Result.success(itemKey);
    }

    public static Result remove(Player player, int index, String expectedItemKey) {
        if (player == null || player.getInventory().isEmpty()) {
            return Result.failure(Failure.INVENTORY_EMPTY, expectedItemKey);
        }
        int resolved = index;
        if (resolved < 0 || resolved >= player.getInventory().size()
                || !Objects.equals(expectedItemKey, player.getInventory().get(resolved))) {
            resolved = player.getInventory().indexOf(expectedItemKey);
        }
        if (resolved < 0) return Result.failure(Failure.ITEM_NOT_OWNED, expectedItemKey);
        String removed = player.getInventory().remove(resolved);
        normalizeChargesAfterRemoval(player, removed);
        return Result.success(removed);
    }

    public static Result equip(Player player, BodyPart requestedSlot, String itemKey) {
        Result validation = validateEquip(player, requestedSlot, itemKey);
        if (!validation.success()) return validation;
        ItemDefinition definition = ItemRegistry.findByKey(itemKey);
        // A quiver dropped on the shield zone must still be stored under its own
        // WEAPON2 slot: combat and the puppet renderer both look it up there.
        // The shield it displaces is handled below via the occupant lookup.
        requestedSlot = definition.getBodyPart() == BodyPart.WEAPON2 ? BodyPart.WEAPON2 : requestedSlot;

        String previous = offHandOccupant(player, requestedSlot);
        ItemDefinition previousDefinition = ItemRegistry.findByKey(previous);
        int itemIndex = player.getInventory().indexOf(itemKey);
        player.getInventory().remove(itemIndex);
        if (previous != null) {
            player.getInventory().add(previous);
            // The occupant may sit in the paired off-hand slot rather than the
            // requested one (shield displaced by a quiver, or the reverse).
            if (previousDefinition != null && previousDefinition.getBodyPart() != null) {
                player.getEquippedItems().remove(previousDefinition.getBodyPart(), previous);
            }
            if (previousDefinition != null && previousDefinition.getSecondaryBodyPart() != null) {
                player.getEquippedItems().remove(previousDefinition.getSecondaryBodyPart(), previous);
            }
        }
        player.getEquippedItems().put(requestedSlot, itemKey);
        if (definition.getSecondaryBodyPart() != null) {
            String displaced = player.getEquippedItems().put(definition.getSecondaryBodyPart(), itemKey);
            if (displaced != null && !displaced.equals(previous) && !displaced.equals(itemKey)) {
                player.getInventory().add(displaced);
            }
        }
        return Result.success(itemKey);
    }

    public static Result validateEquip(Player player, BodyPart requestedSlot, String itemKey) {
        ItemDefinition definition = ItemRegistry.findByKey(itemKey);
        if (player == null || definition == null) return Result.failure(Failure.UNKNOWN_ITEM, itemKey);
        if (!player.getInventory().contains(itemKey)) return Result.failure(Failure.ITEM_NOT_OWNED, itemKey);
        if (!slotMatches(requestedSlot, definition.getBodyPart())) return Result.failure(Failure.WRONG_SLOT, itemKey);
        if (!meetsRequirements(player, definition)) return Result.failure(Failure.REQUIREMENTS_NOT_MET, itemKey);

        return Result.success(itemKey);
    }

    public static Result unequip(Player player, BodyPart slot) {
        if (player == null || slot == null) return Result.failure(Failure.ITEM_NOT_OWNED, null);
        String itemKey = player.getEquippedItems().get(slot);
        if (itemKey == null) return Result.failure(Failure.ITEM_NOT_OWNED, null);
        ItemDefinition definition = ItemRegistry.findByKey(itemKey);
        if (definition != null && definition.getSecondaryBodyPart() == slot
                && itemKey.equals(player.getEquippedItems().get(definition.getBodyPart()))) {
            slot = definition.getBodyPart();
        }
        player.getEquippedItems().remove(slot);
        if (definition != null && definition.getSecondaryBodyPart() != null) {
            player.getEquippedItems().remove(definition.getSecondaryBodyPart(), itemKey);
        }
        player.getInventory().add(itemKey);
        return Result.success(itemKey);
    }

    /** Consumes one use. A depleted charged item is destroyed automatically. */
    public static Result useCharge(Player player, String itemKey) {
        ItemDefinition definition = ItemRegistry.findByKey(itemKey);
        if (player == null || definition == null) return Result.failure(Failure.UNKNOWN_ITEM, itemKey);
        if (definition.isUnlimitedUse() || definition.getNbCharges() <= 0) return Result.success(itemKey);
        int remaining = player.getItemCharges().getOrDefault(itemKey,
                definition.getNbCharges() * Math.max(1, count(player, itemKey)));
        if (remaining <= 0) return Result.failure(Failure.ITEM_NOT_OWNED, itemKey);
        remaining--;
        if (remaining % definition.getNbCharges() == 0) {
            destroyOne(player, itemKey);
        }
        if (remaining > 0) player.getItemCharges().put(itemKey, remaining);
        else player.getItemCharges().remove(itemKey);
        return Result.success(itemKey);
    }

    public static Result destroyOne(Player player, String itemKey) {
        if (player == null || itemKey == null) return Result.failure(Failure.ITEM_NOT_OWNED, itemKey);
        int index = player.getInventory().indexOf(itemKey);
        if (index >= 0) return remove(player, index, itemKey);
        BodyPart equippedSlot = null;
        for (Map.Entry<BodyPart, String> entry : player.getEquippedItems().entrySet()) {
            if (itemKey.equals(entry.getValue())) { equippedSlot = entry.getKey(); break; }
        }
        if (equippedSlot != null) {
            unequipWithoutTransfer(player, equippedSlot, itemKey);
            normalizeChargesAfterRemoval(player, itemKey);
            return Result.success(itemKey);
        }
        return Result.failure(Failure.ITEM_NOT_OWNED, itemKey);
    }

    /** Server-style steal contest; the transfer still obeys unique and weight rules. */
    public static Result steal(Player thief, Player victim, String itemKey, RandomGenerator random) {
        if (thief == null || victim == null || random == null || !victim.getInventory().contains(itemKey)) {
            return Result.failure(Failure.ITEM_NOT_OWNED, itemKey);
        }
        int skill = thief.getSkillLevel("rob");
        int attackRoll = random.nextInt(Math.max(1, skill + thief.getDexterity())) + 1;
        int defenseRoll = random.nextInt(Math.max(1, victim.getDexterity() * 2)) + 1;
        if (attackRoll <= defenseRoll) return Result.failure(Failure.REQUIREMENTS_NOT_MET, itemKey);
        Result capacity = canReceive(thief, itemKey);
        if (!capacity.success()) return capacity;
        ItemDefinition definition = ItemRegistry.findByKey(itemKey);
        int transferredCharges = 0;
        int victimChargesBefore = 0;
        int thiefChargesBefore = 0;
        if (definition != null && definition.getNbCharges() > 0 && !definition.isUnlimitedUse()) {
            victimChargesBefore = victim.getItemCharges().getOrDefault(itemKey,
                    definition.getNbCharges() * count(victim, itemKey));
            thiefChargesBefore = thief.getItemCharges().getOrDefault(itemKey,
                    definition.getNbCharges() * count(thief, itemKey));
            transferredCharges = victimChargesBefore % definition.getNbCharges();
            if (transferredCharges == 0) transferredCharges = definition.getNbCharges();
        }
        remove(victim, victim.getInventory().indexOf(itemKey), itemKey);
        Result added = add(thief, itemKey);
        if (transferredCharges > 0) {
            int remainingVictimCharges = Math.max(0, victimChargesBefore - transferredCharges);
            if (remainingVictimCharges > 0) victim.getItemCharges().put(itemKey, remainingVictimCharges);
            else victim.getItemCharges().remove(itemKey);
            thief.getItemCharges().put(itemKey, thiefChargesBefore + transferredCharges);
        }
        return added;
    }

    public static double equippedArmor(Player player) {
        if (player == null) return 0d;
        double armor = 0d;
        Set<String> mirrored = new HashSet<>();
        for (Map.Entry<BodyPart, String> entry : player.getEquippedItems().entrySet()) {
            ItemDefinition definition = ItemRegistry.findByKey(entry.getValue());
            if (definition == null || isMirroredSecondarySlot(player, entry.getKey(), entry.getValue(), definition)) continue;
            armor += Math.max(0d, definition.getArmorClass());
            mirrored.add(entry.getValue());
        }
        return armor;
    }

    public static int equippedDodgePenalty(Player player) {
        if (player == null) return 0;
        long penalty = 0;
        for (Map.Entry<BodyPart, String> entry : player.getEquippedItems().entrySet()) {
            ItemDefinition definition = ItemRegistry.findByKey(entry.getValue());
            if (definition == null || isMirroredSecondarySlot(player, entry.getKey(), entry.getValue(), definition)) continue;
            penalty += Math.max(0L, definition.getDodgeLost());
        }
        return (int) Math.min(Integer.MAX_VALUE, penalty);
    }

    public static boolean hasWeapon(Player player) {
        return player != null && (player.getEquippedItems().containsKey(BodyPart.WEAPON)
                || player.getEquippedItems().containsKey(BodyPart.WEAPON2));
    }

    public static int chargesForNextInstance(Player player, String itemKey) {
        ItemDefinition definition = ItemRegistry.findByKey(itemKey);
        if (player == null || definition == null || definition.isUnlimitedUse() || definition.getNbCharges() <= 0) {
            return -1;
        }
        int total = player.getItemCharges().getOrDefault(itemKey, definition.getNbCharges() * count(player, itemKey));
        int charges = total % definition.getNbCharges();
        return charges == 0 ? definition.getNbCharges() : charges;
    }

    public static void removeExtractedCharges(Player player, String itemKey, int extractedCharges) {
        if (player == null || extractedCharges < 0) return;
        int remaining = Math.max(0, player.getItemCharges().getOrDefault(itemKey, 0) - extractedCharges);
        if (remaining > 0) player.getItemCharges().put(itemKey, remaining);
        else player.getItemCharges().remove(itemKey);
    }

    private static Result canReceive(Player player, String itemKey) {
        ItemDefinition definition = ItemRegistry.findByKey(itemKey);
        if (definition == null) return Result.failure(Failure.UNKNOWN_ITEM, itemKey);
        if (definition.isUnique() && count(player, itemKey) > 0) return Result.failure(Failure.UNIQUE_ITEM, itemKey);
        if (currentWeight(player) + Math.max(0L, definition.getWeight()) > maximumWeight(player)) {
            return Result.failure(Failure.TOO_HEAVY, itemKey);
        }
        return Result.success(itemKey);
    }

    private static boolean meetsRequirements(Player player, ItemDefinition definition) {
        int attack = player.getSkillLevel("attack");
        if (attack <= 0) attack = player.getLevel() + player.getDexterity();
        return player.getEndurance() >= definition.getMinEnd()
                && player.getStrength() >= definition.getReqStr()
                && player.getDexterity() >= definition.getReqAgi()
                && player.getIntelligence() >= definition.getMinInt()
                && player.getWisdom() >= definition.getMinWis()
                && attack >= definition.getReqAttack();
    }

    /**
     * Returns the item currently blocking the given slot.
     *
     * <p>SHIELD and WEAPON2 are the same physical position in GoN
     * ({@code QUIVER_POS == weapon_left}), so equipping one must displace the
     * other. Any other slot only blocks itself.</p>
     */
    private static String offHandOccupant(Player player, BodyPart slot) {
        String occupant = player.getEquippedItems().get(slot);
        if (occupant != null) return occupant;
        if (slot == BodyPart.WEAPON2) return player.getEquippedItems().get(BodyPart.SHIELD);
        if (slot == BodyPart.SHIELD) return player.getEquippedItems().get(BodyPart.WEAPON2);
        return null;
    }

    private static boolean slotMatches(BodyPart requested, BodyPart defined) {
        if (requested == null || defined == null) return false;
        if (requested == defined) return true;
        if ((requested == BodyPart.RING1 || requested == BodyPart.RING2)
                && (defined == BodyPart.RING1 || defined == BodyPart.RING2)) {
            return true;
        }
        // GoN maps the quiver onto the off-hand: Character.cpp defines
        // QUIVER_POS as weapon_left, the same physical position as the shield.
        // A quiver dropped on the shield zone therefore equips as WEAPON2.
        return requested == BodyPart.SHIELD && defined == BodyPart.WEAPON2;
    }

    private static boolean isMirroredSecondarySlot(Player player, BodyPart slot, String key, ItemDefinition definition) {
        return definition.getSecondaryBodyPart() == slot
                && key.equals(player.getEquippedItems().get(definition.getBodyPart()));
    }

    private static void normalizeChargesAfterRemoval(Player player, String itemKey) {
        ItemDefinition definition = ItemRegistry.findByKey(itemKey);
        if (definition == null || definition.getNbCharges() <= 0 || definition.isUnlimitedUse()) return;
        int maximum = count(player, itemKey) * definition.getNbCharges();
        int remaining = Math.min(maximum, player.getItemCharges().getOrDefault(itemKey, maximum));
        if (remaining > 0) player.getItemCharges().put(itemKey, remaining);
        else player.getItemCharges().remove(itemKey);
    }

    private static void unequipWithoutTransfer(Player player, BodyPart slot, String itemKey) {
        ItemDefinition definition = ItemRegistry.findByKey(itemKey);
        if (definition != null && definition.getSecondaryBodyPart() == slot) {
            player.getEquippedItems().remove(definition.getBodyPart(), itemKey);
        }
        player.getEquippedItems().remove(slot, itemKey);
        if (definition != null && definition.getSecondaryBodyPart() != null) {
            player.getEquippedItems().remove(definition.getSecondaryBodyPart(), itemKey);
        }
    }
}
