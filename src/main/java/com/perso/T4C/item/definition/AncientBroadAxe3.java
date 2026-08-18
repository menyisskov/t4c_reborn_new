package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AncientBroadAxe3 {
  private AncientBroadAxe3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ancient_broad_axe_3",
        "${item.ancient_broad_axe_3}",
        BodyPart.WEAPON,
        "PupSkeletonAxe",
        null,
        null,
        "64kInvSkeletonAxe",
        0L,
        16L,
        0.0d,
        0L,
        0L,
        0L,
        590L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40654,
        1,
        468,
        "1d277+422",
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
        List.of(new ItemDefinition.ItemBoost(382, 8, "self.true_attack*50/100", 0, 0)),
        List.of(),
        false);
  }
}
