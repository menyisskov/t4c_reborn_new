package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AntepalAbyssGloves {
  private AntepalAbyssGloves() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.antepal_abyss_gloves",
        "${item.antepal_abyss_gloves}",
        BodyPart.LEFT_HAND,
        "V2_ManArmorRGlove01__pal5",
        BodyPart.RIGHT_HAND,
        "V2_ManArmorLGlove01__pal5",
        "Inv_ManArmor01Glove__pal5",
        500000L,
        2L,
        28.0d,
        0L,
        300L,
        0L,
        200L,
        0L,
        250L,
        75L,
        1.0d,
        false,
        false,
        true,
        3603,
        2,
        1126,
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
