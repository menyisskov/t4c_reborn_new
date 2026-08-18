package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class OracleBonusChest3 {
  private OracleBonusChest3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.oracle_bonus_chest_3",
        "${item.oracle_bonus_chest_3}",
        null,
        null,
        null,
        null,
        "64kInvMisc 2 - All 1",
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
        41496,
        3,
        41,
        null,
        "0",
        0,
        0,
        true,
        null,
        0,
        null,
        0,
        14400,
        28800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Arcane circlet of power",
                    "Runed malachite diadem",
                    "Golden emerald encrusted tiara",
                    "Critical healing potion",
                    "Critical healing potion",
                    "Critical healing potion",
                    "Deific healing potion",
                    "Deific healing potion",
                    "Manastone",
                    "Manastone",
                    "Mana prism",
                    "Mana prism",
                    "Mystic headband of the wind"))),
        false);
  }
}
