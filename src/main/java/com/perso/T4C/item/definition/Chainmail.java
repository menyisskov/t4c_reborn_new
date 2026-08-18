package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Chainmail {
  private Chainmail() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.chainmail",
        "${item.chainmail}",
        BodyPart.BODY,
        "PupChainMailBody",
        null,
        null,
        "64kInvChainMailBody",
        6926L,
        12L,
        5.95d,
        18L,
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
        40030,
        2,
        269,
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
