package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class PrismaticArmband {
  private PrismaticArmband() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.prismatic_armband",
        "${item.prismatic_armband}",
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
        400L,
        100L,
        50L,
        30L,
        30L,
        0.0d,
        false,
        false,
        false,
        41712,
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
            new ItemDefinition.ItemBoost(994, 13, "10", 0, 0),
            new ItemDefinition.ItemBoost(995, 14, "10", 0, 0),
            new ItemDefinition.ItemBoost(996, 12, "10", 0, 0),
            new ItemDefinition.ItemBoost(997, 15, "10", 0, 0),
            new ItemDefinition.ItemBoost(998, 22, "-20", 0, 0)),
        List.of(),
        false);
  }
}
