package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemScreamingHelmOfOgrimar {
  private ItemItemScreamingHelmOfOgrimar() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.screaming_helm_of_ogrimar",
        "${item.screaming_helm_of_ogrimar}",
        BodyPart.HEAD,
        "PupHornedHelmet",
        null,
        null,
        "64kInvHornedHelmet",
        38620L,
        3L,
        2.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        102L,
        54L,
        0.0d,
        false,
        false,
        false,
        41454,
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
            new ItemDefinition.ItemBoost(696, 24, "10", 0, 0),
            new ItemDefinition.ItemBoost(697, 23, "-10", 0, 0),
            new ItemDefinition.ItemBoost(698, 18, "10", 0, 0),
            new ItemDefinition.ItemBoost(699, 19, "10", 0, 0),
            new ItemDefinition.ItemBoost(700, 16, "10", 0, 0),
            new ItemDefinition.ItemBoost(772, 10009, "-10", 0, 0)),
        List.of(),
        false);
  }
}
