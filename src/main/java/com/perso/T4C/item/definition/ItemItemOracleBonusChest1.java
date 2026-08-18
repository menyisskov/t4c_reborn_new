package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemOracleBonusChest1 {
  private ItemItemOracleBonusChest1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.oracle_bonus_chest_1",
        "${item.oracle_bonus_chest_1}",
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
        41494,
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
                    "Gothic shield",
                    "Dragonscale armor",
                    "Dragonscale protector",
                    "High metal bastard sword 3",
                    "Critical healing potion",
                    "Critical healing potion",
                    "Critical healing potion",
                    "Critical healing potion",
                    "Serious healing potion",
                    "Serious healing potion",
                    "Serious healing potion",
                    "Deific healing potion",
                    "Deific healing potion",
                    "Dragonscale boots")),
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Dragonscale gauntlets",
                    "Dragonscale helmet",
                    "Dragonscale leggings",
                    "Mithril blade 3",
                    "Critical healing potion",
                    "Critical healing potion",
                    "Critical healing potion",
                    "Critical healing potion",
                    "Serious healing potion",
                    "Serious healing potion",
                    "Serious healing potion",
                    "Deific healing potion",
                    "Deific healing potion"))),
        false);
  }
}
