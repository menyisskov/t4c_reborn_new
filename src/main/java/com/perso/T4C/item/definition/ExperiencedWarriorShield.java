package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ExperiencedWarriorShield {
  private ExperiencedWarriorShield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.experienced_warrior_shield",
        "${item.experienced_warrior_shield}",
        BodyPart.SHIELD,
        "SkShield",
        null,
        null,
        "Inv_SkShield",
        2000L,
        0L,
        8.0d,
        0L,
        80L,
        0L,
        150L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        3274,
        2,
        677,
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
