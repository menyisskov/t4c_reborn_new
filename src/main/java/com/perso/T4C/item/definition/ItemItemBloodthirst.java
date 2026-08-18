package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemBloodthirst {
  private ItemItemBloodthirst() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.bloodthirst",
        "${item.bloodthirst}",
        BodyPart.WEAPON,
        "PupBattleDagger",
        null,
        null,
        "64kInvBattleDagger",
        4843L,
        3L,
        0.0d,
        0L,
        0L,
        0L,
        82L,
        0L,
        20L,
        0L,
        1.0d,
        false,
        false,
        false,
        40197,
        1,
        274,
        "1d25+18",
        "600+1d300",
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
        List.of(new ItemDefinition.ItemBoost(121, 3, "5", 0, 0)),
        List.of(),
        false);
  }
}
