package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ChainmailGirdle {
  private ChainmailGirdle() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.chainmail_girdle",
        "${item.chainmail_girdle}",
        BodyPart.BELT,
        null,
        null,
        null,
        "64kInvBelt",
        1564L,
        4L,
        1.3d,
        4L,
        80L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40737,
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
        List.of(),
        List.of(),
        false);
  }
}
