package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class VibreventEnOxandre {
  private VibreventEnOxandre() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.vibrevent_en_oxandre",
        "${item.vibrevent_en_oxandre}",
        BodyPart.WEAPON,
        "V2_Bow05",
        null,
        null,
        "Inv_V2_Bow05",
        0L,
        11L,
        0.0d,
        0L,
        0L,
        0L,
        109L,
        445L,
        0L,
        0L,
        1.5384615384615385d,
        false,
        true,
        false,
        3385,
        9,
        747,
        "1d107+224+3*arrow_dmg",
        "650",
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
