package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CollierDeTorpeur {
  private CollierDeTorpeur() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.collier_de_torpeur",
        "${item.collier_de_torpeur}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 1",
        50000L,
        1L,
        15.0d,
        0L,
        350L,
        0L,
        100L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        3370,
        2,
        172,
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
