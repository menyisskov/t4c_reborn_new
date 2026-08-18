package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RustedLongSword1 {
  private RustedLongSword1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.rusted_long_sword_1",
        "${item.rusted_long_sword_1}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvNormalSword",
        403L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        24L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40514,
        1,
        2,
        "1d6+6",
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
        List.of(new ItemDefinition.ItemBoost(285, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
