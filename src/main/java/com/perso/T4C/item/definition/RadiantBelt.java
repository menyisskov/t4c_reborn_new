package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RadiantBelt {
  private RadiantBelt() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.radiant_belt",
        "${item.radiant_belt}",
        BodyPart.BELT,
        null,
        null,
        null,
        "64kInvBelt",
        500000L,
        2L,
        12.0d,
        0L,
        300L,
        0L,
        0L,
        0L,
        75L,
        400L,
        1.0d,
        false,
        false,
        true,
        3515,
        2,
        235,
        null,
        null,
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
