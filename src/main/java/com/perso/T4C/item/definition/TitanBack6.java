package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class TitanBack6 {
  private TitanBack6() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.titan_back6",
        "${item.titan_back6}",
        BodyPart.BACK,
        "NM_DechuWings",
        null,
        null,
        "Inv_NMDechuWings",
        2000000L,
        2L,
        51.0d,
        0L,
        500L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        3820,
        2,
        888,
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
