package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ChainmailCoif {
  private ChainmailCoif() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.chainmail_coif",
        "${item.chainmail_coif}",
        BodyPart.HEAD,
        "PupChainMailCoif",
        null,
        null,
        "64kInvChainMailHelm",
        2223L,
        6L,
        1.69d,
        5L,
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
        40201,
        2,
        270,
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
