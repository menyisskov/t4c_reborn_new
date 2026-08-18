package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RockHelm {
  private RockHelm() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.rock_helm",
        "${item.rock_helm}",
        BodyPart.HEAD,
        "WitchHat4",
        null,
        null,
        "inv_WitchHat4",
        500000L,
        2L,
        25.0d,
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
        true,
        3502,
        2,
        877,
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
