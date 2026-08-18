package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAncientBlade {
  private ItemItemAncientBlade() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ancient_blade",
        "${item.ancient_blade}",
        BodyPart.WEAPON,
        "V2_Dague01",
        null,
        null,
        "Inv_V2_dague01",
        76897L,
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
        40444,
        1,
        687,
        "1d58+110",
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
