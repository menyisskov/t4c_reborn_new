package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemCrownedStaffOfCleverness {
  private ItemItemCrownedStaffOfCleverness() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.crowned_staff_of_cleverness",
        "${item.crowned_staff_of_cleverness}",
        BodyPart.WEAPON,
        "PupLichStaff",
        null,
        null,
        "64kInvLichStaff",
        0L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        20L,
        0L,
        175L,
        15L,
        1.0d,
        false,
        false,
        false,
        40764,
        1,
        294,
        "1d21+34",
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
        List.of(
            new ItemDefinition.ItemBoost(513, 8, "self.true_attack*15/100", 0, 0),
            new ItemDefinition.ItemBoost(514, 1, "50", 0, 0)),
        List.of(),
        false);
  }
}
