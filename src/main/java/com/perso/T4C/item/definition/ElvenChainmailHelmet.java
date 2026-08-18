package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ElvenChainmailHelmet {
  private ElvenChainmailHelmet() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.elven_chainmail_helmet",
        "${item.elven_chainmail_helmet}",
        BodyPart.HEAD,
        "PupChainMailCoif",
        null,
        null,
        "64kInvChainMailHelm",
        4702L,
        5L,
        2.86d,
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
        40449,
        2,
        270,
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
            new ItemDefinition.ItemBoost(171, 12, "2", 0, 0),
            new ItemDefinition.ItemBoost(172, 22, "2", 0, 0),
            new ItemDefinition.ItemBoost(173, 15, "2", 0, 0),
            new ItemDefinition.ItemBoost(174, 13, "2", 0, 0),
            new ItemDefinition.ItemBoost(175, 14, "2", 0, 0)),
        List.of(),
        false);
  }
}
