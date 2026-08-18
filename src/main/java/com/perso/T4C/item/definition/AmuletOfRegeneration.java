package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AmuletOfRegeneration {
  private AmuletOfRegeneration() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.amulet_of_regeneration",
        "${item.amulet_of_regeneration}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 2",
        1L,
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
        41864,
        2,
        173,
        null,
        "0",
        0,
        -1,
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
