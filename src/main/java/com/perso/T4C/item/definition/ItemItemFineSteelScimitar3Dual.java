package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemFineSteelScimitar3Dual {
  private ItemItemFineSteelScimitar3Dual() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.fine_steel_scimitar_3_dual",
        "${item.fine_steel_scimitar_3_dual}",
        BodyPart.WEAPON2,
        "V2_2Sword05",
        null,
        null,
        "Inv_V2_Sword05",
        77094L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        155L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        3161,
        1,
        718,
        "1d48+86",
        "if(862-self.agi/250*862/2<600?600:862-self.agi/250*862/2)+1d431",
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
