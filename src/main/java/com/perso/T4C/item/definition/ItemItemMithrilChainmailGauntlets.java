package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemMithrilChainmailGauntlets {
  private ItemItemMithrilChainmailGauntlets() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_chainmail_gauntlets",
        "${item.mithril_chainmail_gauntlets}",
        BodyPart.LEFT_HAND,
        "PupLeatherGloveL",
        BodyPart.RIGHT_HAND,
        "PupLeatherGloveR",
        "64kInvLeatherArmorGloves",
        8724L,
        5L,
        4.995d,
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
        40333,
        2,
        259,
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
            new ItemDefinition.ItemBoost(191, 12, "3", 0, 0),
            new ItemDefinition.ItemBoost(192, 22, "3", 0, 0),
            new ItemDefinition.ItemBoost(193, 15, "3", 0, 0),
            new ItemDefinition.ItemBoost(194, 13, "3", 0, 0),
            new ItemDefinition.ItemBoost(195, 14, "3", 0, 0)),
        List.of(),
        false);
  }
}
