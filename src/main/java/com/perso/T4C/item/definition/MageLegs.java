package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MageLegs {
  private MageLegs() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mage_legs",
        "${item.mage_legs}",
        BodyPart.LEGS,
        "V2_ManArmorLegs01__pal9",
        null,
        null,
        "Inv_ManArmor01Legs__pal9",
        2000L,
        0L,
        20.0d,
        0L,
        50L,
        0L,
        0L,
        0L,
        300L,
        75L,
        1.0d,
        false,
        false,
        true,
        3355,
        2,
        1123,
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
