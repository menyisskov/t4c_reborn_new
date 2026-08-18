package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemEclipseShield {
  private ItemItemEclipseShield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.eclipse_shield",
        "${item.eclipse_shield}",
        BodyPart.SHIELD,
        "PupSkeletonShield__pal4",
        null,
        null,
        "64kInvSkeletonShield__pal4",
        40000L,
        2L,
        35.0d,
        0L,
        350L,
        0L,
        100L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        3322,
        2,
        1052,
        null,
        null,
        0,
        -1,
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
