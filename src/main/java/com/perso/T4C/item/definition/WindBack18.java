package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class WindBack18 {
  private WindBack18() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.wind_back18",
        "${item.wind_back18}",
        BodyPart.BACK,
        "NightmareMedWings",
        null,
        null,
        "Inv_NightMareWings",
        2000000L,
        2L,
        25.0d,
        0L,
        300L,
        0L,
        0L,
        0L,
        125L,
        350L,
        1.0d,
        false,
        false,
        true,
        3850,
        2,
        1097,
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
