package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ArmorOfSelfishness {
  private ArmorOfSelfishness() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.armor_of_selfishness",
        "${item.armor_of_selfishness}",
        BodyPart.BODY,
        "PupLeatherBody",
        null,
        null,
        "64kInvLeatherArmorBody",
        4418L,
        8L,
        5.0d,
        5L,
        67L,
        0L,
        0L,
        0L,
        30L,
        0L,
        0.0d,
        false,
        false,
        false,
        40141,
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
