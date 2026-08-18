package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AirKey {
  private AirKey() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.air_key",
        "${item.air_key}",
        BodyPart.BODY,
        null,
        null,
        null,
        "64kInvMisc 1 - Part 1 6",
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
        true,
        false,
        true,
        3306,
        6,
        32,
        null,
        null,
        0,
        0,
        false,
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
