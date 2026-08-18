package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class HighMetalMace2 {
  private HighMetalMace2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.high_metal_mace_2",
        "${item.high_metal_mace_2}",
        BodyPart.WEAPON,
        "PupMace",
        null,
        null,
        "64kInvMace",
        57821L,
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
        40689,
        1,
        119,
        "if(target.r_dark=5025?1d46+82:1d40+71)",
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
        List.of(new ItemDefinition.ItemBoost(427, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
