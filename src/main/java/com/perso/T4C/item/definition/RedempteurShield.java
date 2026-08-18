package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RedempteurShield {
  private RedempteurShield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.redempteur_shield",
        "${item.redempteur_shield}",
        BodyPart.SHIELD,
        "V2_Shield01",
        null,
        null,
        "Inv_V2_Shield01",
        500000L,
        2L,
        45.0d,
        0L,
        300L,
        0L,
        200L,
        0L,
        75L,
        250L,
        1.0d,
        false,
        false,
        true,
        3612,
        2,
        871,
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
