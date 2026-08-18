package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MithrilChainmailHelmet {
  private MithrilChainmailHelmet() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_chainmail_helmet",
        "${item.mithril_chainmail_helmet}",
        BodyPart.HEAD,
        "PupChainMailCoif",
        null,
        null,
        "64kInvChainMailHelm",
        9449L,
        5L,
        4.81d,
        5L,
        150L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40325,
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
            new ItemDefinition.ItemBoost(196, 12, "3", 0, 0),
            new ItemDefinition.ItemBoost(197, 22, "3", 0, 0),
            new ItemDefinition.ItemBoost(198, 15, "3", 0, 0),
            new ItemDefinition.ItemBoost(199, 13, "3", 0, 0),
            new ItemDefinition.ItemBoost(200, 14, "3", 0, 0)),
        List.of(),
        false);
  }
}
