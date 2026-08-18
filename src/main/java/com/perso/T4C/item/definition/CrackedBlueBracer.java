package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CrackedBlueBracer {
  private CrackedBlueBracer() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cracked_blue_bracer",
        "${item.cracked_blue_bracer}",
        BodyPart.BRACER,
        null,
        null,
        null,
        "64kInvBracelet",
        0L,
        1L,
        10.0d,
        0L,
        50L,
        300L,
        100L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        41714,
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
            new ItemDefinition.ItemBoost(1001, 14, "30", 0, 0),
            new ItemDefinition.ItemBoost(1002, 13, "-15", 0, 0)),
        List.of(),
        false);
  }
}
