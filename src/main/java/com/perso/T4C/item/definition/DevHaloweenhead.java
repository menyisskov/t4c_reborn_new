package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class DevHaloweenhead {
  private DevHaloweenhead() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dev_haloweenhead",
        "${item.dev_haloweenhead}",
        BodyPart.HEAD,
        "HalloweenHead",
        null,
        null,
        "Inv_HalloweenHead",
        7999L,
        3L,
        1.56d,
        2L,
        75L,
        0L,
        0L,
        50L,
        16L,
        12L,
        1.0d,
        false,
        false,
        false,
        3228,
        2,
        1078,
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
