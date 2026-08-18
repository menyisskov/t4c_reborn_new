package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AncientScimitar1 {
  private AncientScimitar1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ancient_scimitar_1",
        "${item.ancient_scimitar_1}",
        BodyPart.WEAPON,
        "PupSkeletonSword",
        null,
        null,
        "64kInvSkeletonSword",
        0L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        459L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40401,
        1,
        471,
        "1d99+161",
        "if(862-self.agi/250*862/2<600?600:862-self.agi/250*862/2)+1d431",
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
        List.of(new ItemDefinition.ItemBoost(334, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
