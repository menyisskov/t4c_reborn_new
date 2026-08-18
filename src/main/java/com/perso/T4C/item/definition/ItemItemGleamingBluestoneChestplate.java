package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemGleamingBluestoneChestplate {
  private ItemItemGleamingBluestoneChestplate() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gleaming_bluestone_chestplate",
        "${item.gleaming_bluestone_chestplate}",
        BodyPart.BODY,
        "PupPlateBody",
        null,
        null,
        "64kInvPlateArmorSleeves",
        0L,
        5L,
        12.25d,
        19L,
        125L,
        0L,
        150L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        41555,
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
        List.of(),
        List.of(),
        false);
  }
}
