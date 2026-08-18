package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MithrilChainmailArmor {
  private MithrilChainmailArmor() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_chainmail_armor",
        "${item.mithril_chainmail_armor}",
        BodyPart.BODY,
        "PupChainMailBody",
        null,
        null,
        "64kInvChainMailBody",
        29662L,
        10L,
        16.75d,
        17L,
        150L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40324,
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
            new ItemDefinition.ItemBoost(181, 12, "3", 0, 0),
            new ItemDefinition.ItemBoost(182, 22, "3", 0, 0),
            new ItemDefinition.ItemBoost(183, 15, "3", 0, 0),
            new ItemDefinition.ItemBoost(184, 13, "3", 0, 0),
            new ItemDefinition.ItemBoost(185, 14, "3", 0, 0)),
        List.of(),
        false);
  }
}
