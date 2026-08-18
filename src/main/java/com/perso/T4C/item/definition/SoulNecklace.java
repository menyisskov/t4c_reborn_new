package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class SoulNecklace {
  private SoulNecklace() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.soul_necklace",
        "${item.soul_necklace}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 1",
        2415L,
        1L,
        10.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        75L,
        300L,
        1.0d,
        false,
        false,
        false,
        3330,
        2,
        172,
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
