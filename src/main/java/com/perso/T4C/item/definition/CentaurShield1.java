package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CentaurShield1 {
  private CentaurShield1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.centaur_shield_1",
        "${item.centaur_shield_1}",
        BodyPart.SHIELD,
        "PupCentaurShield",
        null,
        null,
        "64kInvCentaurShield1",
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
        41627,
        2,
        460,
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
