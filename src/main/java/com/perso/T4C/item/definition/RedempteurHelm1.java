package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RedempteurHelm1 {
  private RedempteurHelm1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.redempteur_helm_1",
        "${item.redempteur_helm_1}",
        BodyPart.HEAD,
        "NM_OreilleLapin",
        null,
        null,
        "Inv_OreilleLapin",
        500000L,
        2L,
        35.0d,
        0L,
        300L,
        0L,
        200L,
        0L,
        75L,
        250L,
        1.0d,
        false,
        false,
        true,
        3875,
        2,
        1152,
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
