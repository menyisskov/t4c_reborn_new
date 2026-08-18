package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAncientBlade1Dual {
  private ItemItemAncientBlade1Dual() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ancient_blade_1_dual",
        "${item.ancient_blade_1_dual}",
        BodyPart.WEAPON2,
        "V2_2Dague01",
        null,
        null,
        "Inv_V2_dague01",
        0L,
        5L,
        0.0d,
        0L,
        0L,
        0L,
        300L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        3174,
        1,
        737,
        "1d75+143",
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
        List.of(),
        List.of(),
        false);
  }
}
