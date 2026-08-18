package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemShamanShield {
  private ItemItemShamanShield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.shaman_shield",
        "${item.shaman_shield}",
        BodyPart.SHIELD,
        "V2_Shield02",
        null,
        null,
        "Inv_V2_Shield02",
        2000L,
        0L,
        9.0d,
        0L,
        70L,
        0L,
        0L,
        185L,
        185L,
        0L,
        1.0d,
        false,
        false,
        true,
        3921,
        2,
        872,
        null,
        null,
        0,
        -1,
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
