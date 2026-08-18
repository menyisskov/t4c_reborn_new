package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class StaffOfThorns1 {
  private StaffOfThorns1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.staff_of_thorns_1",
        "${item.staff_of_thorns_1}",
        BodyPart.WEAPON,
        "PupWoodenStaff",
        null,
        null,
        "64kInvWoodenStaff",
        1574L,
        11L,
        0.0d,
        0L,
        0L,
        0L,
        14L,
        0L,
        39L,
        0L,
        1.0d,
        false,
        false,
        false,
        40664,
        1,
        118,
        "1d9+11",
        "if(1500-self.agi/250*1500/2<1500/2?1500/2:1500-self.agi/250*1500/2)+1d750",
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
        List.of(new ItemDefinition.ItemBoost(405, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
