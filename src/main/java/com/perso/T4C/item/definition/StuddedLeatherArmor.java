package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class StuddedLeatherArmor {
  private StuddedLeatherArmor() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.studded_leather_armor",
        "${item.studded_leather_armor}",
        BodyPart.BODY,
        "PupStuddedBodyArmor",
        null,
        null,
        "64kInvStuddedLeatherBody",
        1513L,
        9L,
        2.8d,
        10L,
        45L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40029,
        2,
        283,
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
