package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class StonecrestCryptChest1 {
  private StonecrestCryptChest1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.stonecrest_crypt_chest_1",
        "${item.stonecrest_crypt_chest_1}",
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
        41354,
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
        282,
        1200,
        2400,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Scalemail leggings",
                    "Critical healing potion",
                    "Rough amethyst",
                    "Scroll of recall",
                    "Elm recurve bow",
                    "Elven leather helmet",
                    "Hickory flatbow",
                    "Elven leather belt",
                    "Potion of clear thought",
                    "High metal flail",
                    "Serious healing potion",
                    "Scalemail leggings",
                    "Critical healing potion",
                    "Rough amethyst",
                    "Scroll of recall")),
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Light healing potion",
                    "Healing potion",
                    "Serious healing potion",
                    "Critical healing potion",
                    "Scroll of recall",
                    "Potion of cure rabies",
                    "Potion of cure poison",
                    "Potion of cure disease",
                    "Potion of fury",
                    "Potion of tranquility",
                    "Potion of clear thought",
                    "Potion of fortitude",
                    "Potion of nimbleness",
                    "Torch"))),
        false);
  }
}
