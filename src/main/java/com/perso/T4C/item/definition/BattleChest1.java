package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class BattleChest1 {
  private BattleChest1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.battle_chest_1",
        "${item.battle_chest_1}",
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
        41470,
        3,
        238,
        null,
        "0",
        0,
        0,
        true,
        null,
        0,
        null,
        323,
        1200,
        2200,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Light healing potion",
                    "Healing potion",
                    "Serious healing potion",
                    "Potion of clear thought",
                    "Potion of fury",
                    "Potion of regeneration",
                    "Potion of nimbleness",
                    "Potion of fortitude",
                    "Potion of tranquility",
                    "Potion of mana",
                    "Manastone",
                    "Mana elixir")),
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Raw crystal",
                    "Light healing potion",
                    "Healing potion",
                    "Serious healing potion",
                    "Critical healing potion",
                    "Potion of regeneration",
                    "Potion of fury",
                    "Potion of clear thought",
                    "Potion of tranquility",
                    "Round shield",
                    "Crude skraug bow"))),
        false);
  }
}
