package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRingmailArmor {
  private ItemItemRingmailArmor() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ringmail_armor",
        "${item.ringmail_armor}",
        BodyPart.BODY,
        "PupChainMailBody",
        null,
        null,
        "64kInvChainMailBody",
        3346L,
        12L,
        4.15d,
        14L,
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
        40031,
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
