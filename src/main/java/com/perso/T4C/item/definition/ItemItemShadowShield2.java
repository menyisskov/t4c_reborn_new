package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemShadowShield2 {
  private ItemItemShadowShield2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.shadow_shield_2",
        "${item.shadow_shield_2}",
        BodyPart.SHIELD,
        "PupCentaurShield",
        null,
        null,
        "64kInvCentaurShield1",
        500000L,
        2L,
        35.0d,
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
        4091,
        2,
        460,
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
