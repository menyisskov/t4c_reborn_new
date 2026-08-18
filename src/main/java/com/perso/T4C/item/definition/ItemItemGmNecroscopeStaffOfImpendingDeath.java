package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemGmNecroscopeStaffOfImpendingDeath {
  private ItemItemGmNecroscopeStaffOfImpendingDeath() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gm_necroscope_staff_of_impending_death",
        "${item.gm_necroscope_staff_of_impending_death}",
        BodyPart.WEAPON,
        "PupLichStaff",
        null,
        null,
        "64kInvLichStaff",
        0L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40728,
        1,
        294,
        null,
        "0",
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
        List.of(new ItemDefinition.ItemBoost(455, 1, "20", 0, 0)),
        List.of(),
        false);
  }
}
