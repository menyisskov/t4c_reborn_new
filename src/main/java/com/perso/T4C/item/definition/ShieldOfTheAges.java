package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ShieldOfTheAges {
  private ShieldOfTheAges() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.shield_of_the_ages",
        "${item.shield_of_the_ages}",
        BodyPart.SHIELD,
        "PupSkeletonShield",
        null,
        null,
        "64kInvSkeletonShield",
        96708L,
        0L,
        41.91d,
        0L,
        300L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40354,
        2,
        470,
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
        List.of(new ItemDefinition.ItemBoost(979, 10008, "25", 0, 0)),
        List.of(),
        false);
  }
}
