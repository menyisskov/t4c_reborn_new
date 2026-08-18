package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class WoodenShield {
  private WoodenShield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.wooden_shield",
        "${item.wooden_shield}",
        BodyPart.SHIELD,
        "PupOrcShield",
        null,
        null,
        "64kInvOrcShield",
        1118L,
        8L,
        1.98d,
        7L,
        45L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40175,
        2,
        282,
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
