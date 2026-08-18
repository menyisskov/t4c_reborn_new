package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemFineSteelBattleAxe3 {
  private ItemItemFineSteelBattleAxe3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.fine_steel_battle_axe_3",
        "${item.fine_steel_battle_axe_3}",
        BodyPart.WEAPON,
        "PupBattleAxe",
        null,
        null,
        "64kInvDouble Axe",
        93191L,
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
        40398,
        1,
        122,
        "1d73+136",
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
        List.of(new ItemDefinition.ItemBoost(378, 8, "self.true_attack*50/100", 0, 0)),
        List.of(),
        false);
  }
}
