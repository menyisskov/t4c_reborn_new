package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAncientPlatemailHelmet {
  private ItemItemAncientPlatemailHelmet() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ancient_platemail_helmet",
        "${item.ancient_platemail_helmet}",
        BodyPart.HEAD,
        "PupPlateHelm",
        null,
        null,
        "64kInvPlateArmorHelm",
        41926L,
        3L,
        16.51d,
        0L,
        300L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40440,
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
            new ItemDefinition.ItemBoost(271, 12, "10", 0, 0),
            new ItemDefinition.ItemBoost(272, 22, "10", 0, 0),
            new ItemDefinition.ItemBoost(273, 15, "10", 0, 0),
            new ItemDefinition.ItemBoost(274, 13, "10", 0, 0),
            new ItemDefinition.ItemBoost(275, 14, "10", 0, 0)),
        List.of(),
        false);
  }
}
