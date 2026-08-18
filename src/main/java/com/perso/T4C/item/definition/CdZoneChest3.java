package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class CdZoneChest3 {
  private CdZoneChest3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cd_zone_chest_3",
        "${item.cd_zone_chest_3}",
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
        41164,
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
        548,
        1100,
        1600,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Light healing potion",
                    "Elven leather boots",
                    "Fine steel hand axe",
                    "High metal short sword",
                    "Mithril chainmail boots",
                    "Light healing potion",
                    "Hickory recurve bow",
                    "Elm recurve bow",
                    "Serious healing potion",
                    "Rough ruby",
                    "Mithril blade",
                    "Potion of tranquility",
                    "Cat claw ornated staff",
                    "Plate gauntlets"))),
        false);
  }
}
