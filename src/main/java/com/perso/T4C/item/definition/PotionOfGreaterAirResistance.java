package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class PotionOfGreaterAirResistance {
  private PotionOfGreaterAirResistance() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.potion_of_greater_air_resistance",
        "${item.potion_of_greater_air_resistance}",
        null,
        null,
        null,
        null,
        "64kInvPotion 1",
        1000L,
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
        40618,
        5,
        240,
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
        List.of(new ItemDefinition.ItemSpell(10291, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
