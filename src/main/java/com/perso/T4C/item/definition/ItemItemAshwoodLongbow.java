package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAshwoodLongbow {
  private ItemItemAshwoodLongbow() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ashwood_longbow",
        "${item.ashwood_longbow}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow",
        201L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        13L,
        24L,
        0L,
        0L,
        1150.0d,
        false,
        true,
        false,
        41177,
        9,
        421,
        "1d6+4+5*arrow_dmg/4",
        "1150",
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
