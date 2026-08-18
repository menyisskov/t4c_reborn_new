package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemBraceletBrutal {
  private ItemItemBraceletBrutal() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.bracelet_brutal",
        "${item.bracelet_brutal}",
        BodyPart.BRACER,
        null,
        null,
        null,
        "64kInvBracelet",
        50000L,
        1L,
        15.0d,
        0L,
        350L,
        0L,
        100L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        3372,
        2,
        237,
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
