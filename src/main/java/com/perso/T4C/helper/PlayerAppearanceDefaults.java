package com.perso.T4C.helper;

import com.perso.T4C.config.Paths;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;

import java.util.Map;

/**
 * Applies the default puppet appearance to a player: naked body-part sprites for every empty
 * slot, then the appearances of whatever is equipped on top.
 *
 * <p>The sprite names themselves live in {@link Paths#APPEARANCE_DEFAULTS_BIN} and are read
 * through {@link AppearanceDefaultsCatalog}; only the male puppet is used for players.
 */
public final class PlayerAppearanceDefaults {

    private PlayerAppearanceDefaults() {
    }

    public static void applyDefaults(Player player) {
        if (player == null || player.getAnimations() == null) {
            return;
        }
        Map<BodyPart, String> partMap = player.getAnimations().getPartMap();
        if (partMap == null) {
            return;
        }
        Map<BodyPart, String> nakedParts =
                AppearanceDefaultsCatalog.nakedParts(AppearanceDefaultsCatalog.MALE);
        resetEquipmentVisuals(partMap);
        for (Map.Entry<BodyPart, String> entry : nakedParts.entrySet()) {
            ensureDefaultPart(partMap, entry.getKey(), entry.getValue());
        }

        applyEquippedAppearances(player, partMap);
        ConcealmentResolver.applyConcealment(partMap);
        player.getAnimations().setBowEquipped(hasBowEquipped(player));
    }

    /**
     * Returns true when any equipped item is a bow.
     */
    public static boolean hasBowEquipped(Player player) {
        if (player == null || player.getEquippedItems() == null) {
            return false;
        }
        for (String itemName : player.getEquippedItems().values()) {
            if (itemName == null || itemName.isEmpty()) {
                continue;
            }
            ItemDefinition def = ItemDefinition.get(itemName);
            if (def != null && def.isBow()) {
                return true;
            }
        }
        return false;
    }

    public static String getAppearanceFor(String itemName, BodyPart part) {
        if (itemName == null || itemName.isEmpty()) {
            return null;
        }
        ItemDefinition def = ItemDefinition.get(itemName);
        if (def == null) {
            return itemName;
        }
        String appearance = def.getAppearanceEquippedFor(part);
        if (appearance != null && !appearance.isEmpty()) {
            return appearance;
        }
        // A registered item without an equipped appearance is intentionally
        // invisible in the original client (quivers, focuses, arrows, etc.).
        // Never interpret its display/key name as a sprite base.
        return null;
    }

    private static void applyEquippedAppearances(Player player, Map<BodyPart, String> partMap) {
        if (player.getEquippedItems() == null) {
            return;
        }
        for (Map.Entry<BodyPart, String> entry : player.getEquippedItems().entrySet()) {
            BodyPart part = entry.getKey();
            String itemName = entry.getValue();
            if (part == null || itemName == null || itemName.isEmpty()) {
                continue;
            }
            ItemDefinition def = ItemDefinition.get(itemName);
            String primary = getAppearanceFor(itemName, part);
            if (primary != null && !primary.isEmpty()) {
                // Inventory/equipment uses HEAD as its slot. The original puppet
                // renders its sprite independently in PUP_HAT (index 11), while
                // PUP_HEAD (index 5) remains the naked head for crowns and hats.
                BodyPart visualPart = part == BodyPart.HEAD ? BodyPart.HAT : part;
                partMap.put(visualPart, primary);
            }
            if (def != null && def.getSecondaryBodyPart() != null) {
                String secondary = def.getAppearanceEquippedSecondary();
                if (secondary != null && !secondary.isEmpty()) {
                    partMap.put(def.getSecondaryBodyPart(), secondary);
                }
            }
        }
    }

    private static void ensureDefaultPart(Map<BodyPart, String> partMap, BodyPart part, String sprite) {
        // These are sprite bases, not item keys: no naked part has an ItemDefinition of its own,
        // so they are written straight through rather than resolved against the item registry.
        if (partMap.containsKey(part) || sprite == null || sprite.isEmpty()) {
            return;
        }
        partMap.put(part, sprite);
    }

    private static void resetEquipmentVisuals(Map<BodyPart, String> partMap) {
        // Rebuild every equipment-driven layer from equippedItems. HAIR is a
        // character customization and is therefore deliberately preserved.
        for (BodyPart part : BodyPart.values()) {
            if (part != BodyPart.HAIR) partMap.remove(part);
        }
    }
}
