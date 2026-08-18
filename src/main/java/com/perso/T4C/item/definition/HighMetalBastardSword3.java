package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class HighMetalBastardSword3 {
  private HighMetalBastardSword3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.high_metal_bastard_sword_3",
        "${item.high_metal_bastard_sword_3}",
        BodyPart.WEAPON,
        "V2_Special06",
        null,
        null,
        "Inv_V2_Sp06",
        0L,
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
        40470,
        1,
        761,
        "1d80+150",
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
        List.of(new ItemDefinition.ItemBoost(313, 8, "self.true_attack*50/100", 0, 0)),
        List.of(),
        false);
  }
}
