package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CollierShadow {
  private CollierShadow() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.collier_shadow",
        "${item.collier_shadow}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 2",
        100000L,
        1L,
        15.0d,
        0L,
        50L,
        0L,
        0L,
        0L,
        300L,
        75L,
        1.0d,
        false,
        false,
        false,
        3544,
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
