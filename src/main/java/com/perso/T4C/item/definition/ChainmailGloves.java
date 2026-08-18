package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ChainmailGloves {
  private ChainmailGloves() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.chainmail_gloves",
        "${item.chainmail_gloves}",
        BodyPart.LEFT_HAND,
        "PupLeatherGloveL",
        BodyPart.RIGHT_HAND,
        "PupLeatherGloveR",
        "64kInvLeatherArmorGloves",
        2055L,
        6L,
        1.755d,
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
        40200,
        2,
        259,
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
