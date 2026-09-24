package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemCriticalHealingPotion {
  private ItemItemCriticalHealingPotion() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.critical_healing_potion",
        "${item.critical_healing_potion}",
        null,
        null,
        null,
        null,
        "64kInvPotion 2",
        25000L,
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
        40621,
        5,
        241,
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
        List.of(new ItemDefinition.ItemSpell(10208, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
