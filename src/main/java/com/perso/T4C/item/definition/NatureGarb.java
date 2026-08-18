package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class NatureGarb {
  private NatureGarb() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.nature_garb",
        "${item.nature_garb}",
        BodyPart.BODY,
        "PupMageRobe",
        null,
        null,
        "64kInvMageRobe",
        8747L,
        5L,
        10.0d,
        25L,
        20L,
        0L,
        0L,
        0L,
        15L,
        115L,
        0.0d,
        false,
        false,
        false,
        40548,
        2,
        424,
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
        List.of(new ItemDefinition.ItemBoost(516, 4, "40", 0, 0)),
        List.of(),
        false);
  }
}
