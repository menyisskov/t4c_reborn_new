package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAntepalArchiGloves {
  private ItemItemAntepalArchiGloves() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.antepal_archi_gloves",
        "${item.antepal_archi_gloves}",
        BodyPart.LEFT_HAND,
        "V2_ManArmorRGlove01__pal4",
        BodyPart.RIGHT_HAND,
        "V2_ManArmorLGlove01__pal4",
        "Inv_ManArmor01Glove__pal4",
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
        3594,
        2,
        1125,
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
