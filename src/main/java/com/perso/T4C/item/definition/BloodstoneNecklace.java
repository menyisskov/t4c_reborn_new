package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class BloodstoneNecklace {
  private BloodstoneNecklace() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.bloodstone_necklace",
        "${item.bloodstone_necklace}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 2",
        1981L,
        1L,
        2.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        44L,
        21L,
        0.0d,
        false,
        false,
        false,
        40724,
        2,
        173,
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
        List.of(new ItemDefinition.ItemBoost(449, 1, "10", 0, 0)),
        List.of(),
        false);
  }
}
