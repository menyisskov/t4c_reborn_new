package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemOakFlatbow {
  private ItemItemOakFlatbow() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.oak_flatbow",
        "${item.oak_flatbow}",
        BodyPart.WEAPON,
        "PupShortBow",
        null,
        null,
        "64kInvShortBow",
        37721L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        31L,
        213L,
        0L,
        0L,
        1000.0d,
        false,
        true,
        false,
        41213,
        9,
        86,
        "1d31+69+arrow_dmg",
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
        List.of(),
        List.of(),
        false);
  }
}
