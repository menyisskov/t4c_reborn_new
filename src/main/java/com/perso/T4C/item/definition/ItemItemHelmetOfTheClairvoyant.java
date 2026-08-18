package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemHelmetOfTheClairvoyant {
  private ItemItemHelmetOfTheClairvoyant() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.helmet_of_the_clairvoyant",
        "${item.helmet_of_the_clairvoyant}",
        BodyPart.HEAD,
        "PupHornedHelmet",
        null,
        null,
        "64kInvHornedHelmet",
        7923L,
        3L,
        3.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        26L,
        54L,
        0.0d,
        false,
        false,
        false,
        41612,
        2,
        276,
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
            new ItemDefinition.ItemBoost(909, 10009, "10", 0, 0),
            new ItemDefinition.ItemBoost(910, 4, "10", 0, 0)),
        List.of(),
        false);
  }
}
