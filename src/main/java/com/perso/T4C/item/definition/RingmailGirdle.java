package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RingmailGirdle {
  private RingmailGirdle() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ringmail_girdle",
        "${item.ringmail_girdle}",
        BodyPart.BELT,
        null,
        null,
        null,
        "64kInvBelt",
        769L,
        4L,
        0.9d,
        3L,
        60L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40735,
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
