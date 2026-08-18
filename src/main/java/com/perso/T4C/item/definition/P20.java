package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class P20 {
  private P20() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.p20",
        "${item.p20}",
        null,
        null,
        null,
        null,
        "64kInvBlueFlask",
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
        40890,
        1,
        236,
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
