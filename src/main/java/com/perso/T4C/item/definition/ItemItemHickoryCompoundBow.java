package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemHickoryCompoundBow {
  private ItemItemHickoryCompoundBow() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.hickory_compound_bow",
        "${item.hickory_compound_bow}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow2",
        27730L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        49L,
        184L,
        0L,
        0L,
        850.0d,
        false,
        true,
        false,
        41209,
        9,
        448,
        "1d30+68+5*arrow_dmg/2",
        "850",
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
