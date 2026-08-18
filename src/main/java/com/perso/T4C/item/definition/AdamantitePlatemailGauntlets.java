package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AdamantitePlatemailGauntlets {
  private AdamantitePlatemailGauntlets() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.adamantite_platemail_gauntlets",
        "${item.adamantite_platemail_gauntlets}",
        BodyPart.LEFT_HAND,
        "PupPlateGloveL",
        BodyPart.RIGHT_HAND,
        "PupPlateGloveR",
        "64kInvPlateGlove",
        22051L,
        7L,
        10.665d,
        15L,
        230L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40517,
        2,
        263,
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
