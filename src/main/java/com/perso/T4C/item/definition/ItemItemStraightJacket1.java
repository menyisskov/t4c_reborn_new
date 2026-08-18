package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemStraightJacket1 {
  private ItemItemStraightJacket1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.straight_jacket_1",
        "${item.straight_jacket_1}",
        BodyPart.BODY,
        "PupBodyClothSet1",
        null,
        null,
        "64kInvClothSet1Body",
        1513L,
        9L,
        2.8d,
        10L,
        45L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        41621,
        2,
        285,
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
            new ItemDefinition.ItemBoost(927, 10009, "5", 0, 0),
            new ItemDefinition.ItemBoost(928, 10029, "5", 0, 0),
            new ItemDefinition.ItemBoost(929, 3, "5", 0, 0),
            new ItemDefinition.ItemBoost(930, 9, "-25", 0, 0),
            new ItemDefinition.ItemBoost(931, 8, "-25", 0, 0)),
        List.of(),
        false);
  }
}
