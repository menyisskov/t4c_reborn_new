package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemSacrificeLegs {
  private ItemItemSacrificeLegs() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.sacrifice_legs",
        "${item.sacrifice_legs}",
        BodyPart.LEGS,
        "V2_ManArmorLegs01__pal7",
        null,
        null,
        "Inv_ManArmor01Legs__pal7",
        500000L,
        2L,
        35.0d,
        0L,
        150L,
        0L,
        0L,
        310L,
        310L,
        0L,
        1.0d,
        false,
        false,
        true,
        3929,
        2,
        1121,
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
