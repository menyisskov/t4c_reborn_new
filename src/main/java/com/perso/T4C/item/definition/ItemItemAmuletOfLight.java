package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAmuletOfLight {
  private ItemItemAmuletOfLight() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.amulet_of_light",
        "${item.amulet_of_light}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 2",
        736L,
        1L,
        1.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        27L,
        28L,
        0.0d,
        false,
        false,
        false,
        40179,
        2,
        173,
        null,
        "0",
        100,
        0,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(),
        List.of(new ItemDefinition.ItemBoost(103, 11, "100", 0, 0)),
        List.of(),
        false);
  }
}
