package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MithrilBroadAxe3 {
  private MithrilBroadAxe3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_broad_axe_3",
        "${item.mithril_broad_axe_3}",
        BodyPart.WEAPON,
        "PupBattleAxe",
        null,
        null,
        "64kInvDouble Axe",
        0L,
        16L,
        0.0d,
        0L,
        0L,
        0L,
        416L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40651,
        1,
        122,
        "1d205+350",
        "if(1500-self.agi/250*1500/2<1500/2?1500/2:1500-self.agi/250*1500/2)+1d750",
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
        List.of(new ItemDefinition.ItemBoost(385, 8, "self.true_attack*50/100", 0, 0)),
        List.of(),
        false);
  }
}
