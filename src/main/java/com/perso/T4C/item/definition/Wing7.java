package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Wing7 {
  private Wing7() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.wing7",
        "${item.wing7}",
        BodyPart.BACK,
        "NM2_DechuWings",
        null,
        null,
        "Inv_NM2DechuWings",
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
        3641,
        2,
        918,
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
