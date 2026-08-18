package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ExperiencedArcherBack {
  private ExperiencedArcherBack() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.experienced_archer_back",
        "${item.experienced_archer_back}",
        BodyPart.BACK,
        "PupRedCape__pal6",
        null,
        null,
        "64kInvRedCape__pal6",
        2000L,
        0L,
        8.0d,
        0L,
        50L,
        0L,
        50L,
        150L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        3283,
        2,
        655,
        null,
        null,
        0,
        -1,
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
