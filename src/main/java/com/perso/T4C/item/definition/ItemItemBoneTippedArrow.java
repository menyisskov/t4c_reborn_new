package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemBoneTippedArrow {
  private ItemItemBoneTippedArrow() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.bone_tipped_arrow",
        "${item.bone_tipped_arrow}",
        BodyPart.WEAPON2,
        null,
        null,
        null,
        "64kInvQuiver",
        5000L,
        3L,
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
        true,
        true,
        41172,
        8,
        422,
        "1d3",
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
        List.of(),
        List.of(),
        false);
  }
}
