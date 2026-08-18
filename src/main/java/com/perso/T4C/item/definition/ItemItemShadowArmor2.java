package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemShadowArmor2 {
  private ItemItemShadowArmor2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.shadow_armor_2",
        "${item.shadow_armor_2}",
        BodyPart.BODY,
        "PupWhiteRobe__pal10",
        null,
        null,
        "64kInvWhiteRobe__pal10",
        500000L,
        2L,
        45.0d,
        0L,
        300L,
        0L,
        0L,
        0L,
        400L,
        75L,
        1.0d,
        false,
        false,
        true,
        4089,
        2,
        594,
        null,
        null,
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
