package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ShamanArmor {
  private ShamanArmor() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.shaman_armor",
        "${item.shaman_armor}",
        BodyPart.BODY,
        "PupWhiteRobe__pal11",
        null,
        null,
        "64kInvWhiteRobe__pal11",
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
        3919,
        2,
        595,
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
