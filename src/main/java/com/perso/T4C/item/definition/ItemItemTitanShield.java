package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemTitanShield {
  private ItemItemTitanShield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.titan_shield",
        "${item.titan_shield}",
        BodyPart.SHIELD,
        "PupSkeletonShield__pal8",
        null,
        null,
        "64kInvSkeletonShield__pal8",
        500000L,
        2L,
        120.0d,
        0L,
        500L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        3431,
        2,
        1056,
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
