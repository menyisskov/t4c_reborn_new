package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class SacrificeArmor300 {
  private SacrificeArmor300() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.sacrifice_armor_300",
        "${item.sacrifice_armor_300}",
        BodyPart.BODY,
        "ManLichRobeKimono",
        null,
        null,
        "Inv_LichRobeKimono",
        500000L,
        2L,
        95.0d,
        0L,
        350L,
        0L,
        0L,
        310L,
        310L,
        0L,
        1.0d,
        false,
        false,
        true,
        4153,
        2,
        928,
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
