package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class MordenthalDungeon1 {
  private MordenthalDungeon1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mordenthal_dungeon_1",
        "${item.mordenthal_dungeon_1}",
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
        41167,
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
        543,
        1000,
        1800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Torch",
                    "Potion of mana",
                    "Manastone",
                    "Potion of cure poison",
                    "Potion of cure disease",
                    "Potion of cure rabies",
                    "Scroll of recall",
                    "Mana elixir")),
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Fine steel scimitar",
                    "Hickory compound bow",
                    "Potion of cure disease",
                    "Potion of fortitude",
                    "Critical healing potion",
                    "Torch",
                    "High metal mace",
                    "High metal short sword",
                    "Potion of cure disease",
                    "High metal short sword",
                    "Potion of cure poison",
                    "Potion of cure disease",
                    "Potion of cure rabies",
                    "Scroll of recall"))),
        false);
  }
}
