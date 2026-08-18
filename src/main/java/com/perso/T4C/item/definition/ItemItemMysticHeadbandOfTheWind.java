package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemMysticHeadbandOfTheWind {
  private ItemItemMysticHeadbandOfTheWind() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mystic_headband_of_the_wind",
        "${item.mystic_headband_of_the_wind}",
        BodyPart.HEAD,
        "PupGoldenCrown",
        null,
        null,
        "64kInvGoldenCrown",
        0L,
        3L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        76L,
        66L,
        0.0d,
        false,
        false,
        false,
        41294,
        2,
        279,
        null,
        "0",
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
        List.of(
            new ItemDefinition.ItemBoost(637, 9, "50", 0, 0),
            new ItemDefinition.ItemBoost(638, 16, "25", 0, 0)),
        List.of(),
        false);
  }
}
