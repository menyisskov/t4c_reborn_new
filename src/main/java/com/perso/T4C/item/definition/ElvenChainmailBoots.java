package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ElvenChainmailBoots {
  private ElvenChainmailBoots() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.elven_chainmail_boots",
        "${item.elven_chainmail_boots}",
        BodyPart.FEET,
        "PupBlackLeatherBoots",
        null,
        null,
        "64kInvBlackLeatherBoots",
        4245L,
        5L,
        2.97d,
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
        40425,
        2,
        288,
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
            new ItemDefinition.ItemBoost(161, 12, "2", 0, 0),
            new ItemDefinition.ItemBoost(162, 22, "2", 0, 0),
            new ItemDefinition.ItemBoost(163, 15, "2", 0, 0),
            new ItemDefinition.ItemBoost(164, 13, "2", 0, 0),
            new ItemDefinition.ItemBoost(165, 14, "2", 0, 0)),
        List.of(),
        false);
  }
}
