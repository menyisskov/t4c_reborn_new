package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AmuletteDuTruand {
  private AmuletteDuTruand() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.amulette_du_truand",
        "${item.amulette_du_truand}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 2",
        0L,
        1L,
        10.0d,
        0L,
        35L,
        0L,
        80L,
        63L,
        40L,
        40L,
        1.0d,
        false,
        false,
        false,
        3368,
        2,
        173,
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
