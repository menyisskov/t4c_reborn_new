package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AntepalArchiBoots {
  private AntepalArchiBoots() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.antepal_archi_boots",
        "${item.antepal_archi_boots}",
        BodyPart.FEET,
        "V2_ManArmorBoots01__pal4",
        null,
        null,
        "Inv_ManArmor01Boots__pal4",
        500000L,
        2L,
        30.0d,
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
        3593,
        2,
        1132,
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
