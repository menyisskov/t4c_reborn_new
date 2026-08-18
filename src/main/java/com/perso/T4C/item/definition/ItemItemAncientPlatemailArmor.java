package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAncientPlatemailArmor {
  private ItemItemAncientPlatemailArmor() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ancient_platemail_armor",
        "${item.ancient_platemail_armor}",
        BodyPart.BODY,
        "PupPlateBody",
        null,
        null,
        "64kInvPlateArmorSleeves",
        0L,
        8L,
        57.25d,
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
        40412,
        2,
        264,
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
            new ItemDefinition.ItemBoost(256, 12, "10", 0, 0),
            new ItemDefinition.ItemBoost(257, 22, "10", 0, 0),
            new ItemDefinition.ItemBoost(258, 15, "10", 0, 0),
            new ItemDefinition.ItemBoost(259, 13, "10", 0, 0),
            new ItemDefinition.ItemBoost(260, 14, "10", 0, 0)),
        List.of(),
        false);
  }
}
