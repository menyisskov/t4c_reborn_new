package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemHalberd {
  private ItemItemHalberd() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.halberd",
        "${item.halberd}",
        BodyPart.WEAPON,
        "PupHalberd",
        null,
        null,
        "64kInvHalberd",
        0L,
        0L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        41683,
        1,
        498,
        "1d10+15",
        "if(750-self.agi/250*750/2<600?600:750-self.agi/250*750/2)+1d375",
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
