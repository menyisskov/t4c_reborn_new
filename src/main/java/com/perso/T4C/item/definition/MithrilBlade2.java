package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MithrilBlade2 {
  private MithrilBlade2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_blade_2",
        "${item.mithril_blade_2}",
        BodyPart.WEAPON,
        "V2_Dague04",
        null,
        null,
        "Inv_V2_dague04",
        83191L,
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
        40486,
        1,
        690,
        "1d46+86",
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
        List.of(new ItemDefinition.ItemBoost(360, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
