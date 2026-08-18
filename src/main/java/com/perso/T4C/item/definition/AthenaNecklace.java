package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AthenaNecklace {
  private AthenaNecklace() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.athena_necklace",
        "${item.athena_necklace}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 3",
        7923L,
        1L,
        8.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        26L,
        34L,
        0.0d,
        false,
        false,
        false,
        41544,
        2,
        174,
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
