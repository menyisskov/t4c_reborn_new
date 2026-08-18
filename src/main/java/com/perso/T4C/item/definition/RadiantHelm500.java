package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RadiantHelm500 {
  private RadiantHelm500() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.radiant_helm_500",
        "${item.radiant_helm_500}",
        BodyPart.HEAD,
        "WitchHat5",
        null,
        null,
        "inv_WitchHat5",
        500000L,
        2L,
        50.0d,
        0L,
        500L,
        0L,
        0L,
        0L,
        75L,
        400L,
        1.0d,
        false,
        false,
        true,
        4126,
        2,
        878,
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
