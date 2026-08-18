package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemPotionOfPartialEarthResistance {
  private ItemItemPotionOfPartialEarthResistance() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.potion_of_partial_earth_resistance",
        "${item.potion_of_partial_earth_resistance}",
        null,
        null,
        null,
        null,
        "64kInvPotions 5",
        166L,
        2L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40612,
        5,
        255,
        null,
        "0",
        0,
        1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10287, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
