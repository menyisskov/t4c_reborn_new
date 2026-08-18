package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Tetsubo1 {
  private Tetsubo1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.tetsubo_1",
        "${item.tetsubo_1}",
        BodyPart.WEAPON,
        "PupWoodenStaff",
        null,
        null,
        "64kInvWoodenStaff",
        6215L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        17L,
        0L,
        68L,
        0L,
        1.0d,
        false,
        false,
        false,
        40673,
        1,
        118,
        "1d14+23",
        "if(1687-self.agi/250*1687/2<1687/2?1687/2:1687-self.agi/250*1687/2)+1d844",
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
        List.of(new ItemDefinition.ItemBoost(411, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
