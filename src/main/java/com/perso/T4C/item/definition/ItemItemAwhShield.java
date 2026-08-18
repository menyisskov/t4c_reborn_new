package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAwhShield {
  private ItemItemAwhShield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.awh_shield",
        "${item.awh_shield}",
        BodyPart.SHIELD,
        "PupSkeletonShield",
        null,
        null,
        "64kInvSkeletonShield",
        0L,
        2L,
        5.0d,
        0L,
        20L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        41699,
        2,
        470,
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
            new ItemDefinition.ItemBoost(970, 12, "5", 0, 0),
            new ItemDefinition.ItemBoost(971, 22, "5", 0, 0),
            new ItemDefinition.ItemBoost(972, 15, "5", 0, 0),
            new ItemDefinition.ItemBoost(973, 13, "5", 0, 0),
            new ItemDefinition.ItemBoost(974, 14, "5", 0, 0),
            new ItemDefinition.ItemBoost(975, 10008, "15", 0, 0),
            new ItemDefinition.ItemBoost(976, 9, "(self.true_dodge/6)", 0, 0)),
        List.of(),
        false);
  }
}
