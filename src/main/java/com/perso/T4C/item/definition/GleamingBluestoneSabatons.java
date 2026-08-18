package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class GleamingBluestoneSabatons {
  private GleamingBluestoneSabatons() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gleaming_bluestone_sabatons",
        "${item.gleaming_bluestone_sabatons}",
        BodyPart.FEET,
        "PupPlateFoot",
        null,
        null,
        "64kInvPlateArmorFeet",
        0L,
        3L,
        3.645d,
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
        41559,
        2,
        265,
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
