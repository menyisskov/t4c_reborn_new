package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class HighMetalLongSword2 {
  private HighMetalLongSword2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.high_metal_long_sword_2",
        "${item.high_metal_long_sword_2}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvNormalSword",
        97601L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        198L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40534,
        1,
        2,
        "1d48+89",
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
        List.of(new ItemDefinition.ItemBoost(306, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
