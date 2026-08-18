package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemCdZoneChest2 {
  private ItemItemCdZoneChest2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cd_zone_chest_2",
        "${item.cd_zone_chest_2}",
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
        41163,
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
        134,
        800,
        1400,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Bracers of battle",
                    "Critical healing potion",
                    "Potion of cure disease",
                    "Scroll of recall",
                    "High metal mace",
                    "Fine steel mace",
                    "Hickory longbow",
                    "Elven leather gloves",
                    "Fine steel dagger",
                    "Serious healing potion",
                    "Torch",
                    "Potion of tranquility",
                    "Potion of cure rabies",
                    "Hickory recurve bow",
                    "High metal broadsword"))),
        false);
  }
}
