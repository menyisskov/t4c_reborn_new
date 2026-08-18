package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ExperiencedArcherBelt {
  private ExperiencedArcherBelt() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.experienced_archer_belt",
        "${item.experienced_archer_belt}",
        BodyPart.BELT,
        null,
        null,
        null,
        "64kInvBelt",
        2000L,
        0L,
        4.0d,
        0L,
        50L,
        0L,
        50L,
        150L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        3284,
        9,
        235,
        null,
        null,
        0,
        -1,
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
