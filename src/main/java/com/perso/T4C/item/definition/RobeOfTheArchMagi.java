package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RobeOfTheArchMagi {
  private RobeOfTheArchMagi() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.robe_of_the_arch_magi",
        "${item.robe_of_the_arch_magi}",
        BodyPart.BODY,
        "PupRedRobe",
        null,
        null,
        "64kInvRedRobe",
        0L,
        5L,
        17.0d,
        25L,
        27L,
        0L,
        0L,
        0L,
        190L,
        15L,
        0.0d,
        false,
        false,
        false,
        41153,
        2,
        423,
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
        List.of(
            new ItemDefinition.ItemBoost(622, 1, "50", 0, 0),
            new ItemDefinition.ItemBoost(623, 17, "20", 0, 0)),
        List.of(),
        false);
  }
}
