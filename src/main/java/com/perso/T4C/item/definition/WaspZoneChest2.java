package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class WaspZoneChest2 {
  private WaspZoneChest2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.wasp_zone_chest_2",
        "${item.wasp_zone_chest_2}",
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
        41157,
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
        637,
        1000,
        1600,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "High metal short sword",
                    "Hickory longbow",
                    "Bone tipped arrow",
                    "Hickory reflex bow",
                    "Fine steel scimitar",
                    "Oak flatbow",
                    "Healing potion",
                    "Serious healing potion",
                    "Critical healing potion",
                    "Ring of the ranger",
                    "Healing potion",
                    "Serious healing potion",
                    "Critical healing potion")),
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Healing potion",
                    "Serious healing potion",
                    "Potion of clear thought",
                    "Potion of tranquility",
                    "Potion of fury",
                    "Potion of fortitude",
                    "Potion of nimbleness",
                    "Light healing potion",
                    "Potion of mana",
                    "Mana elixir"))),
        false);
  }
}
