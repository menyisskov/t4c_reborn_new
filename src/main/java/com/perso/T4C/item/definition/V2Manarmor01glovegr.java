package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class V2Manarmor01glovegr {
  private V2Manarmor01glovegr() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.v2_manarmor01glovegr",
        "${item.v2_manarmor01glovegr}",
        BodyPart.LEFT_HAND,
        "V2_ManArmorRGlove01",
        BodyPart.RIGHT_HAND,
        "V2_ManArmorLGlove01",
        "Inv_ManArmor01Glove",
        467L,
        4L,
        0.81d,
        3L,
        45L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        3202,
        2,
        883,
        null,
        "0",
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
