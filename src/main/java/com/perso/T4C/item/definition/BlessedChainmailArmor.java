package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class BlessedChainmailArmor {
  private BlessedChainmailArmor() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.blessed_chainmail_armor",
        "${item.blessed_chainmail_armor}",
        BodyPart.BODY,
        "PupChainMailBody",
        null,
        null,
        "64kInvChainMailBody",
        9331L,
        12L,
        5.95d,
        18L,
        80L,
        0L,
        0L,
        0L,
        40L,
        53L,
        0.0d,
        false,
        false,
        false,
        40748,
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
        List.of(new ItemDefinition.ItemBoost(4, 22, "25", 0, 0)),
        List.of(),
        false);
  }
}
