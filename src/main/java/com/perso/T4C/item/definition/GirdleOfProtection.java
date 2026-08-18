package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class GirdleOfProtection {
  private GirdleOfProtection() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.girdle_of_protection",
        "${item.girdle_of_protection}",
        BodyPart.BELT,
        null,
        null,
        null,
        "64kInvBelt",
        472L,
        3L,
        2.3d,
        1L,
        25L,
        0L,
        0L,
        0L,
        19L,
        21L,
        0.0d,
        false,
        false,
        false,
        40600,
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
        List.of(),
        List.of(),
        false);
  }
}
