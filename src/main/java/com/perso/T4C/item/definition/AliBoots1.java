package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AliBoots1 {
  private AliBoots1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ali_boots_1",
        "${item.ali_boots_1}",
        BodyPart.FEET,
        "PupPlateFoot",
        null,
        null,
        "64kInvPlateArmorFeet",
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
        3967,
        2,
        265,
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
