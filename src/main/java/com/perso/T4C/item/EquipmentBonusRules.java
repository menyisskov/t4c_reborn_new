package com.perso.T4C.item;

import com.perso.T4C.combat.EquipmentBoostRules;
import com.perso.T4C.combat.WeaponDamageRules;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import java.util.ArrayList;
import java.util.Map;

/** Offline equipment lookup adapter for the shared base-stat boost evaluator. */
public final class EquipmentBonusRules {
  private EquipmentBonusRules() {}

  public static int bonus(Player player, int statId) {
    if (player == null) return 0;
    var boosts = new ArrayList<EquipmentBoostRules.Boost>();
    for (Map.Entry<BodyPart, String> equipped : player.getEquippedItems().entrySet()) {
      ItemDefinition item = ItemRegistry.findByKey(equipped.getValue());
      if (item == null || isMirrored(player, equipped.getKey(), equipped.getValue(), item)) continue;
      for (ItemDefinition.ItemBoost boost : item.getBoosts()) {
        boosts.add(new EquipmentBoostRules.Boost(boost.getBoostId(), boost.getStatId(),
            boost.getExpression(), boost.getMinWis(), boost.getMinInt()));
      }
    }
    var base = new WeaponDamageRules.Stats(player.getStrength(), player.getEndurance(),
        player.getDexterity(), player.getIntelligence(), player.getWisdom(), player.getLevel());
    var skills = Map.of("attack", player.getSkillLevel("attack"), "dodge", player.getSkillLevel("dodge"),
        "light", player.getSkillLevel("light"), "archery", player.getSkillLevel("archery"));
    return EquipmentBoostRules.bonuses(base, skills, boosts).getOrDefault(statId, 0);
  }

  private static boolean isMirrored(Player player, BodyPart slot, String key, ItemDefinition item) {
    return item.getSecondaryBodyPart() == slot
        && key.equals(player.getEquippedItems().get(item.getBodyPart()));
  }
}
