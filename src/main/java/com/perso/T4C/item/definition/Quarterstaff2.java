package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Quarterstaff2 {
  private Quarterstaff2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.quarterstaff_2",
        "${item.quarterstaff_2}",
        BodyPart.WEAPON,
        "PupWoodenStaff",
        null,
        null,
        "64kInvWoodenStaff",
        1169L,
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
        40662,
        1,
        118,
        "1d8+9",
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
        List.of(new ItemDefinition.ItemBoost(404, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
