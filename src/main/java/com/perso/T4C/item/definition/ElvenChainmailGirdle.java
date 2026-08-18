package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ElvenChainmailGirdle {
  private ElvenChainmailGirdle() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.elven_chainmail_girdle",
        "${item.elven_chainmail_girdle}",
        BodyPart.BELT,
        null,
        null,
        null,
        "64kInvBelt",
        3298L,
        4L,
        2.2d,
        3L,
        110L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40739,
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
