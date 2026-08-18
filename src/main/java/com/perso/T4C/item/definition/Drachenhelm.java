package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Drachenhelm {
  private Drachenhelm() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.drachenhelm",
        "${item.drachenhelm}",
        BodyPart.HEAD,
        "PupShamanHelm",
        null,
        null,
        "64kInvShamanHelm",
        0L,
        2L,
        10.0d,
        0L,
        10L,
        0L,
        0L,
        0L,
        70L,
        50L,
        0.0d,
        false,
        false,
        false,
        41661,
        2,
        462,
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
        List.of(new ItemDefinition.ItemBoost(942, 9, "50", 0, 0)),
        List.of(),
        false);
  }
}
