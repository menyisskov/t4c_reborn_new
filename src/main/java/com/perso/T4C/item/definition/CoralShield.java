package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CoralShield {
  private CoralShield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.coral_shield",
        "${item.coral_shield}",
        BodyPart.SHIELD,
        "PupBarossaShield",
        null,
        null,
        "64kInvBarossaShield",
        49634L,
        12L,
        16.83d,
        30L,
        180L,
        0L,
        0L,
        0L,
        90L,
        65L,
        0.0d,
        false,
        false,
        false,
        40578,
        2,
        273,
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
        List.of(new ItemDefinition.ItemBoost(467, 13, "50", 0, 0)),
        List.of(),
        false);
  }
}
