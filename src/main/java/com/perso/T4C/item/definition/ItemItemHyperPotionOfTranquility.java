package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemHyperPotionOfTranquility {
  private ItemItemHyperPotionOfTranquility() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.hyper_potion_of_tranquility",
        "${item.hyper_potion_of_tranquility}",
        null,
        null,
        null,
        null,
        "64kInvMisc 9 - All 3",
        666L,
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
        40816,
        5,
        98,
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
        List.of(new ItemDefinition.ItemSpell(10178, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
