package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAnneauArchi {
  private ItemItemAnneauArchi() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.anneau_archi",
        "${item.anneau_archi}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 1",
        100000L,
        1L,
        15.0d,
        0L,
        300L,
        0L,
        0L,
        0L,
        400L,
        75L,
        1.0d,
        false,
        false,
        false,
        3565,
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
