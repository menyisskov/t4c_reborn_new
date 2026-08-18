package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class BattleChest2 {
  private BattleChest2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.battle_chest_2",
        "${item.battle_chest_2}",
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
        41471,
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
        433,
        1200,
        2200,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Raw crystal",
                    "Prismatic blade",
                    "Hickory flatbow",
                    "Light healing potion",
                    "Healing potion",
                    "Mana elixir",
                    "Potion of mana",
                    "Manastone",
                    "Serious healing potion",
                    "Critical healing potion",
                    "Fine steel short sword",
                    "Polished broadsword"))),
        false);
  }
}
