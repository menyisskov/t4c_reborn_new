package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemGemOfTheSun {
  private ItemItemGemOfTheSun() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gem_of_the_sun",
        "${item.gem_of_the_sun}",
        null,
        null,
        null,
        null,
        "64kInvGems 2",
        0L,
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
        41658,
        5,
        165,
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
        List.of(new ItemDefinition.ItemSpell(10679, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
