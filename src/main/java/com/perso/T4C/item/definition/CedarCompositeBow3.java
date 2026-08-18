package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CedarCompositeBow3 {
  private CedarCompositeBow3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cedar_composite_bow_3",
        "${item.cedar_composite_bow_3}",
        BodyPart.WEAPON,
        "V2_Bow04",
        null,
        null,
        "Inv_V2_Bow04",
        0L,
        11L,
        0.0d,
        0L,
        0L,
        0L,
        94L,
        372L,
        0L,
        0L,
        750.0d,
        false,
        true,
        false,
        41247,
        9,
        448,
        "1d94+205+3*arrow_dmg",
        "750",
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
        List.of(new ItemDefinition.ItemBoost(762, 10035, "true_skill(35)*50/100", 0, 0)),
        List.of(),
        false);
  }
}
