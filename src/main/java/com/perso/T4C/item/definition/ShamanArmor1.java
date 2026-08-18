package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ShamanArmor1 {
  private ShamanArmor1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.shaman_armor_1",
        "${item.shaman_armor_1}",
        BodyPart.BODY,
        "ManLichRobeKimono",
        null,
        null,
        "Inv_LichRobeKimono",
        0L,
        5L,
        20.0d,
        25L,
        70L,
        0L,
        0L,
        185L,
        185L,
        0L,
        1.0d,
        false,
        false,
        false,
        3920,
        2,
        928,
        null,
        "0",
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
