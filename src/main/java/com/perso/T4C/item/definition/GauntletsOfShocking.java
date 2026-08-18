package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class GauntletsOfShocking {
  private GauntletsOfShocking() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gauntlets_of_shocking",
        "${item.gauntlets_of_shocking}",
        BodyPart.LEFT_HAND,
        "PupPlateGloveL",
        BodyPart.RIGHT_HAND,
        "PupPlateGloveR",
        "64kInvPlateGlove",
        5808L,
        8L,
        3.645d,
        16L,
        125L,
        0L,
        0L,
        0L,
        20L,
        25L,
        0.0d,
        false,
        false,
        false,
        41461,
        2,
        263,
        null,
        "0",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10408, 0, 100)),
        List.of(new ItemDefinition.ItemBoost(802, 3, "10", 0, 0)),
        List.of(),
        false);
  }
}
