package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ShamanHelm {
  private ShamanHelm() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.shaman_helm",
        "${item.shaman_helm}",
        BodyPart.HEAD,
        "PupShamanHelm",
        null,
        null,
        "64kInvShamanHelm",
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
        41629,
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
        List.of(),
        List.of(),
        false);
  }
}
