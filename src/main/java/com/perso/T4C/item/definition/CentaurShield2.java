package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CentaurShield2 {
  private CentaurShield2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.centaur_shield_2",
        "${item.centaur_shield_2}",
        BodyPart.SHIELD,
        "PupCentaurShield2",
        null,
        null,
        "64kInvCentaurShield2",
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
        41628,
        2,
        461,
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
