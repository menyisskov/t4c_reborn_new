package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RustedDirk2 {
  private RustedDirk2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.rusted_dirk_2",
        "${item.rusted_dirk_2}",
        BodyPart.WEAPON,
        "PupBattleDagger",
        null,
        null,
        "64kInvBattleDagger",
        29L,
        3L,
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
        40422,
        1,
        274,
        "1d4+1",
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
        List.of(new ItemDefinition.ItemBoost(342, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
