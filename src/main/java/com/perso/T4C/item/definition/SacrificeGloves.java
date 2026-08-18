package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class SacrificeGloves {
  private SacrificeGloves() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.sacrifice_gloves",
        "${item.sacrifice_gloves}",
        BodyPart.LEFT_HAND,
        "V2_ManArmorRGlove01__pal7",
        BodyPart.RIGHT_HAND,
        "V2_ManArmorLGlove01__pal7",
        "Inv_ManArmor01Glove__pal7",
        500000L,
        2L,
        18.0d,
        0L,
        150L,
        0L,
        0L,
        310L,
        310L,
        0L,
        1.0d,
        false,
        false,
        true,
        3927,
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
