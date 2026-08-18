package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAmuletOfIronwill {
  private ItemItemAmuletOfIronwill() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.amulet_of_ironwill",
        "${item.amulet_of_ironwill}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 1",
        2439L,
        1L,
        1.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        22L,
        23L,
        0.0d,
        false,
        false,
        false,
        40051,
        2,
        172,
        null,
        "0",
        0,
        0,
        true,
        null,
        5,
        null,
        0,
        0,
        0,
        List.of(),
        List.of(new ItemDefinition.ItemBoost(21, 5, "5", 0, 0)),
        List.of(),
        false);
  }
}
