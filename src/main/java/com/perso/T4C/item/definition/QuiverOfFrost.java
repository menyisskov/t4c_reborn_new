package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class QuiverOfFrost {
  private QuiverOfFrost() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.quiver_of_frost",
        "${item.quiver_of_frost}",
        BodyPart.WEAPON2,
        null,
        null,
        null,
        "64kIconQuiver",
        22000L,
        3L,
        0.0d,
        0L,
        0L,
        0L,
        50L,
        100L,
        0L,
        0L,
        1.0d,
        false,
        true,
        true,
        3290,
        8,
        451,
        "1d12",
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
