package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAntepalAbyssArmor {
  private ItemItemAntepalAbyssArmor() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.antepal_abyss_armor",
        "${item.antepal_abyss_armor}",
        BodyPart.BODY,
        "V2_ManArmorBody01__pal5",
        null,
        null,
        "Inv_ManArmor01Body__pal5",
        500000L,
        2L,
        55.0d,
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
        3599,
        2,
        1112,
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
