package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Tetsubo2 {
  private Tetsubo2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.tetsubo_2",
        "${item.tetsubo_2}",
        BodyPart.WEAPON,
        "PupWoodenStaff",
        null,
        null,
        "64kInvWoodenStaff",
        9323L,
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
        40674,
        1,
        118,
        "1d16+26",
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
        List.of(new ItemDefinition.ItemBoost(412, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
