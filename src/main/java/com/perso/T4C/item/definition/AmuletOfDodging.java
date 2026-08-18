package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AmuletOfDodging {
  private AmuletOfDodging() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.amulet_of_dodging",
        "${item.amulet_of_dodging}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 1",
        495L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        21L,
        19L,
        0.0d,
        false,
        false,
        false,
        40133,
        2,
        172,
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
        List.of(new ItemDefinition.ItemBoost(90, 9, "10", 0, 0)),
        List.of(),
        false);
  }
}
