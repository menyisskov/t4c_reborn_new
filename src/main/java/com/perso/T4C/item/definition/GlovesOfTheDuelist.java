package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class GlovesOfTheDuelist {
  private GlovesOfTheDuelist() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gloves_of_the_duelist",
        "${item.gloves_of_the_duelist}",
        BodyPart.LEFT_HAND,
        "PupLeatherGloveL",
        BodyPart.RIGHT_HAND,
        "PupLeatherGloveR",
        "64kInvLeatherArmorGloves",
        467L,
        4L,
        1.81d,
        3L,
        45L,
        0L,
        0L,
        0L,
        27L,
        33L,
        0.0d,
        false,
        false,
        false,
        40122,
        2,
        259,
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
        List.of(new ItemDefinition.ItemBoost(75, 8, "25", 0, 0)),
        List.of(),
        false);
  }
}
