package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemLeatherBeltOfSurvival {
  private ItemItemLeatherBeltOfSurvival() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.leather_belt_of_survival",
        "${item.leather_belt_of_survival}",
        BodyPart.BELT,
        null,
        null,
        null,
        "64kInvBelt",
        140L,
        2L,
        1.0d,
        1L,
        25L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40727,
        2,
        235,
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
        List.of(new ItemDefinition.ItemBoost(454, 10004, "10", 0, 0)),
        List.of(),
        false);
  }
}
