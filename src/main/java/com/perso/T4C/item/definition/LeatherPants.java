package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class LeatherPants {
  private LeatherPants() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.leather_pants",
        "${item.leather_pants}",
        BodyPart.LEGS,
        "PupLeatherPants",
        null,
        null,
        "64kInvLeatherArmorLegs",
        90L,
        5L,
        0.45d,
        1L,
        25L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40021,
        2,
        261,
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
