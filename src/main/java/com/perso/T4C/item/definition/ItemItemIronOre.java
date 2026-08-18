package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemIronOre {
  private ItemItemIronOre() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.iron_ore",
        "${item.iron_ore}",
        BodyPart.BODY,
        null,
        null,
        null,
        "InvDebritFer",
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
        1.0d,
        false,
        false,
        true,
        3463,
        10,
        1187,
        null,
        null,
        0,
        0,
        true,
        "brigand key",
        0,
        null,
        0,
        600,
        600,
        List.of(),
        List.of(),
        List.of(),
        false);
  }
}
