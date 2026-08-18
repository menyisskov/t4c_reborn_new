package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemLostHelmOfTheDragon {
  private ItemItemLostHelmOfTheDragon() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.lost_helm_of_the_dragon",
        "${item.lost_helm_of_the_dragon}",
        BodyPart.HEAD,
        "PupHornedHelmet",
        null,
        null,
        "64kInvHornedHelmet",
        0L,
        8L,
        7.5d,
        20L,
        150L,
        0L,
        0L,
        0L,
        30L,
        46L,
        0.0d,
        false,
        false,
        false,
        41466,
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
            new ItemDefinition.ItemBoost(842, 13, "5", 0, 0),
            new ItemDefinition.ItemBoost(843, 14, "5", 0, 0),
            new ItemDefinition.ItemBoost(844, 12, "5", 0, 0),
            new ItemDefinition.ItemBoost(845, 15, "5", 0, 0),
            new ItemDefinition.ItemBoost(846, 22, "5", 0, 0)),
        List.of(),
        false);
  }
}
