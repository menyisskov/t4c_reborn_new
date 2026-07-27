package com.perso.T4C.combat;

import com.perso.T4C.item.InventoryService;
import com.perso.T4C.player.Player;

/** Faithful counterpart of Character::GetTrueAC and Character::GetAC. */
public final class ArmorClassRules {
    private ArmorClassRules() {
    }

    /** Character::GetTrueAC: sum of equipped item AC, without spell/stat boosts. */
    public static double trueArmorClass(Player player) {
        return player == null ? 0d : Math.max(0d, InventoryService.equippedArmor(player));
    }

    /** Character::GetAC: QueryBoost(STAT_AC) + GetTrueAC, clamped to zero. */
    public static double effectiveArmorClass(Player player) {
        if (player == null) return 0d;
        return Math.max(0d, trueArmorClass(player) + player.getArmorClassBoost());
    }
}
