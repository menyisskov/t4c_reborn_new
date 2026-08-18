package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemHighMetalDagger3 {
  private ItemItemHighMetalDagger3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.high_metal_dagger_3",
        "${item.high_metal_dagger_3}",
        BodyPart.WEAPON,
        "PupBattleDagger",
        null,
        null,
        "64kInvBattleDagger",
        49404L,
        4L,
        0.0d,
        0L,
        0L,
        0L,
        126L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40485,
        1,
        274,
        "1d34+60",
        "if(675-self.agi/250*675/2<600?600:675-self.agi/250*675/2)+1d338",
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
        List.of(new ItemDefinition.ItemBoost(356, 8, "self.true_attack*50/100", 0, 0)),
        List.of(),
        false);
  }
}
