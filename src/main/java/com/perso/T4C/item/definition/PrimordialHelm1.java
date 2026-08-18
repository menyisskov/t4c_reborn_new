package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class PrimordialHelm1 {
  private PrimordialHelm1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.primordial_helm_1",
        "${item.primordial_helm_1}",
        BodyPart.HEAD,
        "PupShamanHelm",
        null,
        null,
        "64kInvShamanHelm",
        500000L,
        2L,
        42.0d,
        0L,
        300L,
        0L,
        0L,
        300L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        3975,
        2,
        462,
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
