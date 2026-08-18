package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemExperiencedArcherGloves {
  private ItemItemExperiencedArcherGloves() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.experienced_archer_gloves",
        "${item.experienced_archer_gloves}",
        BodyPart.LEFT_HAND,
        "V2_ManArmorRGlove01__pal8",
        BodyPart.RIGHT_HAND,
        "V2_ManArmorLGlove01__pal8",
        "Inv_ManArmor01Glove__pal8",
        2000L,
        0L,
        8.0d,
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
        3286,
        2,
        1129,
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
