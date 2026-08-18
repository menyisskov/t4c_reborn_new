package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class DrakesTomeOfDestruction {
  private DrakesTomeOfDestruction() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.drakes_tome_of_destruction",
        "${item.drakes_tome_of_destruction}",
        BodyPart.WEAPON2,
        null,
        null,
        null,
        "64kInvMisc 3 - Part 1 1",
        0L,
        0L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        65535L,
        65535L,
        0.0d,
        false,
        false,
        false,
        40976,
        1,
        45,
        null,
        "0",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10273, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
