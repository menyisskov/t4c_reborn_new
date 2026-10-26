package com.perso.T4C.helper;

import com.perso.T4C.combat.WeaponDamageRules;
import com.perso.T4C.item.EquipmentBonusRules;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;

/** Offline adapter; the headless server uses the same rules with its own resolved state. */
public final class CombatMath {
  private CombatMath() {}

  public static int computeMeleeDamage(Player player) {
    return WeaponDamageRules.melee(stats(player), weapon(player, BodyPart.WEAPON),
        weapon(player, BodyPart.WEAPON2), EquipmentBonusRules.bonus(player, 10));
  }

  public static int computeBowDamage(Player player) {
    return WeaponDamageRules.bow(stats(player), weapon(player, BodyPart.WEAPON),
        weapon(player, BodyPart.WEAPON2), EquipmentBonusRules.bonus(player, 10));
  }

  public static int computeOffHandDamage(Player player) {
    if (player == null) return 0;
    return WeaponDamageRules.offHand(stats(player), weapon(player, BodyPart.WEAPON),
        weapon(player, BodyPart.WEAPON2), EquipmentBonusRules.bonus(player, 10));
  }

  private static WeaponDamageRules.Stats stats(Player player) {
    return player == null ? new WeaponDamageRules.Stats(0, 0, 0, 0, 0, 0)
        : new WeaponDamageRules.Stats(player.getEffectiveStrength(), player.getEffectiveEndurance(),
            player.getEffectiveDexterity(), player.getEffectiveIntelligence(), player.getEffectiveWisdom(), player.getLevel());
  }

  private static WeaponDamageRules.Weapon weapon(Player player, BodyPart slot) {
    if (player == null) return null;
    String key = player.getEquippedItems().get(slot);
    var definition = ItemRegistry.findByKey(key);
    return definition == null ? null : new WeaponDamageRules.Weapon(key, definition.getBodyPart(),
        definition.isBow(), definition.getStructure(), definition.getDmgFormula());
  }
}
