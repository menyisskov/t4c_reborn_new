package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRustedDagger2 {
  private ItemItemRustedDagger2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.rusted_dagger_2",
        "${item.rusted_dagger_2}",
        BodyPart.WEAPON,
        "PupBattleDagger",
        null,
        null,
        "64kInvBattleDagger",
        605L,
        4L,
        0.0d,
        0L,
        0L,
        0L,
        24L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40438,
        1,
        274,
        "1d7+6",
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
        List.of(new ItemDefinition.ItemBoost(344, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
