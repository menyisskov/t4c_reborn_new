package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemStonecrestCryptChest2 {
  private ItemItemStonecrestCryptChest2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.stonecrest_crypt_chest_2",
        "${item.stonecrest_crypt_chest_2}",
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
        41355,
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
        361,
        1200,
        2400,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Elm recurve bow",
                    "Elven leather helmet",
                    "Scroll of recall",
                    "Potion of tranquility",
                    "Hickory flatbow",
                    "Elven leather boots",
                    "Fine steel scimitar",
                    "Light healing potion",
                    "Elm reflex bow",
                    "Potion of clear thought",
                    "High metal short sword",
                    "Mana elixir",
                    "Manastone",
                    "Potion of mana"))),
        false);
  }
}
