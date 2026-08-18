package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class GemOfVitality {
  private GemOfVitality() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gem_of_vitality",
        "${item.gem_of_vitality}",
        null,
        null,
        null,
        null,
        "64kInvSmallGemRed",
        1L,
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
        41875,
        5,
        500,
        null,
        "0",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10784, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
