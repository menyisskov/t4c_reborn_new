package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemGirdleOfInsight {
  private ItemItemGirdleOfInsight() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.girdle_of_insight",
        "${item.girdle_of_insight}",
        BodyPart.BELT,
        null,
        null,
        null,
        "64kInvBelt",
        1L,
        0L,
        5.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        60L,
        60L,
        0.0d,
        false,
        false,
        false,
        41859,
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
        List.of(
            new ItemDefinition.ItemBoost(1020, 1, "25", 0, 0),
            new ItemDefinition.ItemBoost(1021, 4, "25", 0, 0)),
        List.of(),
        false);
  }
}
