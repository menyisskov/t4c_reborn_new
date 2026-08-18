package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class BonePart {
  private BonePart() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.bone_part",
        "${item.bone_part}",
        null,
        null,
        null,
        null,
        "64kInvSkeletonBone",
        70000L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        3419,
        6,
        181,
        null,
        null,
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
