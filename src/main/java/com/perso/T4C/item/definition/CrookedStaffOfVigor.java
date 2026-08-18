package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CrookedStaffOfVigor {
  private CrookedStaffOfVigor() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.crooked_staff_of_vigor",
        "${item.crooked_staff_of_vigor}",
        BodyPart.WEAPON,
        "PupWoodenStaff",
        null,
        null,
        "64kInvWoodenStaff",
        0L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        25L,
        0L,
        100L,
        20L,
        1.0d,
        false,
        false,
        false,
        40858,
        1,
        118,
        "1d19+31",
        "if(1406-self.agi/250*1406/2<1406/2?1406/2:1406-self.agi/250*1406/2)+1d703",
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
        List.of(
            new ItemDefinition.ItemBoost(552, 3, "20", 0, 0),
            new ItemDefinition.ItemBoost(553, 2, "20", 0, 0)),
        List.of(),
        false);
  }
}
