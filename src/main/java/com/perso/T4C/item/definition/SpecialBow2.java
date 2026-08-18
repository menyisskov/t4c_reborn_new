package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class SpecialBow2 {
  private SpecialBow2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.special_bow_2",
        "${item.special_bow_2}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow2",
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
        800.0d,
        false,
        true,
        false,
        41579,
        9,
        448,
        "1d1+1*arrow_dmg",
        "800",
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
