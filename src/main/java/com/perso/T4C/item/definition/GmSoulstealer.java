package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class GmSoulstealer {
  private GmSoulstealer() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gm_soulstealer",
        "${item.gm_soulstealer}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvGlinting Sword",
        1L,
        10L,
        666.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40073,
        1,
        202,
        "1d666",
        "0",
        100,
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
            new ItemDefinition.ItemBoost(24, 3, "50", 0, 0),
            new ItemDefinition.ItemBoost(25, 2, "50", 0, 0),
            new ItemDefinition.ItemBoost(26, 6, "50", 0, 0),
            new ItemDefinition.ItemBoost(27, 1, "50", 0, 0),
            new ItemDefinition.ItemBoost(28, 7, "50", 0, 0),
            new ItemDefinition.ItemBoost(29, 5, "50", 0, 0),
            new ItemDefinition.ItemBoost(30, 4, "50", 0, 0),
            new ItemDefinition.ItemBoost(31, 8, "666", 0, 0),
            new ItemDefinition.ItemBoost(32, 9, "666", 0, 0)),
        List.of(),
        false);
  }
}
