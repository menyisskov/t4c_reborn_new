package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemDevShotgun {
  private ItemItemDevShotgun() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dev_shotgun",
        "${item.dev_shotgun}",
        BodyPart.WEAPON,
        "T4CP_12",
        null,
        null,
        "Inv_T4CP_12",
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
        3241,
        1,
        894,
        null,
        null,
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
