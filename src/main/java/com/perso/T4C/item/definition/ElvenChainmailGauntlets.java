package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ElvenChainmailGauntlets {
  private ElvenChainmailGauntlets() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.elven_chainmail_gauntlets",
        "${item.elven_chainmail_gauntlets}",
        BodyPart.LEFT_HAND,
        "PupLeatherGloveL",
        BodyPart.RIGHT_HAND,
        "PupLeatherGloveR",
        "64kInvLeatherArmorGloves",
        4343L,
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
        40432,
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
            new ItemDefinition.ItemBoost(166, 12, "2", 0, 0),
            new ItemDefinition.ItemBoost(167, 22, "2", 0, 0),
            new ItemDefinition.ItemBoost(168, 15, "2", 0, 0),
            new ItemDefinition.ItemBoost(169, 13, "2", 0, 0),
            new ItemDefinition.ItemBoost(170, 14, "2", 0, 0)),
        List.of(),
        false);
  }
}
