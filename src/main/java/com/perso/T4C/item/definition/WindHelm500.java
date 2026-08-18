package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class WindHelm500 {
  private WindHelm500() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.wind_helm_500",
        "${item.wind_helm_500}",
        BodyPart.HEAD,
        "PupGoldenCrown",
        null,
        null,
        "64kInvGoldenCrown",
        500000L,
        2L,
        50.0d,
        0L,
        500L,
        0L,
        0L,
        0L,
        125L,
        350L,
        1.0d,
        false,
        false,
        true,
        4144,
        2,
        279,
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
