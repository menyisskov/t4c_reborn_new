package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemTrueshotQuiver {
  private ItemItemTrueshotQuiver() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.trueshot_quiver",
        "${item.trueshot_quiver}",
        BodyPart.WEAPON2,
        null,
        null,
        null,
        "64kIconQuiver",
        1L,
        0L,
        0.0d,
        0L,
        0L,
        0L,
        20L,
        110L,
        0L,
        0L,
        0.0d,
        false,
        true,
        true,
        41856,
        8,
        455,
        "self.level*0.1",
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
            new ItemDefinition.ItemBoost(1016, 6, "10", 0, 0),
            new ItemDefinition.ItemBoost(1017, 10035, "50", 0, 0)),
        List.of(),
        false);
  }
}
