package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class FineSteelShortSword {
  private FineSteelShortSword() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.fine_steel_short_sword",
        "${item.fine_steel_short_sword}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvShortSword",
        9464L,
        6L,
        0.0d,
        0L,
        0L,
        0L,
        111L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40416,
        1,
        1,
        "1d20+35",
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
