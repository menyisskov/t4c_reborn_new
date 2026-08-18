package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RockShield {
  private RockShield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.rock_shield",
        "${item.rock_shield}",
        BodyPart.SHIELD,
        null,
        null,
        null,
        "InvEmeraldFocus",
        500000L,
        2L,
        35.0d,
        0L,
        300L,
        0L,
        0L,
        0L,
        75L,
        400L,
        1.0d,
        false,
        false,
        true,
        3504,
        2,
        570,
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
