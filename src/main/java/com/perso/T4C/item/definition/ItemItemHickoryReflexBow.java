package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemHickoryReflexBow {
  private ItemItemHickoryReflexBow() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.hickory_reflex_bow",
        "${item.hickory_reflex_bow}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow",
        19273L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        31L,
        155L,
        0L,
        0L,
        1000.0d,
        false,
        true,
        false,
        41203,
        9,
        421,
        "1d25+54+3*arrow_dmg/2",
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
