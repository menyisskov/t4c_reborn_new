package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CloakOfArmageddon {
  private CloakOfArmageddon() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cloak_of_armageddon",
        "${item.cloak_of_armageddon}",
        BodyPart.BODY,
        "PupNecromanRobe",
        null,
        null,
        "64kInvNecromanRobe",
        0L,
        5L,
        20.0d,
        25L,
        37L,
        0L,
        0L,
        0L,
        175L,
        70L,
        0.0d,
        false,
        false,
        false,
        41385,
        2,
        278,
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
        List.of(new ItemDefinition.ItemSpell(10418, 0, 100)),
        List.of(new ItemDefinition.ItemBoost(830, 1, "50", 0, 0)),
        List.of(),
        false);
  }
}
