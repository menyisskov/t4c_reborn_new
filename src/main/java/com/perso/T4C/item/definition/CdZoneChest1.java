package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class CdZoneChest1 {
  private CdZoneChest1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cd_zone_chest_1",
        "${item.cd_zone_chest_1}",
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
        41162,
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
        434,
        800,
        1400,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Cord of treachery",
                    "Light healing potion",
                    "Potion of fury",
                    "Hickory compound bow",
                    "Hickory recurve bow",
                    "Potion of cure rabies",
                    "Mithril dagger",
                    "Large shield",
                    "Scroll of recall",
                    "Elven chainmail gauntlets",
                    "Oak flatbow",
                    "Plate boots",
                    "Plate helmet"))),
        false);
  }
}
