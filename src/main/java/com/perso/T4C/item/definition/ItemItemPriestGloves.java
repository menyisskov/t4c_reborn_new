package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemPriestGloves {
  private ItemItemPriestGloves() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.priest_gloves",
        "${item.priest_gloves}",
        BodyPart.LEFT_HAND,
        "V2_ManArmorRGlove01",
        BodyPart.RIGHT_HAND,
        "V2_ManArmorLGlove01",
        "Inv_ManArmor01Glove",
        2000L,
        0L,
        9.0d,
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
        3349,
        2,
        883,
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
