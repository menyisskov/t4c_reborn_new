package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemDrachensword {
  private ItemItemDrachensword() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.drachensword",
        "${item.drachensword}",
        BodyPart.WEAPON,
        "PupRealDarkSword",
        null,
        null,
        "64kInvRealDarkSword",
        0L,
        3L,
        0.0d,
        0L,
        0L,
        0L,
        230L,
        60L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        41663,
        1,
        275,
        "1d35+63",
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
        List.of(new ItemDefinition.ItemBoost(941, 3, "50", 0, 0)),
        List.of(),
        false);
  }
}
