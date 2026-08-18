package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class StaffOfThorns {
  private StaffOfThorns() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.staff_of_thorns",
        "${item.staff_of_thorns}",
        BodyPart.WEAPON,
        "PupWoodenStaff",
        null,
        null,
        "64kInvWoodenStaff",
        787L,
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
        40663,
        1,
        118,
        "1d8+10",
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
        List.of(),
        List.of(),
        false);
  }
}
