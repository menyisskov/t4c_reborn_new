package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class PolishedDirk1 {
  private PolishedDirk1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.polished_dirk_1",
        "${item.polished_dirk_1}",
        BodyPart.WEAPON,
        "PupBattleDagger",
        null,
        null,
        "64kInvBattleDagger",
        3510L,
        3L,
        0.0d,
        0L,
        0L,
        0L,
        53L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40459,
        1,
        274,
        "1d11+15",
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
        List.of(new ItemDefinition.ItemBoost(346, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
