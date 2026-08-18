package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemSilverSpoonOfTheProphet {
  private ItemItemSilverSpoonOfTheProphet() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.silver_spoon_of_the_prophet",
        "${item.silver_spoon_of_the_prophet}",
        BodyPart.WEAPON2,
        null,
        null,
        null,
        "64kInvKitchen 3",
        0L,
        1L,
        1.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        41613,
        2,
        111,
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
            new ItemDefinition.ItemBoost(901, 4, "10", 0, 0),
            new ItemDefinition.ItemBoost(902, 23, "10", 0, 0),
            new ItemDefinition.ItemBoost(903, 11, "100", 0, 0)),
        List.of(),
        false);
  }
}
