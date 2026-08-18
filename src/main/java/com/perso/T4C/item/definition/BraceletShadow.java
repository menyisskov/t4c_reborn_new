package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class BraceletShadow {
  private BraceletShadow() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.bracelet_shadow",
        "${item.bracelet_shadow}",
        BodyPart.BRACER,
        null,
        null,
        null,
        "64kInvBracelet",
        100000L,
        1L,
        15.0d,
        0L,
        300L,
        0L,
        0L,
        0L,
        400L,
        75L,
        1.0d,
        false,
        false,
        false,
        3556,
        2,
        237,
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
