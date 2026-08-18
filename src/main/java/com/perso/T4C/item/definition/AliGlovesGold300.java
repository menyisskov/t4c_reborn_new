package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AliGlovesGold300 {
  private AliGlovesGold300() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ali_gloves_gold_300",
        "${item.ali_gloves_gold_300}",
        BodyPart.LEFT_HAND,
        "V2_ManArmorRGlove01__pal7",
        BodyPart.RIGHT_HAND,
        "V2_ManArmorLGlove01__pal7",
        "Inv_ManArmor01Glove__pal7",
        500000L,
        2L,
        28.0d,
        0L,
        270L,
        0L,
        350L,
        350L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        4149,
        2,
        1128,
        null,
        null,
        0,
        -1,
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
