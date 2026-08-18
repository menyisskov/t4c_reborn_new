package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AliBelt300 {
  private AliBelt300() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ali_belt_300",
        "${item.ali_belt_300}",
        BodyPart.BELT,
        null,
        null,
        null,
        "64kInvBelt",
        500000L,
        2L,
        26.0d,
        0L,
        270L,
        0L,
        350L,
        350L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        4147,
        2,
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
