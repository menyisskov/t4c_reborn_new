package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemPolishedBroadsword1 {
  private ItemItemPolishedBroadsword1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.polished_broadsword_1",
        "${item.polished_broadsword_1}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvNormalSword",
        9686L,
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
        40339,
        1,
        2,
        "1d20+31",
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
        List.of(new ItemDefinition.ItemBoost(292, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
