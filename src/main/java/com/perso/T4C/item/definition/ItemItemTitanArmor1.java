package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemTitanArmor1 {
  private ItemItemTitanArmor1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.titan_armor_1",
        "${item.titan_armor_1}",
        BodyPart.BODY,
        "PupWhiteRobe__pal9",
        null,
        null,
        "64kInvWhiteRobe__pal9",
        500000L,
        2L,
        207.0d,
        0L,
        500L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        4024,
        2,
        593,
        null,
        null,
        0,
        -1,
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
