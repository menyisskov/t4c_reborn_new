package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class FineSteelDagger3Dual {
  private FineSteelDagger3Dual() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.fine_steel_dagger_3_dual",
        "${item.fine_steel_dagger_3_dual}",
        BodyPart.WEAPON2,
        "V2_2Dague01",
        null,
        null,
        "Inv_V2_dague01",
        27849L,
        4L,
        0.0d,
        0L,
        0L,
        0L,
        97L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        3157,
        1,
        737,
        "1d27+45",
        "if(675-self.agi/250*675/2<600?600:675-self.agi/250*675/2)+1d338",
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
