package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class OracleBonusChest2 {
  private OracleBonusChest2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.oracle_bonus_chest_2",
        "${item.oracle_bonus_chest_2}",
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
        41495,
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
                    "High clerist robes",
                    "Cloak of renewal",
                    "Mantle of death",
                    "Mana elixir",
                    "Mana elixir",
                    "Mana elixir",
                    "Mana elixir",
                    "Manastone",
                    "Manastone",
                    "Manastone",
                    "Mana prism",
                    "Mana prism",
                    "Mana prism",
                    "Robe of the Arch Magi")),
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Ruby power focus",
                    "Sapphire power focus",
                    "Diamond power focus",
                    "Emerald power focus",
                    "Mana elixir",
                    "Mana elixir",
                    "Mana elixir",
                    "Mana elixir",
                    "Manastone",
                    "Manastone",
                    "Manastone",
                    "Mana prism",
                    "Mana prism",
                    "Mana prism"))),
        false);
  }
}
