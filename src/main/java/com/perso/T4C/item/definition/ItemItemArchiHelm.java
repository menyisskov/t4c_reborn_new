package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemArchiHelm {
  private ItemItemArchiHelm() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.archi_helm",
        "${item.archi_helm}",
        BodyPart.HEAD,
        "WitchHat3",
        null,
        null,
        "inv_WitchHat3",
        500000L,
        2L,
        25.0d,
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
        true,
        3473,
        2,
        660,
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
