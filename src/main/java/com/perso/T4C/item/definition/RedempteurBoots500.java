package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RedempteurBoots500 {
  private RedempteurBoots500() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.redempteur_boots_500",
        "${item.redempteur_boots_500}",
        BodyPart.FEET,
        "V2_ManArmorBoots01",
        null,
        null,
        "Inv_ManArmor01Boots",
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
        4136,
        2,
        885,
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
