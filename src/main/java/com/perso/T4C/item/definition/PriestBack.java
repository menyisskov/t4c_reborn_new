package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class PriestBack {
  private PriestBack() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.priest_back",
        "${item.priest_back}",
        BodyPart.BACK,
        "PupRedCape__pal6",
        null,
        null,
        "64kInvRedCape__pal6",
        2000L,
        0L,
        15.0d,
        0L,
        50L,
        0L,
        0L,
        0L,
        75L,
        300L,
        1.0d,
        false,
        false,
        true,
        3346,
        2,
        655,
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
