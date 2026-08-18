package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class FineSteelHandAxe2 {
  private FineSteelHandAxe2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.fine_steel_hand_axe_2",
        "${item.fine_steel_hand_axe_2}",
        BodyPart.WEAPON,
        "V2_Hache02",
        null,
        null,
        "Inv_V2_Hache02",
        57821L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        155L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40439,
        1,
        723,
        "1d45+81",
        "if(937-self.agi/250*937/2<600?600:937-self.agi/250*937/2)+1d469",
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
        List.of(new ItemDefinition.ItemBoost(375, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
