package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class GleamingBluestoneGauntlets {
  private GleamingBluestoneGauntlets() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gleaming_bluestone_gauntlets",
        "${item.gleaming_bluestone_gauntlets}",
        BodyPart.LEFT_HAND,
        "PupPlateGloveL",
        BodyPart.RIGHT_HAND,
        "PupPlateGloveR",
        "64kInvPlateGlove",
        0L,
        3L,
        3.645d,
        5L,
        125L,
        0L,
        150L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        41558,
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
