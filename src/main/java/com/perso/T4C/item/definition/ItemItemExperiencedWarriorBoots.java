package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemExperiencedWarriorBoots {
  private ItemItemExperiencedWarriorBoots() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.experienced_warrior_boots",
        "${item.experienced_warrior_boots}",
        BodyPart.FEET,
        "V2_ManArmorBoots01__pal6",
        null,
        null,
        "Inv_ManArmor01Boots__pal6",
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
        3269,
        2,
        1134,
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
