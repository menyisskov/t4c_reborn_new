package com.perso.T4C.item;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.ItemIconBinaryIO;

import java.io.File;
import java.util.Map;

/**
 * Generic per-category item icons, the Java port of the C++ client's {@code ItemIcons}
 * table (see {@code GameIcons.h} / {@code VisualObjectList.cpp}).
 *
 * <p>The original client keeps two icon tables keyed by the same appearance id:
 * {@code InvItemIcons} maps each appearance to its own {@code 64kInv*} inventory
 * sprite, while {@code ItemIcons} maps it to one of 26 generic {@code 64kIcon*}
 * category sprites — several appearances deliberately share one icon (short and long
 * sword both use {@code 64kIconSword}). The buy/sell dialog uses {@code ItemIcons}
 * (<code>V3_BuyDlg.cpp</code>: {@code ItemIcons((*i).appearance)}), which is what this
 * class provides; {@link ItemDefinition#getAppearanceInventory()} covers the other table.
 *
 * <p>The table is loaded from {@link Paths#ITEM_ICONS_BIN} via {@link ItemIconBinaryIO}.
 * Lookups that miss return {@code null}, mirroring the {@code "???"} fallback sprite
 * of the C++ {@code GameIcons::operator()} without forcing a placeholder on callers.
 */
public final class ItemIconRegistry {
    private static Map<Integer, String> byAppearanceId;

    private ItemIconRegistry() {
    }

    /** Generic category icon sprite for an appearance id, or {@code null} if unbound. */
    public static synchronized String iconFor(int appearanceId) {
        if (appearanceId <= 0) {
            return null;
        }
        load();
        return byAppearanceId.get(appearanceId);
    }

    /** Generic category icon sprite for an item, or {@code null} if unbound. */
    public static String iconFor(ItemDefinition def) {
        return def == null ? null : iconFor(def.getAppearanceId());
    }

    public static synchronized void invalidate() {
        byAppearanceId = null;
    }

    private static void load() {
        if (byAppearanceId != null) {
            return;
        }
        Map<Integer, String> map = Map.of();
        File file = new File(Paths.ITEM_ICONS_BIN);
        if (file.exists()) {
            try {
                map = ItemIconBinaryIO.read(file);
            } catch (Exception ignored) {
                // Unreadable table: callers fall back to their own icon handling.
            }
        }
        byAppearanceId = Map.copyOf(map);
    }
}
