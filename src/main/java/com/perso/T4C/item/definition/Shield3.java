package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Shield3 {
  private Shield3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.shield3",
        "${item.shield3}",
        BodyPart.WEAPON,
        "SwordAngel",
        null,
        null,
        "Inv_SwordAngel",
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
        3366,
        1,
        666,
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
