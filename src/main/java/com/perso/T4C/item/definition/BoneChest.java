package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class BoneChest {
  private BoneChest() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.bone_chest",
        "${item.bone_chest}",
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
        41482,
        3,
        20,
        null,
        "0",
        0,
        0,
        true,
        "Chipped bone key",
        80,
        null,
        54,
        3000,
        3000,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Light healing potion",
                    "Healing potion",
                    "Serious healing potion",
                    "Critical healing potion")),
            new ItemDefinition.ContainerLootGroup(List.of("Steel safe key"))),
        false);
  }
}
