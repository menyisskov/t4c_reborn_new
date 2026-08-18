package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemHighMetalBattleAxe1 {
  private ItemItemHighMetalBattleAxe1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.high_metal_battle_axe_1",
        "${item.high_metal_battle_axe_1}",
        BodyPart.WEAPON,
        "PupBattleAxe",
        null,
        null,
        "64kInvDouble Axe",
        0L,
        13L,
        0.0d,
        0L,
        0L,
        0L,
        271L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40642,
        1,
        122,
        "1d83+155",
        "if(1125-self.agi/250*1125/2<600?600:1125-self.agi/250*1125/2)+1d563",
        0,
        0,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(),
        List.of(new ItemDefinition.ItemBoost(391, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
