package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class SacrificeShield {
  private SacrificeShield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.sacrifice_shield",
        "${item.sacrifice_shield}",
        BodyPart.SHIELD,
        "PupSkavenShield2",
        null,
        null,
        "64kInvSkavenShield2",
        500000L,
        2L,
        35.0d,
        0L,
        150L,
        0L,
        0L,
        310L,
        310L,
        0L,
        1.0d,
        false,
        false,
        true,
        3931,
        2,
        466,
        null,
        null,
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
