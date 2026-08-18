package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class StonecrestCryptChest3 {
  private StonecrestCryptChest3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.stonecrest_crypt_chest_3",
        "${item.stonecrest_crypt_chest_3}",
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
        41356,
        3,
        20,
        null,
        "0",
        0,
        0,
        true,
        "Blackened iron key",
        75,
        null,
        321,
        1000,
        2000,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Hickory recurve bow",
                    "Potion of fury",
                    "Scroll of recall",
                    "Scalemail protector",
                    "Fine steel mace",
                    "Rough moonstone",
                    "Elven leather belt",
                    "Potion of nimbleness",
                    "Large shield",
                    "Fine steel hand axe",
                    "Potion of clear thought",
                    "Potion of cure disease",
                    "Plate gauntlets",
                    "Ring of the berserker")),
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Light healing potion",
                    "Healing potion",
                    "Serious healing potion",
                    "Critical healing potion",
                    "Potion of fury",
                    "Potion of nimbleness",
                    "Potion of clear thought",
                    "Potion of tranquility",
                    "Potion of fortitude",
                    "Potion of cure disease",
                    "Potion of cure poison",
                    "Potion of cure rabies",
                    "Scroll of recall"))),
        false);
  }
}
