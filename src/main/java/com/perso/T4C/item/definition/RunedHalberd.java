package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RunedHalberd {
  private RunedHalberd() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.runed_halberd",
        "${item.runed_halberd}",
        BodyPart.WEAPON,
        "PupHalberd",
        null,
        null,
        "64kInvHalberd",
        0L,
        5L,
        10.0d,
        0L,
        150L,
        500L,
        425L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        41718,
        1,
        498,
        "1d178+303",
        "if(1500-self.agi/250*1500/2<1500/2?1500/2:1500-self.agi/250*1500/2)+1d750",
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
            new ItemDefinition.ItemBoost(1011, 8, "self.true_attack*30/100", 0, 0),
            new ItemDefinition.ItemBoost(1012, 10002, "10", 0, 0),
            new ItemDefinition.ItemBoost(1013, 10027, "25", 0, 0)),
        List.of(),
        false);
  }
}
