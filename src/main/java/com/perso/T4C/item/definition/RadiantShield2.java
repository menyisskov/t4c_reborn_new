package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RadiantShield2 {
  private RadiantShield2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.radiant_shield_2",
        "${item.radiant_shield_2}",
        BodyPart.SHIELD,
        "PupSkeletonShield__pal7",
        null,
        null,
        "64kInvSkeletonShield__pal7",
        500000L,
        2L,
        35.0d,
        0L,
        300L,
        0L,
        0L,
        0L,
        75L,
        400L,
        1.0d,
        false,
        false,
        true,
        4015,
        2,
        1055,
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
