package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class FineSteelLongSword2 {
  private FineSteelLongSword2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.fine_steel_long_sword_2",
        "${item.fine_steel_long_sword_2}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvNormalSword",
        37053L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        126L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40343,
        1,
        2,
        "1d31+54",
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
        List.of(new ItemDefinition.ItemBoost(298, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
