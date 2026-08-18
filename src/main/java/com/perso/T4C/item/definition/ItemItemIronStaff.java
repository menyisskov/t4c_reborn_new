package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemIronStaff {
  private ItemItemIronStaff() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.iron_staff",
        "${item.iron_staff}",
        BodyPart.WEAPON,
        "PupWoodenStaff",
        null,
        null,
        "64kInvWoodenStaff",
        130L,
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
        40055,
        1,
        118,
        "1d5+4",
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
        List.of(),
        List.of(),
        false);
  }
}
