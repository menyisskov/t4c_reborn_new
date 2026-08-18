package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemElvenChainmailArmor {
  private ItemItemElvenChainmailArmor() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.elven_chainmail_armor",
        "${item.elven_chainmail_armor}",
        BodyPart.BODY,
        "PupChainMailBody",
        null,
        null,
        "64kInvChainMailBody",
        14725L,
        10L,
        10.0d,
        12L,
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
        40433,
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
        List.of(
            new ItemDefinition.ItemBoost(156, 12, "2", 0, 0),
            new ItemDefinition.ItemBoost(157, 22, "2", 0, 0),
            new ItemDefinition.ItemBoost(158, 15, "2", 0, 0),
            new ItemDefinition.ItemBoost(159, 13, "2", 0, 0),
            new ItemDefinition.ItemBoost(160, 14, "2", 0, 0)),
        List.of(),
        false);
  }
}
