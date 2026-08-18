package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ElmReflexBow {
  private ElmReflexBow() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.elm_reflex_bow",
        "${item.elm_reflex_bow}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow",
        4843L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        22L,
        82L,
        0L,
        0L,
        1000.0d,
        false,
        true,
        false,
        41190,
        9,
        421,
        "1d14+26+3*arrow_dmg/2",
        "1000",
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
