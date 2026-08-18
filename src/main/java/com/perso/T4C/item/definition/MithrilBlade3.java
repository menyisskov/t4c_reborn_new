package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MithrilBlade3 {
  private MithrilBlade3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_blade_3",
        "${item.mithril_blade_3}",
        BodyPart.WEAPON,
        "V2_Dague04",
        null,
        null,
        "Inv_V2_dague04",
        0L,
        5L,
        0.0d,
        0L,
        0L,
        0L,
        184L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40499,
        1,
        690,
        "1d54+99",
        "if(750-self.agi/250*750/2<600?600:750-self.agi/250*750/2)+1d375",
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
        List.of(new ItemDefinition.ItemBoost(361, 8, "self.true_attack*50/100", 0, 0)),
        List.of(),
        false);
  }
}
