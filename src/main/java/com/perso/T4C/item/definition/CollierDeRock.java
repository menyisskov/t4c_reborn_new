package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CollierDeRock {
  private CollierDeRock() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.collier_de_rock",
        "${item.collier_de_rock}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 2",
        50000L,
        1L,
        15.0d,
        0L,
        300L,
        0L,
        0L,
        0L,
        75L,
        400L,
        1.0d,
        false,
        false,
        false,
        3541,
        2,
        173,
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
