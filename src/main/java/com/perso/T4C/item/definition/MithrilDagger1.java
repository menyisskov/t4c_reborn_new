package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MithrilDagger1 {
  private MithrilDagger1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_dagger_1",
        "${item.mithril_dagger_1}",
        BodyPart.WEAPON,
        "PupBattleDagger",
        null,
        null,
        "64kInvBattleDagger",
        38547L,
        4L,
        0.0d,
        0L,
        0L,
        0L,
        155L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40489,
        1,
        274,
        "1d32+57",
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
        List.of(new ItemDefinition.ItemBoost(357, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
