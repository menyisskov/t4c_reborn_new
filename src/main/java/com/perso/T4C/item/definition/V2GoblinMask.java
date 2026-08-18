package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class V2GoblinMask {
  private V2GoblinMask() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.v2_goblin_mask",
        "${item.v2_goblin_mask}",
        BodyPart.MASK,
        "PupGobmask",
        null,
        null,
        "InvGobMask",
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
        3028,
        2,
        565,
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
