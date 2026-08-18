package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemMantleOfTheElements {
  private ItemItemMantleOfTheElements() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mantle_of_the_elements",
        "${item.mantle_of_the_elements}",
        BodyPart.BODY,
        "PupRedRobe",
        null,
        null,
        "64kInvRedRobe",
        28701L,
        5L,
        15.0d,
        25L,
        25L,
        0L,
        0L,
        0L,
        128L,
        53L,
        0.0d,
        false,
        false,
        false,
        41152,
        2,
        423,
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
            new ItemDefinition.ItemBoost(617, 1, "20", 0, 0),
            new ItemDefinition.ItemBoost(618, 17, "20", 0, 0),
            new ItemDefinition.ItemBoost(619, 18, "20", 0, 0),
            new ItemDefinition.ItemBoost(620, 16, "20", 0, 0),
            new ItemDefinition.ItemBoost(621, 19, "20", 0, 0)),
        List.of(),
        false);
  }
}
