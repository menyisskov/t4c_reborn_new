package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ScalemailLeggings {
  private ScalemailLeggings() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.scalemail_leggings",
        "${item.scalemail_leggings}",
        BodyPart.LEGS,
        "PupChainMailLegs",
        null,
        null,
        "64kInvChainMailLegs",
        3956L,
        6L,
        2.85d,
        5L,
        100L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40431,
        2,
        268,
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
