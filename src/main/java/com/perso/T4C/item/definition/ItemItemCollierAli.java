package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemCollierAli {
  private ItemItemCollierAli() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.collier_ali",
        "${item.collier_ali}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 1",
        100000L,
        1L,
        15.0d,
        0L,
        70L,
        0L,
        350L,
        350L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        3936,
        2,
        172,
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
