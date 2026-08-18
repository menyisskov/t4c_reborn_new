package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AnneauDesArcanes {
  private AnneauDesArcanes() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.anneau_des_arcanes",
        "${item.anneau_des_arcanes}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 1",
        50000L,
        1L,
        15.0d,
        0L,
        50L,
        0L,
        0L,
        0L,
        300L,
        75L,
        1.0d,
        false,
        false,
        false,
        3375,
        2,
        176,
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
