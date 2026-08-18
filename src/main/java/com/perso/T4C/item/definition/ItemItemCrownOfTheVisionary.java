package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemCrownOfTheVisionary {
  private ItemItemCrownOfTheVisionary() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.crown_of_the_visionary",
        "${item.crown_of_the_visionary}",
        BodyPart.HEAD,
        "PupGoldenCrown",
        null,
        null,
        "64kInvGoldenCrown",
        7923L,
        3L,
        3.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        47L,
        50L,
        0.0d,
        false,
        false,
        false,
        41617,
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
            new ItemDefinition.ItemBoost(911, 13, "5", 0, 0),
            new ItemDefinition.ItemBoost(912, 14, "5", 0, 0),
            new ItemDefinition.ItemBoost(913, 15, "5", 0, 0),
            new ItemDefinition.ItemBoost(914, 12, "5", 0, 0),
            new ItemDefinition.ItemBoost(915, 22, "5", 0, 0)),
        List.of(),
        false);
  }
}
