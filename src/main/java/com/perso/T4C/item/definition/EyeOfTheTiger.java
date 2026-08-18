package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class EyeOfTheTiger {
  private EyeOfTheTiger() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.eye_of_the_tiger",
        "${item.eye_of_the_tiger}",
        BodyPart.WEAPON2,
        null,
        null,
        null,
        "InvEyeOfTiger",
        0L,
        3L,
        10.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        115L,
        113L,
        0.0d,
        false,
        false,
        false,
        41383,
        2,
        571,
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
            new ItemDefinition.ItemBoost(851, 11, "100", 0, 0),
            new ItemDefinition.ItemBoost(852, 13, "5", 0, 0),
            new ItemDefinition.ItemBoost(853, 14, "5", 0, 0),
            new ItemDefinition.ItemBoost(854, 15, "5", 0, 0),
            new ItemDefinition.ItemBoost(855, 22, "5", 0, 0),
            new ItemDefinition.ItemBoost(856, 12, "5", 0, 0),
            new ItemDefinition.ItemBoost(857, 17, "5", 0, 0),
            new ItemDefinition.ItemBoost(858, 24, "5", 0, 0),
            new ItemDefinition.ItemBoost(859, 16, "5", 0, 0),
            new ItemDefinition.ItemBoost(860, 18, "5", 0, 0),
            new ItemDefinition.ItemBoost(861, 19, "5", 0, 0),
            new ItemDefinition.ItemBoost(862, 23, "5", 0, 0)),
        List.of(),
        false);
  }
}
