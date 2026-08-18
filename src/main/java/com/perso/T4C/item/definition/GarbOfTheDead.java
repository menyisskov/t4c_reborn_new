package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class GarbOfTheDead {
  private GarbOfTheDead() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.garb_of_the_dead",
        "${item.garb_of_the_dead}",
        BodyPart.BODY,
        "PupNecromanRobe",
        null,
        null,
        "64kInvNecromanRobe",
        8747L,
        5L,
        11.0d,
        25L,
        21L,
        0L,
        0L,
        0L,
        128L,
        15L,
        0.0d,
        false,
        false,
        false,
        41148,
        2,
        278,
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
            new ItemDefinition.ItemBoost(608, 1, "20", 0, 0),
            new ItemDefinition.ItemBoost(609, 24, "25", 0, 0)),
        List.of(),
        false);
  }
}
