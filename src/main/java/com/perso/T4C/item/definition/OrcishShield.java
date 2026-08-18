package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class OrcishShield {
  private OrcishShield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.orcish_shield",
        "${item.orcish_shield}",
        BodyPart.SHIELD,
        "PupOrcShield",
        null,
        null,
        "64kInvOrcShield",
        5088L,
        9L,
        4.29d,
        13L,
        80L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40212,
        2,
        282,
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
