package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemHardenedLeatherArmor {
  private ItemItemHardenedLeatherArmor() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.hardened_leather_armor",
        "${item.hardened_leather_armor}",
        BodyPart.BODY,
        "PupLeatherBody",
        null,
        null,
        "64kInvLeatherArmorBody",
        202L,
        8L,
        1.45d,
        4L,
        25L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40024,
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
