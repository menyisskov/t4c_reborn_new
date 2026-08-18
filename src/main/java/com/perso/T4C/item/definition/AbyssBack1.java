package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AbyssBack1 {
  private AbyssBack1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.abyss_back1",
        "${item.abyss_back1}",
        BodyPart.BACK,
        "ArchWings__pal2",
        null,
        null,
        "Inv_ArchWingsW",
        2000000L,
        2L,
        25.0d,
        0L,
        300L,
        0L,
        0L,
        0L,
        400L,
        75L,
        1.0d,
        false,
        false,
        true,
        3653,
        2,
        958,
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
