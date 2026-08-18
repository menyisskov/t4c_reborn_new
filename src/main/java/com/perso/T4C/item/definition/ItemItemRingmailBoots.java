package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRingmailBoots {
  private ItemItemRingmailBoots() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ringmail_boots",
        "${item.ringmail_boots}",
        BodyPart.FEET,
        "PupBlackLeatherBoots",
        null,
        null,
        "64kInvBlackLeatherBoots",
        983L,
        6L,
        1.215d,
        4L,
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
        40504,
        2,
        288,
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
