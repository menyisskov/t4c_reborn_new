package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MithrilBroadsword1 {
  private MithrilBroadsword1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_broadsword_1",
        "${item.mithril_broadsword_1}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvNormalSword",
        0L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        285L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40380,
        1,
        2,
        "1d64+120",
        "if(825-self.agi/250*825/2<600?600:825-self.agi/250*825/2)+1d413",
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
        List.of(new ItemDefinition.ItemBoost(316, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
