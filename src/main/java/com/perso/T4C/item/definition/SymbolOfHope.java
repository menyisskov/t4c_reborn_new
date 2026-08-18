package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class SymbolOfHope {
  private SymbolOfHope() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.symbol_of_hope",
        "${item.symbol_of_hope}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 1",
        2415L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        25L,
        68L,
        0.0d,
        false,
        false,
        false,
        40909,
        2,
        172,
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
        List.of(),
        List.of(new ItemDefinition.ItemBoost(571, 23, "self.true_light *25/100", 0, 0)),
        List.of(),
        false);
  }
}
