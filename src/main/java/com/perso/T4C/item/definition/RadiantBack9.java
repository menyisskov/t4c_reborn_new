package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RadiantBack9 {
  private RadiantBack9() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.radiant_back9",
        "${item.radiant_back9}",
        BodyPart.BACK,
        "NMS_nDechuWings",
        null,
        null,
        "Inv_NMS_nDechuWings",
        2000000L,
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
        3751,
        2,
        919,
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
