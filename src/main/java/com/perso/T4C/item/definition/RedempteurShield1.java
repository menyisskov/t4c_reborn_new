package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RedempteurShield1 {
  private RedempteurShield1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.redempteur_shield_1",
        "${item.redempteur_shield_1}",
        BodyPart.SHIELD,
        "PupSkeletonShield__pal3",
        null,
        null,
        "64kInvSkeletonShield__pal3",
        500000L,
        2L,
        45.0d,
        0L,
        300L,
        0L,
        200L,
        0L,
        75L,
        250L,
        1.0d,
        false,
        false,
        true,
        3877,
        2,
        1051,
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
