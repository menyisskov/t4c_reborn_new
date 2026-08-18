package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class FineSteelMace {
  private FineSteelMace() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.fine_steel_mace",
        "${item.fine_steel_mace}",
        BodyPart.WEAPON,
        "PupMace",
        null,
        null,
        "Inv_V2_Sp05",
        12351L,
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
        40480,
        1,
        119,
        "if(target.r_dark=5025?1d29+50:1d25+44)",
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
