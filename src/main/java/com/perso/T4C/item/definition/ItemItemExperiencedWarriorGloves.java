package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemExperiencedWarriorGloves {
  private ItemItemExperiencedWarriorGloves() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.experienced_warrior_gloves",
        "${item.experienced_warrior_gloves}",
        BodyPart.LEFT_HAND,
        "V2_ManArmorRGlove01__pal6",
        BodyPart.RIGHT_HAND,
        "V2_ManArmorLGlove01__pal6",
        "Inv_ManArmor01Glove__pal6",
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
        3272,
        2,
        1127,
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
