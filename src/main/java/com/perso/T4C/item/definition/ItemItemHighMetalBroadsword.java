package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemHighMetalBroadsword {
  private ItemItemHighMetalBroadsword() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.high_metal_broadsword",
        "${item.high_metal_broadsword}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvNormalSword",
        37721L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        213L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40336,
        1,
        2,
        "1d41+77",
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
        List.of(),
        List.of(),
        false);
  }
}
