package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemDrachenshield {
  private ItemItemDrachenshield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.drachenshield",
        "${item.drachenshield}",
        BodyPart.SHIELD,
        "PupSkavenShield1",
        null,
        null,
        "64kInvSkavenShield1",
        0L,
        2L,
        12.21d,
        25L,
        125L,
        0L,
        40L,
        30L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        41664,
        2,
        465,
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
        List.of(new ItemDefinition.ItemBoost(955, 8, "50", 0, 0)),
        List.of(),
        false);
  }
}
