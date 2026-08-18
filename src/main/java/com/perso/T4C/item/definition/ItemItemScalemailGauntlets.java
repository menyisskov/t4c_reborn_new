package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemScalemailGauntlets {
  private ItemItemScalemailGauntlets() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.scalemail_gauntlets",
        "${item.scalemail_gauntlets}",
        BodyPart.LEFT_HAND,
        "PupLeatherGloveL",
        BodyPart.RIGHT_HAND,
        "PupLeatherGloveR",
        "64kInvLeatherArmorGloves",
        3485L,
        4L,
        2.565d,
        4L,
        100L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40506,
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
        List.of(),
        List.of(),
        false);
  }
}
