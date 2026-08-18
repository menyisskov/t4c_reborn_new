package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ArmorOfCowardice {
  private ArmorOfCowardice() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.armor_of_cowardice",
        "${item.armor_of_cowardice}",
        BodyPart.BODY,
        "PupLeatherBody",
        null,
        null,
        "64kInvLeatherArmorBody",
        1579L,
        8L,
        2.8d,
        0L,
        46L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40136,
        2,
        8,
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
