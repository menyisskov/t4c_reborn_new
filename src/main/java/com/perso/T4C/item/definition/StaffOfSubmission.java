package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class StaffOfSubmission {
  private StaffOfSubmission() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.staff_of_submission",
        "${item.staff_of_submission}",
        BodyPart.WEAPON,
        "PupGemStaff",
        null,
        null,
        "64kInvGemStaff",
        0L,
        0L,
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
        40933,
        1,
        295,
        null,
        "if(1500-self.agi/250*1500/2<1500/2?1500/2:1500-self.agi/250*1500/2)+1d750",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10260, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
