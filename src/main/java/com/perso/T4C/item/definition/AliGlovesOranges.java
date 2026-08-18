package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AliGlovesOranges {
  private AliGlovesOranges() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ali_gloves_oranges",
        "${item.ali_gloves_oranges}",
        BodyPart.LEFT_HAND,
        "V2_ManArmorRGlove01__pal8",
        BodyPart.RIGHT_HAND,
        "V2_ManArmorLGlove01__pal8",
        "Inv_ManArmor01Glove__pal8",
        500000L,
        2L,
        8.0d,
        0L,
        70L,
        0L,
        350L,
        350L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        3889,
        2,
        1129,
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
