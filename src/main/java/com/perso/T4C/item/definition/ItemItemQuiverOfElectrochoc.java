package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemQuiverOfElectrochoc {
  private ItemItemQuiverOfElectrochoc() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.quiver_of_electrochoc",
        "${item.quiver_of_electrochoc}",
        BodyPart.WEAPON2,
        null,
        null,
        null,
        "64kIconQuiver",
        52000L,
        3L,
        0.0d,
        0L,
        0L,
        0L,
        100L,
        500L,
        0L,
        0L,
        1.0d,
        false,
        true,
        true,
        3437,
        8,
        452,
        "1d50+100",
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
