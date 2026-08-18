package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ArStone {
  private ArStone() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ar_stone",
        "${item.ar_stone}",
        BodyPart.BODY,
        null,
        null,
        null,
        "64kInvVolcanoRock",
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
        true,
        3468,
        6,
        204,
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
