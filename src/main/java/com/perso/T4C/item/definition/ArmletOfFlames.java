package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ArmletOfFlames {
  private ArmletOfFlames() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.armlet_of_flames",
        "${item.armlet_of_flames}",
        BodyPart.BRACER,
        null,
        null,
        null,
        "64kInvBracelet",
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
        41860,
        2,
        237,
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
