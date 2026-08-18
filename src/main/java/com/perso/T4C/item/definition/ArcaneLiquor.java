package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ArcaneLiquor {
  private ArcaneLiquor() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.arcane_liquor",
        "${item.arcane_liquor}",
        null,
        null,
        null,
        null,
        "64kInvBlueFlask",
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
        41518,
        5,
        236,
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
        List.of(new ItemDefinition.ItemSpell(10463, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
