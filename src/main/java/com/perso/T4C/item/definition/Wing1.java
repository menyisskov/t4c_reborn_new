package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Wing1 {
  private Wing1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.wing1",
        "${item.wing1}",
        BodyPart.BACK,
        "ArchWings__pal2",
        null,
        null,
        "Inv_ArchWingsW",
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
        1.0d,
        false,
        false,
        true,
        3635,
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
