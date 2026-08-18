package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class BraceletOfPower {
  private BraceletOfPower() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.bracelet_of_power",
        "${item.bracelet_of_power}",
        BodyPart.BRACER,
        null,
        null,
        null,
        "64kInvBracelet",
        1189L,
        1L,
        1.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        24L,
        29L,
        0.0d,
        false,
        false,
        false,
        40045,
        2,
        237,
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
            new ItemDefinition.ItemBoost(12, 3, "5", 0, 0),
            new ItemDefinition.ItemBoost(13, 8, "5", 0, 0)),
        List.of(),
        false);
  }
}
