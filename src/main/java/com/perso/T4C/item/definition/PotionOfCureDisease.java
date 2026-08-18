package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class PotionOfCureDisease {
  private PotionOfCureDisease() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.potion_of_cure_disease",
        "${item.potion_of_cure_disease}",
        null,
        null,
        null,
        null,
        "64kInvPotions 4",
        66L,
        1L,
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
        41411,
        5,
        254,
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
        List.of(new ItemDefinition.ItemSpell(10395, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
