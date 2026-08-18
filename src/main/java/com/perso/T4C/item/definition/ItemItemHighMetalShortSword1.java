package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemHighMetalShortSword1 {
  private ItemItemHighMetalShortSword1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.high_metal_short_sword_1",
        "${item.high_metal_short_sword_1}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvShortSword",
        55460L,
        6L,
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
        40383,
        1,
        1,
        "1d37+69",
        "if(750-self.agi/250*750/2<600?600:750-self.agi/250*750/2)+1d375",
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
        List.of(new ItemDefinition.ItemBoost(303, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
