package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAgilityNecklace {
  private ItemItemAgilityNecklace() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.agility_necklace",
        "${item.agility_necklace}",
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
        60L,
        300L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        3333,
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
