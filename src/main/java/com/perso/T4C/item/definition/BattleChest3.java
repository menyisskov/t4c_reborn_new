package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class BattleChest3 {
  private BattleChest3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.battle_chest_3",
        "${item.battle_chest_3}",
        null,
        null,
        null,
        null,
        "64kInvChest",
        0L,
        10000L,
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
        41472,
        3,
        20,
        null,
        "0",
        0,
        0,
        true,
        null,
        0,
        null,
        431,
        1200,
        2200,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Bracers of battle",
                    "Raw crystal",
                    "Sapphire hilted rapier",
                    "Mana elixir",
                    "Manastone",
                    "Scroll of recall",
                    "Potion of fury",
                    "Potion of regeneration",
                    "Potion of nimbleness",
                    "Potion of fortitude",
                    "Potion of clear thought",
                    "Potion of tranquility",
                    "Crude skraug bow",
                    "Mana elixir",
                    "Manastone",
                    "Scroll of recall",
                    "Potion of fury"))),
        false);
  }
}
