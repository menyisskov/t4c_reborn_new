package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemTestRedQuiver {
  private ItemItemTestRedQuiver() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.test_red_quiver",
        "${item.test_red_quiver}",
        BodyPart.WEAPON2,
        null,
        null,
        null,
        "64kIconQuiver",
        0L,
        0L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        true,
        true,
        41366,
        8,
        454,
        "1d2",
        "0",
        0,
        0,
        false,
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
