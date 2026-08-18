package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemMithrilChainmailBoots {
  private ItemItemMithrilChainmailBoots() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_chainmail_boots",
        "${item.mithril_chainmail_boots}",
        BodyPart.FEET,
        "PupBlackLeatherBoots",
        null,
        null,
        "64kInvBlackLeatherBoots",
        8527L,
        5L,
        4.995d,
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
        40338,
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
            new ItemDefinition.ItemBoost(186, 12, "3", 0, 0),
            new ItemDefinition.ItemBoost(187, 22, "3", 0, 0),
            new ItemDefinition.ItemBoost(188, 15, "3", 0, 0),
            new ItemDefinition.ItemBoost(189, 13, "3", 0, 0),
            new ItemDefinition.ItemBoost(190, 14, "3", 0, 0)),
        List.of(),
        false);
  }
}
