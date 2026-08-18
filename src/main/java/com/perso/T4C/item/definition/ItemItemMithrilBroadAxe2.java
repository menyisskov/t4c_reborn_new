package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemMithrilBroadAxe2 {
  private ItemItemMithrilBroadAxe2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_broad_axe_2",
        "${item.mithril_broad_axe_2}",
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
        40650,
        1,
        122,
        "1d178+303",
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
        List.of(new ItemDefinition.ItemBoost(384, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
