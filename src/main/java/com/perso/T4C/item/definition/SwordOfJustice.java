package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class SwordOfJustice {
  private SwordOfJustice() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.sword_of_justice",
        "${item.sword_of_justice}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvNormalSword",
        3638L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        71L,
        0L,
        20L,
        23L,
        1.0d,
        false,
        false,
        false,
        40067,
        1,
        2,
        "1d14+23",
        "if(825-self.agi/250*825/2<600?600:825-self.agi/250*825/2)+1d413",
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
        List.of(new ItemDefinition.ItemBoost(66, 3, "5", 0, 0)),
        List.of(),
        false);
  }
}
