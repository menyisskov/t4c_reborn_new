package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemGoblinLeatherArmor {
  private ItemItemGoblinLeatherArmor() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.goblin_leather_armor",
        "${item.goblin_leather_armor}",
        BodyPart.BODY,
        "PupLeatherBody",
        null,
        null,
        "64kInvLeatherArmorBody",
        364L,
        8L,
        2.45d,
        4L,
        25L,
        0L,
        0L,
        0L,
        17L,
        18L,
        0.0d,
        false,
        false,
        false,
        40119,
        2,
        577,
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
