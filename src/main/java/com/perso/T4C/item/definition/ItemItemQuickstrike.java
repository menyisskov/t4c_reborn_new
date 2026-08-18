package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemQuickstrike {
  private ItemItemQuickstrike() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.quickstrike",
        "${item.quickstrike}",
        BodyPart.WEAPON,
        "PupRealDarkSword",
        null,
        null,
        "64kInvRealDarkSword",
        0L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        80L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40855,
        1,
        275,
        "1d24+41",
        "600+1d300",
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
        List.of(new ItemDefinition.ItemBoost(549, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
