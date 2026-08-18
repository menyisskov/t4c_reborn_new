package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemHyperPotionOfNimbleness {
  private ItemItemHyperPotionOfNimbleness() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.hyper_potion_of_nimbleness",
        "${item.hyper_potion_of_nimbleness}",
        null,
        null,
        null,
        null,
        "64kInvPotions 2",
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
        40818,
        5,
        252,
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
        List.of(new ItemDefinition.ItemSpell(10180, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
