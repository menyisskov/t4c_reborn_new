package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class DwarvenPlatemailBoots {
  private DwarvenPlatemailBoots() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dwarven_platemail_boots",
        "${item.dwarven_platemail_boots}",
        BodyPart.FEET,
        "PupPlateFoot",
        null,
        null,
        "64kInvPlateArmorFeet",
        9830L,
        8L,
        5.67d,
        11L,
        160L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40525,
        2,
        265,
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
            new ItemDefinition.ItemBoost(211, 12, "4", 0, 0),
            new ItemDefinition.ItemBoost(212, 22, "4", 0, 0),
            new ItemDefinition.ItemBoost(213, 15, "4", 0, 0),
            new ItemDefinition.ItemBoost(214, 13, "4", 0, 0),
            new ItemDefinition.ItemBoost(215, 14, "4", 0, 0)),
        List.of(),
        false);
  }
}
