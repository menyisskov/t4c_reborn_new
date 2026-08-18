package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAliArmor300 {
  private ItemItemAliArmor300() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ali_armor_300",
        "${item.ali_armor_300}",
        BodyPart.BODY,
        "PupBodyClothSet1",
        null,
        null,
        "64kInvClothSet1Body",
        500000L,
        2L,
        50.0d,
        0L,
        270L,
        0L,
        350L,
        350L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        4146,
        2,
        285,
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
