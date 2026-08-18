package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AliGlovesGold {
  private AliGlovesGold() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ali_gloves_gold",
        "${item.ali_gloves_gold}",
        BodyPart.LEFT_HAND,
        "V2_ManArmorRGlove01__pal7",
        BodyPart.RIGHT_HAND,
        "V2_ManArmorLGlove01__pal7",
        "Inv_ManArmor01Glove__pal7",
        500000L,
        2L,
        8.0d,
        0L,
        70L,
        0L,
        350L,
        350L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        3893,
        2,
        1128,
        null,
        null,
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
