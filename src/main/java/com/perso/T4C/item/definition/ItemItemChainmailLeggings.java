package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemChainmailLeggings {
  private ItemItemChainmailLeggings() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.chainmail_leggings",
        "${item.chainmail_leggings}",
        BodyPart.LEGS,
        "PupChainMailLegs",
        null,
        null,
        "64kInvChainMailLegs",
        2331L,
        9L,
        1.95d,
        6L,
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
        40202,
        2,
        268,
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
