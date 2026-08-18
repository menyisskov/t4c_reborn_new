package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class HelmetOfTheHealer {
  private HelmetOfTheHealer() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.helmet_of_the_healer",
        "${item.helmet_of_the_healer}",
        BodyPart.HEAD,
        "PupHornedHelmet",
        null,
        null,
        "64kInvHornedHelmet",
        150L,
        4L,
        1.0d,
        1L,
        30L,
        0L,
        0L,
        0L,
        0L,
        30L,
        0.0d,
        false,
        false,
        false,
        40125,
        2,
        276,
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
        List.of(new ItemDefinition.ItemBoost(76, 4, "5", 0, 0)),
        List.of(),
        false);
  }
}
