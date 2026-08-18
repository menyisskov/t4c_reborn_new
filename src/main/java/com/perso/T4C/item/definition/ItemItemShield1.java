package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemShield1 {
  private ItemItemShield1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.shield1",
        "${item.shield1}",
        BodyPart.SHIELD,
        "V2_Shield01",
        null,
        null,
        "Inv_V2_Shield01",
        0L,
        0L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        3364,
        1,
        871,
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
