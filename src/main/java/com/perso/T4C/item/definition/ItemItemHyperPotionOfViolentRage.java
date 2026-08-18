package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemHyperPotionOfViolentRage {
  private ItemItemHyperPotionOfViolentRage() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.hyper_potion_of_violent_rage",
        "${item.hyper_potion_of_violent_rage}",
        null,
        null,
        null,
        null,
        "64kInvPotions 8",
        1850L,
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
        40400,
        5,
        250,
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
        List.of(new ItemDefinition.ItemSpell(10183, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
