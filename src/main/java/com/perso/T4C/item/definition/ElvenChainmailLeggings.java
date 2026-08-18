package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ElvenChainmailLeggings {
  private ElvenChainmailLeggings() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.elven_chainmail_leggings",
        "${item.elven_chainmail_leggings}",
        BodyPart.LEGS,
        "PupChainMailLegs",
        null,
        null,
        "64kInvChainMailLegs",
        4931L,
        8L,
        3.3d,
        4L,
        110L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40415,
        2,
        268,
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
            new ItemDefinition.ItemBoost(176, 12, "2", 0, 0),
            new ItemDefinition.ItemBoost(177, 22, "2", 0, 0),
            new ItemDefinition.ItemBoost(178, 15, "2", 0, 0),
            new ItemDefinition.ItemBoost(179, 13, "2", 0, 0),
            new ItemDefinition.ItemBoost(180, 14, "2", 0, 0)),
        List.of(),
        false);
  }
}
