package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class DarkShield {
  private DarkShield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dark_shield",
        "${item.dark_shield}",
        BodyPart.SHIELD,
        "SkShield",
        null,
        null,
        "Inv_SkShield",
        0L,
        10L,
        22.0d,
        0L,
        190L,
        600L,
        150L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        3189,
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
