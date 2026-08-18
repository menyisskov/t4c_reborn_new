package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class FineSteelHandAxe1 {
  private FineSteelHandAxe1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.fine_steel_hand_axe_1",
        "${item.fine_steel_hand_axe_1}",
        BodyPart.WEAPON,
        "V2_Hache02",
        null,
        null,
        "Inv_V2_Hache02",
        38547L,
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
        40407,
        1,
        723,
        "1d40+72",
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
        List.of(new ItemDefinition.ItemBoost(374, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
