package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ShadowShield1 {
  private ShadowShield1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.shadow_shield_1",
        "${item.shadow_shield_1}",
        BodyPart.SHIELD,
        "PupSkeletonShield__pal4",
        null,
        null,
        "64kInvSkeletonShield__pal4",
        500000L,
        2L,
        35.0d,
        0L,
        300L,
        0L,
        0L,
        0L,
        400L,
        75L,
        1.0d,
        false,
        false,
        true,
        3992,
        2,
        1052,
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
