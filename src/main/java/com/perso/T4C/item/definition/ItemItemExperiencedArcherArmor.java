package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemExperiencedArcherArmor {
  private ItemItemExperiencedArcherArmor() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.experienced_archer_armor",
        "${item.experienced_archer_armor}",
        BodyPart.BODY,
        "V2_ManArmorBody01__pal8",
        null,
        null,
        "Inv_ManArmor01Body__pal8",
        2000L,
        0L,
        16.0d,
        0L,
        50L,
        0L,
        50L,
        150L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        3282,
        2,
        1115,
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
