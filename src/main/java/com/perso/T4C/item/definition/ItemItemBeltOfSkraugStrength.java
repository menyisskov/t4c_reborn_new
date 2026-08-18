package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemBeltOfSkraugStrength {
  private ItemItemBeltOfSkraugStrength() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.belt_of_skraug_strength",
        "${item.belt_of_skraug_strength}",
        BodyPart.BELT,
        null,
        null,
        null,
        "64kInvBelt",
        0L,
        2L,
        2.0d,
        10L,
        100L,
        0L,
        0L,
        0L,
        20L,
        25L,
        0.0d,
        false,
        false,
        false,
        41379,
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
        List.of(new ItemDefinition.ItemBoost(977, 3, "10", 0, 0)),
        List.of(),
        false);
  }
}
