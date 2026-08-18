package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemCloakOfTheAges {
  private ItemItemCloakOfTheAges() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cloak_of_the_ages",
        "${item.cloak_of_the_ages}",
        BodyPart.BODY,
        "PupRedRobe",
        null,
        null,
        "64kInvRedRobe",
        0L,
        5L,
        35.0d,
        25L,
        45L,
        0L,
        0L,
        0L,
        234L,
        146L,
        0.0d,
        false,
        false,
        false,
        41155,
        2,
        423,
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
            new ItemDefinition.ItemBoost(626, 17, "35", 0, 0),
            new ItemDefinition.ItemBoost(627, 18, "35", 0, 0),
            new ItemDefinition.ItemBoost(628, 16, "35", 0, 0),
            new ItemDefinition.ItemBoost(629, 19, "35", 0, 0)),
        List.of(),
        false);
  }
}
