package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ExperiencedWarriorLegs {
  private ExperiencedWarriorLegs() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.experienced_warrior_legs",
        "${item.experienced_warrior_legs}",
        BodyPart.LEGS,
        "V2_ManArmorLegs01__pal6",
        null,
        null,
        "Inv_ManArmor01Legs__pal6",
        2000L,
        0L,
        8.0d,
        0L,
        80L,
        0L,
        150L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        3268,
        2,
        1120,
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
