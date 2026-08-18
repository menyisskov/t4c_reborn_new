package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class AlchisyumPart {
  private AlchisyumPart() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.alchisyum_part",
        "${item.alchisyum_part}",
        null,
        null,
        null,
        null,
        "InvDebritCrystal",
        70000L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        4083,
        6,
        1184,
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
