package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemHerosAmulet {
  private ItemItemHerosAmulet() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.heros_amulet",
        "${item.heros_amulet}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 2",
        0L,
        1L,
        10.0d,
        0L,
        35L,
        500L,
        80L,
        63L,
        40L,
        40L,
        0.0d,
        false,
        false,
        false,
        41710,
        2,
        173,
        null,
        "0",
        0,
        0,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(),
        List.of(
            new ItemDefinition.ItemBoost(982, 8, "50", 0, 0),
            new ItemDefinition.ItemBoost(983, 10035, "50", 0, 0),
            new ItemDefinition.ItemBoost(984, 10027, "10", 0, 0),
            new ItemDefinition.ItemBoost(985, 10008, "10", 0, 0),
            new ItemDefinition.ItemBoost(986, 10002, "10", 0, 0),
            new ItemDefinition.ItemBoost(987, 10029, "10", 0, 0),
            new ItemDefinition.ItemBoost(988, 10001, "10", 0, 0)),
        List.of(),
        false);
  }
}
