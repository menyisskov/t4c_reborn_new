package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MithrilSword {
  private MithrilSword() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_sword",
        "${item.mithril_sword}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvNormalSword",
        3107L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        68L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40138,
        1,
        2,
        "1d13+21",
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
        List.of(),
        List.of(),
        false);
  }
}
