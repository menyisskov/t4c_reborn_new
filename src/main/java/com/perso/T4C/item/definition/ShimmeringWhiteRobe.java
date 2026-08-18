package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ShimmeringWhiteRobe {
  private ShimmeringWhiteRobe() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.shimmering_white_robe",
        "${item.shimmering_white_robe}",
        BodyPart.BODY,
        "PupWhiteRobe",
        null,
        null,
        "64kInvWhiteRobe",
        449L,
        5L,
        2.0d,
        25L,
        12L,
        0L,
        0L,
        0L,
        20L,
        35L,
        0.0d,
        false,
        false,
        false,
        41139,
        2,
        425,
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
            new ItemDefinition.ItemBoost(580, 3, "5", 0, 0),
            new ItemDefinition.ItemBoost(581, 4, "5", 0, 0)),
        List.of(),
        false);
  }
}
