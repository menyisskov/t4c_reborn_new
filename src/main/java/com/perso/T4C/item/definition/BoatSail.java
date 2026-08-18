package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class BoatSail {
  private BoatSail() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.boat_sail",
        "${item.boat_sail}",
        BodyPart.BODY,
        null,
        null,
        null,
        "64kInvWolfPelt",
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
        3388,
        6,
        326,
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
