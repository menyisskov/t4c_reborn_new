package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class PolishedLongSword1 {
  private PolishedLongSword1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.polished_long_sword_1",
        "${item.polished_long_sword_1}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvNormalSword",
        6215L,
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
        40409,
        1,
        2,
        "1d15+24",
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
        List.of(new ItemDefinition.ItemBoost(290, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
