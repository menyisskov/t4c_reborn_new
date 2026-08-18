package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class GleamingBluestoneHeadgear {
  private GleamingBluestoneHeadgear() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gleaming_bluestone_headgear",
        "${item.gleaming_bluestone_headgear}",
        BodyPart.HEAD,
        "PupPlateHelm",
        null,
        null,
        "64kInvPlateArmorHelm",
        0L,
        3L,
        3.51d,
        5L,
        125L,
        0L,
        150L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        41560,
        2,
        267,
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
