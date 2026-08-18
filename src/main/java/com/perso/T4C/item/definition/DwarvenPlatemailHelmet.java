package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class DwarvenPlatemailHelmet {
  private DwarvenPlatemailHelmet() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dwarven_platemail_helmet",
        "${item.dwarven_platemail_helmet}",
        BodyPart.HEAD,
        "PupPlateHelm",
        null,
        null,
        "64kInvPlateArmorHelm",
        10893L,
        8L,
        5.46d,
        10L,
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
        40326,
        2,
        267,
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
            new ItemDefinition.ItemBoost(221, 12, "4", 0, 0),
            new ItemDefinition.ItemBoost(222, 22, "4", 0, 0),
            new ItemDefinition.ItemBoost(223, 15, "4", 0, 0),
            new ItemDefinition.ItemBoost(224, 13, "4", 0, 0),
            new ItemDefinition.ItemBoost(225, 14, "4", 0, 0)),
        List.of(),
        false);
  }
}
