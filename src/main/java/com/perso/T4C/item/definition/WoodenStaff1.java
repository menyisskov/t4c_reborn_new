package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class WoodenStaff1 {
  private WoodenStaff1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.wooden_staff_1",
        "${item.wooden_staff_1}",
        BodyPart.WEAPON,
        "PupSimpleStaff",
        null,
        null,
        "64kInvSimpleStaff",
        19L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40656,
        1,
        296,
        "1d4+1",
        "if(1125-self.agi/250*1125/2<600?600:1125-self.agi/250*1125/2)+1d563",
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
        List.of(new ItemDefinition.ItemBoost(399, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
