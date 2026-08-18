package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemHighMetalBastardSword1Dual {
  private ItemItemHighMetalBastardSword1Dual() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.high_metal_bastard_sword_1_dual",
        "${item.high_metal_bastard_sword_1_dual}",
        BodyPart.WEAPON2,
        "V2_2BusterSlayer03",
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
        3170,
        1,
        709,
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
        List.of(),
        List.of(),
        false);
  }
}
