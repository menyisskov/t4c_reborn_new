package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemDarksword {
  private ItemItemDarksword() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.darksword",
        "${item.darksword}",
        BodyPart.WEAPON,
        "PupRealDarkSword",
        null,
        null,
        "64kInvRealDarkSword",
        51327L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        242L,
        0L,
        35L,
        45L,
        1.0d,
        false,
        false,
        false,
        40008,
        1,
        275,
        "1d45+84",
        "if(787-self.agi/250*787/2<600?600:787-self.agi/250*787/2)+1d394",
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
        List.of(new ItemDefinition.ItemBoost(51, 8, "50", 0, 0)),
        List.of(),
        false);
  }
}
