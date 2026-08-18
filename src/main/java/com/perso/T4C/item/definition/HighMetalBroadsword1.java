package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class HighMetalBroadsword1 {
  private HighMetalBroadsword1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.high_metal_broadsword_1",
        "${item.high_metal_broadsword_1}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvNormalSword",
        75442L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        213L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40461,
        1,
        2,
        "1d48+88",
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
        List.of(new ItemDefinition.ItemBoost(307, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
