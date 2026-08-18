package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemArmoredRobe {
  private ItemItemArmoredRobe() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.armored_robe",
        "${item.armored_robe}",
        BodyPart.BODY,
        "PupArmoredRobe",
        null,
        null,
        "Inv_ArmoredRobe",
        0L,
        6L,
        57.25d,
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
        false,
        3008,
        2,
        674,
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
