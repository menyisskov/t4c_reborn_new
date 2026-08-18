package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Bo1 {
  private Bo1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.bo_1",
        "${item.bo_1}",
        BodyPart.WEAPON,
        "PupSimpleStaff",
        null,
        null,
        "64kInvSimpleStaff",
        2644L,
        11L,
        0.0d,
        0L,
        0L,
        0L,
        15L,
        0L,
        47L,
        0L,
        1.0d,
        false,
        false,
        false,
        40667,
        1,
        296,
        "1d10+15",
        "if(1575-self.agi/250*1575/2<1575/2?1575/2:1575-self.agi/250*1575/2)+1d788",
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
        List.of(new ItemDefinition.ItemBoost(407, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
