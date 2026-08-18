package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemLightFocus {
  private ItemItemLightFocus() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.light_focus",
        "${item.light_focus}",
        BodyPart.WEAPON2,
        null,
        null,
        null,
        "64kInvMisc 3 - Part 1 2",
        0L,
        2L,
        0.0d,
        0L,
        50L,
        0L,
        0L,
        0L,
        75L,
        180L,
        1.0d,
        false,
        false,
        false,
        3382,
        2,
        46,
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
