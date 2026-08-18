package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class TitanBack5 {
  private TitanBack5() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.titan_back5",
        "${item.titan_back5}",
        BodyPart.BACK,
        "ButterFlyWing__pal4",
        null,
        null,
        "Inv_ButterFlyWing__pal4",
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
        3819,
        2,
        670,
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
