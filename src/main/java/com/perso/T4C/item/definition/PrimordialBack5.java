package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class PrimordialBack5 {
  private PrimordialBack5() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.primordial_back5",
        "${item.primordial_back5}",
        BodyPart.BACK,
        "ButterFlyWing__pal4",
        null,
        null,
        "Inv_ButterFlyWing__pal4",
        2000000L,
        2L,
        40.0d,
        0L,
        300L,
        0L,
        0L,
        300L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        3729,
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
