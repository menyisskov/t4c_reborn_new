package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class P5 {
  private P5() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.p5",
        "${item.p5}",
        null,
        null,
        null,
        null,
        "64kInvPotions 5",
        0L,
        0L,
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
        40875,
        1,
        255,
        null,
        "0",
        0,
        0,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10007, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
