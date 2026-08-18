package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemOnyxPowerFocus {
  private ItemItemOnyxPowerFocus() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.onyx_power_focus",
        "${item.onyx_power_focus}",
        BodyPart.WEAPON2,
        null,
        null,
        null,
        "64kInvDarkGem",
        0L,
        2L,
        0.0d,
        0L,
        50L,
        0L,
        0L,
        0L,
        200L,
        75L,
        1.0d,
        false,
        false,
        false,
        3323,
        2,
        474,
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
        List.of(),
        List.of(),
        false);
  }
}
