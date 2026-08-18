package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemFineSteelWarhammer1 {
  private ItemItemFineSteelWarhammer1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.fine_steel_warhammer_1",
        "${item.fine_steel_warhammer_1}",
        BodyPart.WEAPON,
        "PupWarhammer",
        null,
        null,
        "64kInvWarhammer",
        9686L,
        9L,
        0.0d,
        0L,
        0L,
        0L,
        60L,
        0L,
        0L,
        56L,
        1.0d,
        false,
        false,
        false,
        40683,
        1,
        5,
        "if(target.r_dark=5025?1d22+36:1d20+31)",
        "if(937-self.agi/250*937/2<600?600:937-self.agi/250*937/2)+1d469",
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
        List.of(new ItemDefinition.ItemBoost(421, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
