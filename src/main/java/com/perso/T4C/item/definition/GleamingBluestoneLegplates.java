package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class GleamingBluestoneLegplates {
  private GleamingBluestoneLegplates() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gleaming_bluestone_legplates",
        "${item.gleaming_bluestone_legplates}",
        BodyPart.LEGS,
        "PupPlateLegs",
        null,
        null,
        "64kInvPlateArmorLegs",
        0L,
        4L,
        4.05d,
        6L,
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
        41556,
        2,
        266,
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
