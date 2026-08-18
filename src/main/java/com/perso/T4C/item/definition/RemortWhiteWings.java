package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RemortWhiteWings {
  private RemortWhiteWings() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.remort_white_wings",
        "${item.remort_white_wings}",
        BodyPart.BACK,
        "PupSeraphWhiteWings",
        null,
        null,
        "64kInvSeraphWhiteWings",
        0L,
        0L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        41639,
        2,
        473,
        null,
        "0",
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
