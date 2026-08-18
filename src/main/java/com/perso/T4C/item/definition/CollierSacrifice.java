package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CollierSacrifice {
  private CollierSacrifice() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.collier_sacrifice",
        "${item.collier_sacrifice}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 2",
        100000L,
        1L,
        15.0d,
        0L,
        150L,
        0L,
        0L,
        310L,
        310L,
        0L,
        1.0d,
        false,
        false,
        false,
        3935,
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
