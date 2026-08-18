package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MithrilBroadsword2 {
  private MithrilBroadsword2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_broadsword_2",
        "${item.mithril_broadsword_2}",
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
        40375,
        1,
        2,
        "1d72+136",
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
        List.of(new ItemDefinition.ItemBoost(317, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
