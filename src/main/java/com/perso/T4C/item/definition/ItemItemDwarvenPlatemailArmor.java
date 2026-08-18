package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemDwarvenPlatemailArmor {
  private ItemItemDwarvenPlatemailArmor() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dwarven_platemail_armor",
        "${item.dwarven_platemail_armor}",
        BodyPart.BODY,
        "PupPlateBody",
        null,
        null,
        "64kInvPlateArmorSleeves",
        34207L,
        15L,
        19.0d,
        36L,
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
        40328,
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
            new ItemDefinition.ItemBoost(206, 12, "4", 0, 0),
            new ItemDefinition.ItemBoost(207, 22, "4", 0, 0),
            new ItemDefinition.ItemBoost(208, 15, "4", 0, 0),
            new ItemDefinition.ItemBoost(209, 13, "4", 0, 0),
            new ItemDefinition.ItemBoost(210, 14, "4", 0, 0)),
        List.of(),
        false);
  }
}
