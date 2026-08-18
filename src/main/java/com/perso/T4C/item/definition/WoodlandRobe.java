package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class WoodlandRobe {
  private WoodlandRobe() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.woodland_robe",
        "${item.woodland_robe}",
        BodyPart.BODY,
        "PupMageRobe",
        null,
        null,
        "64kInvMageRobe",
        449L,
        5L,
        2.0d,
        25L,
        12L,
        0L,
        0L,
        0L,
        28L,
        28L,
        0.0d,
        false,
        false,
        false,
        41143,
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
        List.of(
            new ItemDefinition.ItemBoost(597, 1, "5", 0, 0),
            new ItemDefinition.ItemBoost(598, 4, "5", 0, 0)),
        List.of(),
        false);
  }
}
