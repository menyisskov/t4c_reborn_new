package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class PrimordialArmor2 {
  private PrimordialArmor2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.primordial_armor_2",
        "${item.primordial_armor_2}",
        BodyPart.BODY,
        "PupWhiteRobe__pal9",
        null,
        null,
        "64kInvWhiteRobe__pal9",
        500000L,
        2L,
        180.0d,
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
        3974,
        2,
        593,
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
