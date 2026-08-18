package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemElvenLeatherArmor {
  private ItemItemElvenLeatherArmor() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.elven_leather_armor",
        "${item.elven_leather_armor}",
        BodyPart.BODY,
        "PupLeatherBody",
        null,
        null,
        "64kInvLeatherArmorBody",
        5909L,
        8L,
        5.5d,
        6L,
        75L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40482,
        2,
        8,
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
