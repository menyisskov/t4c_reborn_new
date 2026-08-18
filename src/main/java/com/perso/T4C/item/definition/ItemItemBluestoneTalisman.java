package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemBluestoneTalisman {
  private ItemItemBluestoneTalisman() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.bluestone_talisman",
        "${item.bluestone_talisman}",
        null,
        null,
        null,
        null,
        "InvBlueStone",
        0L,
        0L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        true,
        false,
        false,
        41561,
        5,
        574,
        null,
        "0",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10487, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
