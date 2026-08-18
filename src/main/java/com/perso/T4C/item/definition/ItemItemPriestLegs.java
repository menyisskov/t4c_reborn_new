package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemPriestLegs {
  private ItemItemPriestLegs() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.priest_legs",
        "${item.priest_legs}",
        BodyPart.LEGS,
        "V2_ManArmorLegs01",
        null,
        null,
        "Inv_ManArmor01Legs",
        2000L,
        0L,
        20.0d,
        0L,
        50L,
        0L,
        0L,
        0L,
        75L,
        300L,
        1.0d,
        false,
        false,
        true,
        3350,
        2,
        881,
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
