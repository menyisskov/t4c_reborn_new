package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemJarkoAmulet {
  private ItemItemJarkoAmulet() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.jarko_amulet",
        "${item.jarko_amulet}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 2",
        437L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        28L,
        15L,
        0.0d,
        false,
        false,
        false,
        40017,
        2,
        173,
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
        List.of(new ItemDefinition.ItemBoost(464, 1, "5", 0, 0)),
        List.of(),
        false);
  }
}
