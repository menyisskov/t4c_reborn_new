package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MonkWhiteSash {
  private MonkWhiteSash() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.monk_white_sash",
        "${item.monk_white_sash}",
        BodyPart.BELT,
        null,
        null,
        null,
        "64kInvBelt",
        1794L,
        2L,
        2.0d,
        0L,
        20L,
        0L,
        0L,
        0L,
        19L,
        36L,
        0.0d,
        false,
        false,
        false,
        41552,
        2,
        235,
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
        List.of(
            new ItemDefinition.ItemBoost(872, 3, "5", 0, 0),
            new ItemDefinition.ItemBoost(873, 23, "5", 0, 0)),
        List.of(),
        false);
  }
}
