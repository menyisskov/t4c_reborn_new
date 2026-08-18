package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemHighMetalMace {
  private ItemItemHighMetalMace() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.high_metal_mace",
        "${item.high_metal_mace}",
        BodyPart.WEAPON,
        "PupMace",
        null,
        null,
        "64kInvMace",
        19273L,
        9L,
        0.0d,
        0L,
        0L,
        0L,
        110L,
        0L,
        0L,
        94L,
        1.0d,
        false,
        false,
        false,
        40515,
        1,
        119,
        "if(target.r_dark=5025?1d35+63:1d30+55)",
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
