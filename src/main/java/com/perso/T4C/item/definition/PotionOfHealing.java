package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class PotionOfHealing {
  private PotionOfHealing() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.potion_of_healing",
        "${item.potion_of_healing}",
        null,
        null,
        null,
        null,
        "64kInvPotion 2",
        16L,
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
        40001,
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
        List.of(new ItemDefinition.ItemSpell(10191, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
