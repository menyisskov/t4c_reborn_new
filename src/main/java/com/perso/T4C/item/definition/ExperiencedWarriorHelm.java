package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ExperiencedWarriorHelm {
  private ExperiencedWarriorHelm() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.experienced_warrior_helm",
        "${item.experienced_warrior_helm}",
        BodyPart.HEAD,
        "V2_Haume04__pal6",
        null,
        null,
        "Inv_V2_Haume04__pal6",
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
        3271,
        2,
        1141,
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
