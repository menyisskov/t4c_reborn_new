package com.perso.T4C.combat;

import com.perso.T4C.item.InventoryService;
import com.perso.T4C.player.Player;

public final class ArmorClassRules {
  private ArmorClassRules() {}

  public static double trueArmorClass(Player player) {
    return player == null ? 0d : Math.max(0d, InventoryService.equippedArmor(player));
  }

  public static double effectiveArmorClass(Player player) {
    if (player == null) return 0d;
    return Math.max(0d, trueArmorClass(player) + player.getArmorClassBoost());
  }
}
