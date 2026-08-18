package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemFineSteelMace3Dual {
  private ItemItemFineSteelMace3Dual() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.fine_steel_mace_3_dual",
        "${item.fine_steel_mace_3_dual}",
        BodyPart.WEAPON2,
        "V2_2Special04",
        null,
        null,
        "Inv_V2_Sp05",
        49404L,
        9L,
        0.0d,
        0L,
        0L,
        0L,
        90L,
        0L,
        0L,
        79L,
        1.0d,
        false,
        false,
        false,
        3152,
        1,
        766,
        "if(target.r_dark=5025?1d43+76:1d37+66)",
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
