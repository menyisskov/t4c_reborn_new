package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAshwoodFlatbow2 {
  private ItemItemAshwoodFlatbow2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ashwood_flatbow_2",
        "${item.ashwood_flatbow_2}",
        BodyPart.WEAPON,
        "PupShortBow",
        null,
        null,
        "64kInvShortBow",
        29L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1000.0d,
        false,
        true,
        false,
        41176,
        9,
        86,
        "1d4+arrow_dmg",
        "1000",
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
        List.of(new ItemDefinition.ItemBoost(730, 10035, "true_skill(35)*30/100", 0, 0)),
        List.of(),
        false);
  }
}
