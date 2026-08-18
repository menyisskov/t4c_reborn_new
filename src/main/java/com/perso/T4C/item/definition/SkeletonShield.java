package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class SkeletonShield {
  private SkeletonShield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.skeleton_shield",
        "${item.skeleton_shield}",
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
        41637,
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
        List.of(),
        List.of(),
        false);
  }
}
