package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class GirdleOfStrength {
  private GirdleOfStrength() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.girdle_of_strength",
        "${item.girdle_of_strength}",
        BodyPart.BELT,
        null,
        null,
        null,
        "64kInvBelt",
        926L,
        3L,
        0.6d,
        2L,
        45L,
        0L,
        0L,
        0L,
        25L,
        30L,
        0.0d,
        false,
        false,
        false,
        40130,
        2,
        235,
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
        List.of(new ItemDefinition.ItemBoost(98, 3, "10", 0, 0)),
        List.of(),
        false);
  }
}
