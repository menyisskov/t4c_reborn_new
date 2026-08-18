package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RobeOfHell {
  private RobeOfHell() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.robe_of_hell",
        "${item.robe_of_hell}",
        BodyPart.BODY,
        "PupNecromanRobe",
        null,
        null,
        "64kInvNecromanRobe",
        666L,
        5L,
        18.0d,
        25L,
        28L,
        0L,
        0L,
        0L,
        123L,
        85L,
        0.0d,
        false,
        false,
        false,
        41391,
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
            new ItemDefinition.ItemBoost(785, 1, "25", 0, 0),
            new ItemDefinition.ItemBoost(787, 13, "25", 0, 0),
            new ItemDefinition.ItemBoost(788, 22, "25", 0, 0),
            new ItemDefinition.ItemBoost(789, 14, "-15", 0, 0),
            new ItemDefinition.ItemBoost(790, 19, "-15", 0, 0),
            new ItemDefinition.ItemBoost(791, 23, "-15", 0, 0)),
        List.of(),
        false);
  }
}
