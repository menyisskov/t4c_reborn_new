package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemCdZoneChest4 {
  private ItemItemCdZoneChest4() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cd_zone_chest_4",
        "${item.cd_zone_chest_4}",
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
        41165,
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
        679,
        1400,
        1800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Rough ruby",
                    "High metal flail",
                    "Potion of clear thought",
                    "Mithril chainmail girdle",
                    "Dwarven platemail boots",
                    "Mithril chainmail gauntlets",
                    "Potion of fury",
                    "Mana prism",
                    "Mithril chainmail leggings",
                    "Potion of nimbleness",
                    "Light healing potion",
                    "Serious healing potion",
                    "Healing potion",
                    "Sapphire hilted rapier",
                    "Plate gauntlets"))),
        false);
  }
}
