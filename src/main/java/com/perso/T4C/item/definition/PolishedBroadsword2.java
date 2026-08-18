package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class PolishedBroadsword2 {
  private PolishedBroadsword2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.polished_broadsword_2",
        "${item.polished_broadsword_2}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvNormalSword",
        14529L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        82L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40382,
        1,
        2,
        "1d21+36",
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
        List.of(new ItemDefinition.ItemBoost(293, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
