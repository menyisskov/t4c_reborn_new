package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemIronStaff1 {
  private ItemItemIronStaff1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.iron_staff_1",
        "${item.iron_staff_1}",
        BodyPart.WEAPON,
        "PupWoodenStaff",
        null,
        null,
        "64kInvWoodenStaff",
        261L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        12L,
        0L,
        21L,
        0L,
        1.0d,
        false,
        false,
        false,
        40658,
        1,
        118,
        "1d6+4",
        "if(1312-self.agi/250*1312/2<1312/2?1312/2:1312-self.agi/250*1312/2)+1d656",
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
        List.of(new ItemDefinition.ItemBoost(401, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
