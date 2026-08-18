package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemV2GoblinMaskice {
  private ItemItemV2GoblinMaskice() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.v2_goblin_maskice",
        "${item.v2_goblin_maskice}",
        BodyPart.MASK,
        "PupGobmask__pal2",
        null,
        null,
        "InvGobMask__pal2",
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
        1.0d,
        false,
        false,
        false,
        3195,
        2,
        873,
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
        List.of(),
        List.of(),
        false);
  }
}
