package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemDrachenrobe {
  private ItemItemDrachenrobe() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.drachenrobe",
        "${item.drachenrobe}",
        BodyPart.BODY,
        "PupNecromanRobe",
        null,
        null,
        "64kInvNecromanRobe",
        0L,
        5L,
        15.0d,
        0L,
        25L,
        0L,
        0L,
        0L,
        98L,
        73L,
        0.0d,
        false,
        false,
        false,
        41662,
        2,
        278,
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
            new ItemDefinition.ItemBoost(943, 16, "10", 0, 0),
            new ItemDefinition.ItemBoost(944, 24, "10", 0, 0),
            new ItemDefinition.ItemBoost(945, 19, "10", 0, 0),
            new ItemDefinition.ItemBoost(946, 17, "10", 0, 0),
            new ItemDefinition.ItemBoost(947, 18, "10", 0, 0),
            new ItemDefinition.ItemBoost(948, 23, "10", 0, 0)),
        List.of(),
        false);
  }
}
