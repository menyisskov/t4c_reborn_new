package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Raincloak {
  private Raincloak() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.raincloak",
        "${item.raincloak}",
        BodyPart.BODY,
        "PupMageRobe",
        null,
        null,
        "64kInvMageRobe",
        0L,
        4L,
        10.0d,
        25L,
        20L,
        0L,
        0L,
        0L,
        100L,
        20L,
        0.0d,
        false,
        false,
        false,
        41672,
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
        List.of(),
        List.of(),
        false);
  }
}
