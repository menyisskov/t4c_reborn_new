package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class NewbiSetBack {
  private NewbiSetBack() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.newbi_set_back",
        "${item.newbi_set_back}",
        BodyPart.BACK,
        "PupRedCape__pal8",
        null,
        null,
        "64kInvRedCape__pal8",
        2000L,
        0L,
        5.0d,
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
        3256,
        2,
        657,
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
