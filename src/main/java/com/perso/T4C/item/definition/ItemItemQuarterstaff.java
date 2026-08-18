package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemQuarterstaff {
  private ItemItemQuarterstaff() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.quarterstaff",
        "${item.quarterstaff}",
        BodyPart.WEAPON,
        "PupWoodenStaff",
        null,
        null,
        "64kInvWoodenStaff",
        389L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        13L,
        0L,
        30L,
        0L,
        1.0d,
        false,
        false,
        false,
        40660,
        1,
        118,
        "1d6+7",
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
        List.of(),
        List.of(),
        false);
  }
}
