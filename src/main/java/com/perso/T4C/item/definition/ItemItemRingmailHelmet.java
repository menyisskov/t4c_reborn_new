package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRingmailHelmet {
  private ItemItemRingmailHelmet() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ringmail_helmet",
        "${item.ringmail_helmet}",
        BodyPart.HEAD,
        "PupChainMailCoif",
        null,
        null,
        "64kInvChainMailHelm",
        1086L,
        6L,
        1.17d,
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
        40552,
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
