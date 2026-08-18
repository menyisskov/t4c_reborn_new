package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemMithrilChainmailLeggings {
  private ItemItemMithrilChainmailLeggings() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_chainmail_leggings",
        "${item.mithril_chainmail_leggings}",
        BodyPart.LEGS,
        "PupChainMailLegs",
        null,
        null,
        "64kInvChainMailLegs",
        9910L,
        7L,
        5.55d,
        6L,
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
        40332,
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
            new ItemDefinition.ItemBoost(201, 12, "3", 0, 0),
            new ItemDefinition.ItemBoost(202, 22, "3", 0, 0),
            new ItemDefinition.ItemBoost(203, 15, "3", 0, 0),
            new ItemDefinition.ItemBoost(204, 13, "3", 0, 0),
            new ItemDefinition.ItemBoost(205, 14, "3", 0, 0)),
        List.of(),
        false);
  }
}
