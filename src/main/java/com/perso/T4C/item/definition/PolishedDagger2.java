package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class PolishedDagger2 {
  private PolishedDagger2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.polished_dagger_2",
        "${item.polished_dagger_2}",
        BodyPart.WEAPON,
        "PupBattleDagger",
        null,
        null,
        "64kInvBattleDagger",
        9323L,
        4L,
        0.0d,
        0L,
        0L,
        0L,
        68L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40446,
        1,
        274,
        "1d16+26",
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
        List.of(new ItemDefinition.ItemBoost(349, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
