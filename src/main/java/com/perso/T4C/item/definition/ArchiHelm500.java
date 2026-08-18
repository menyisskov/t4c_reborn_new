package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ArchiHelm500 {
  private ArchiHelm500() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.archi_helm_500",
        "${item.archi_helm_500}",
        BodyPart.HEAD,
        "WitchHat3",
        null,
        null,
        "inv_WitchHat3",
        500000L,
        2L,
        50.0d,
        0L,
        500L,
        0L,
        0L,
        0L,
        400L,
        75L,
        1.0d,
        false,
        false,
        true,
        4108,
        2,
        660,
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
