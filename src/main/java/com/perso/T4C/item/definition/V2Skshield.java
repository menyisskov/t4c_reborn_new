package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class V2Skshield {
  private V2Skshield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.v2_skshield",
        "${item.v2_skshield}",
        BodyPart.SHIELD,
        "SkShield",
        null,
        null,
        "Inv_SkShield",
        96708L,
        0L,
        41.91d,
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
        3039,
        2,
        677,
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
