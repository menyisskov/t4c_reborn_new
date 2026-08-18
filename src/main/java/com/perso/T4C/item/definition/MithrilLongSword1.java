package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MithrilLongSword1 {
  private MithrilLongSword1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_long_sword_1",
        "${item.mithril_long_sword_1}",
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
        271L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40468,
        1,
        2,
        "1d58+108",
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
        List.of(new ItemDefinition.ItemBoost(314, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
