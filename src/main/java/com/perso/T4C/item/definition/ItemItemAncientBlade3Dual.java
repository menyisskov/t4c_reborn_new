package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAncientBlade3Dual {
  private ItemItemAncientBlade3Dual() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ancient_blade_3_dual",
        "${item.ancient_blade_3_dual}",
        BodyPart.WEAPON2,
        "V2_2Dague06",
        null,
        null,
        "Inv_V2_Viperine",
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
        3176,
        1,
        742,
        "1d87+165",
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
