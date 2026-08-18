package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemPolishedBroadsword3 {
  private ItemItemPolishedBroadsword3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.polished_broadsword_3",
        "${item.polished_broadsword_3}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvNormalSword",
        19372L,
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
        40384,
        1,
        2,
        "1d25+41",
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
        List.of(new ItemDefinition.ItemBoost(294, 8, "self.true_attack*50/100", 0, 0)),
        List.of(),
        false);
  }
}
