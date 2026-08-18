package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class V2Ham {
  private V2Ham() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.v2_ham",
        "${item.v2_ham}",
        BodyPart.WEAPON,
        "Ham",
        null,
        null,
        "Inv_Hamm",
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
        false,
        3017,
        1,
        671,
        "0",
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
