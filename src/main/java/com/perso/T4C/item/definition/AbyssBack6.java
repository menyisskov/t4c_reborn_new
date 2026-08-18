package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AbyssBack6 {
  private AbyssBack6() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.abyss_back6",
        "${item.abyss_back6}",
        BodyPart.BACK,
        "NM_DechuWings",
        null,
        null,
        "Inv_NMDechuWings",
        2000000L,
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
        3658,
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
