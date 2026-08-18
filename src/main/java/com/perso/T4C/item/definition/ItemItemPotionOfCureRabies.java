package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemPotionOfCureRabies {
  private ItemItemPotionOfCureRabies() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.potion_of_cure_rabies",
        "${item.potion_of_cure_rabies}",
        null,
        null,
        null,
        null,
        "64kInvPotions 4",
        333L,
        1L,
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
        41410,
        5,
        254,
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
        List.of(new ItemDefinition.ItemSpell(10353, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
