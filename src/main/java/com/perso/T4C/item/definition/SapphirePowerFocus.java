package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class SapphirePowerFocus {
  private SapphirePowerFocus() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.sapphire_power_focus",
        "${item.sapphire_power_focus}",
        BodyPart.WEAPON2,
        null,
        null,
        null,
        "InvSaphireFocus",
        0L,
        2L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        65L,
        28L,
        0.0d,
        false,
        false,
        false,
        41299,
        2,
        572,
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
            new ItemDefinition.ItemBoost(660, 18, "25", 0, 0),
            new ItemDefinition.ItemBoost(661, 17, "-15", 0, 0),
            new ItemDefinition.ItemBoost(662, 12, "-10", 0, 0),
            new ItemDefinition.ItemBoost(663, 15, "-10", 0, 0),
            new ItemDefinition.ItemBoost(664, 13, "-10", 0, 0),
            new ItemDefinition.ItemBoost(665, 14, "-10", 0, 0)),
        List.of(),
        false);
  }
}
