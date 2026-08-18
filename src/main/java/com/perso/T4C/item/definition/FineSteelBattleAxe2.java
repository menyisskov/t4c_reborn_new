package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class FineSteelBattleAxe2 {
  private FineSteelBattleAxe2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.fine_steel_battle_axe_2",
        "${item.fine_steel_battle_axe_2}",
        BodyPart.WEAPON,
        "PupBattleAxe",
        null,
        null,
        "64kInvDouble Axe",
        83191L,
        13L,
        0.0d,
        0L,
        0L,
        0L,
        184L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40418,
        1,
        122,
        "1d64+117",
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
        List.of(new ItemDefinition.ItemBoost(377, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
