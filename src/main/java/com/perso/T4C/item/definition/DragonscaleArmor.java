package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class DragonscaleArmor {
  private DragonscaleArmor() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dragonscale_armor",
        "${item.dragonscale_armor}",
        BodyPart.BODY,
        "PupChainMailBody",
        null,
        null,
        "64kInvScale",
        89693L,
        10L,
        23.05d,
        14L,
        180L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40572,
        2,
        188,
        null,
        "0",
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
        List.of(
            new ItemDefinition.ItemBoost(231, 12, "5", 0, 0),
            new ItemDefinition.ItemBoost(232, 22, "5", 0, 0),
            new ItemDefinition.ItemBoost(233, 15, "5", 0, 0),
            new ItemDefinition.ItemBoost(234, 13, "10", 0, 0),
            new ItemDefinition.ItemBoost(235, 14, "5", 0, 0)),
        List.of(),
        false);
  }
}
