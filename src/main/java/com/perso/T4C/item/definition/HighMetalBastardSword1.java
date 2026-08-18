package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class HighMetalBastardSword1 {
  private HighMetalBastardSword1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.high_metal_bastard_sword_1",
        "${item.high_metal_bastard_sword_1}",
        BodyPart.WEAPON,
        "V2_BusterSlayer03",
        null,
        null,
        "Inv_V2_BusterSlayer03",
        98491L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        242L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40471,
        1,
        694,
        "1d61+115",
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
        List.of(new ItemDefinition.ItemBoost(311, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
