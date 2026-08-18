package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemPendantOfLuck {
  private ItemItemPendantOfLuck() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.pendant_of_luck",
        "${item.pendant_of_luck}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 3",
        2691L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        25L,
        25L,
        0.0d,
        false,
        false,
        false,
        40047,
        2,
        174,
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
        List.of(new ItemDefinition.ItemBoost(17, 7, "10", 0, 0)),
        List.of(),
        false);
  }
}
