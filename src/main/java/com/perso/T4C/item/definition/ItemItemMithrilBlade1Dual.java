package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemMithrilBlade1Dual {
  private ItemItemMithrilBlade1Dual() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_blade_1_dual",
        "${item.mithril_blade_1_dual}",
        BodyPart.WEAPON2,
        "V2_2Dague04",
        null,
        null,
        "Inv_V2_dague04",
        55460L,
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
        3166,
        1,
        740,
        "1d41+76",
        "if(750-self.agi/250*750/2<600?600:750-self.agi/250*750/2)+1d375",
        0,
        0,
        false,
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
