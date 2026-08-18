package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class VialOfSpiderVenom {
  private VialOfSpiderVenom() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.vial_of_spider_venom",
        "${item.vial_of_spider_venom}",
        null,
        null,
        null,
        null,
        "64kInvPotions 7",
        10L,
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
        40222,
        5,
        257,
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
        List.of(new ItemDefinition.ItemSpell(10100, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
