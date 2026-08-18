package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class SkeletonSword {
  private SkeletonSword() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.skeleton_sword",
        "${item.skeleton_sword}",
        BodyPart.WEAPON,
        "PupSkeletonSword",
        null,
        null,
        "64kInvSkeletonSword",
        0L,
        0L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        41638,
        1,
        471,
        "1d1",
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
