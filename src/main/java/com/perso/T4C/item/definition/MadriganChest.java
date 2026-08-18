package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class MadriganChest {
  private MadriganChest() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.madrigan_chest",
        "${item.madrigan_chest}",
        null,
        null,
        null,
        null,
        "64kInvMisc 2 - All 1",
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
        41599,
        3,
        41,
        null,
        "0",
        0,
        0,
        true,
        "Madrigan Key",
        250,
        null,
        544,
        2000,
        3000,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Madrigan hat of camouflage",
                    "Helmet of the Clairvoyant",
                    "Straight jacket 1",
                    "Ring of the assassin",
                    "Rusted short sword",
                    "Rusted long sword",
                    "Polished short sword",
                    "Polished long sword",
                    "Ashwood longbow",
                    "Ashwood reflex bow",
                    "Elm flatbow",
                    "Elm longbow",
                    "Elm reflex bow",
                    "Elm recurve bow",
                    "Rusted hand axe")),
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Potion of partial fire resistance",
                    "Potion of partial water resistance",
                    "Potion of partial earth resistance",
                    "Potion of partial air resistance",
                    "Potion of partial protection from evil",
                    "Potion of tranquility",
                    "Potion of clear thought",
                    "Potion of fortitude",
                    "Potion of nimbleness",
                    "Light healing potion",
                    "Healing potion",
                    "Serious healing potion",
                    "Critical healing potion",
                    "Deific healing potion",
                    "Potion of mana",
                    "Mana elixir",
                    "Mana prism",
                    "Manastone",
                    "Potion of fury"))),
        false);
  }
}
