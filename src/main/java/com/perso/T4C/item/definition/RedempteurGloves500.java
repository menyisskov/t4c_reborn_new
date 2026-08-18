package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RedempteurGloves500 {
  private RedempteurGloves500() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.redempteur_gloves_500",
        "${item.redempteur_gloves_500}",
        BodyPart.LEFT_HAND,
        "V2_ManArmorRGlove01",
        BodyPart.RIGHT_HAND,
        "V2_ManArmorLGlove01",
        "Inv_ManArmor01Glove",
        500000L,
        2L,
        45.0d,
        0L,
        500L,
        0L,
        200L,
        0L,
        75L,
        250L,
        1.0d,
        false,
        false,
        true,
        4137,
        2,
        883,
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
