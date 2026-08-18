package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RubyPowerFocus {
  private RubyPowerFocus() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ruby_power_focus",
        "${item.ruby_power_focus}",
        BodyPart.WEAPON2,
        null,
        null,
        null,
        "InvRubisFocus",
        0L,
        2L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        78L,
        15L,
        0.0d,
        false,
        false,
        false,
        41298,
        2,
        568,
        null,
        "0",
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
        List.of(
            new ItemDefinition.ItemBoost(654, 17, "25", 0, 0),
            new ItemDefinition.ItemBoost(655, 18, "-15", 0, 0),
            new ItemDefinition.ItemBoost(656, 12, "-10", 0, 0),
            new ItemDefinition.ItemBoost(657, 15, "-10", 0, 0),
            new ItemDefinition.ItemBoost(658, 13, "-10", 0, 0),
            new ItemDefinition.ItemBoost(659, 14, "-10", 0, 0)),
        List.of(),
        false);
  }
}
