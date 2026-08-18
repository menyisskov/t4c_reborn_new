package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemPotionOfCurePoison {
  private ItemItemPotionOfCurePoison() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.potion_of_cure_poison",
        "${item.potion_of_cure_poison}",
        null,
        null,
        null,
        null,
        "64kInvPotions 5",
        66L,
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
        41409,
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
        List.of(new ItemDefinition.ItemSpell(10394, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
