package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemTwistedLongSword {
  private ItemItemTwistedLongSword() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.twisted_long_sword",
        "${item.twisted_long_sword}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvNormalSword",
        0L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        75L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40860,
        1,
        2,
        "1d16+26",
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
        List.of(new ItemDefinition.ItemBoost(560, 10008, "25", 0, 0)),
        List.of(),
        false);
  }
}
