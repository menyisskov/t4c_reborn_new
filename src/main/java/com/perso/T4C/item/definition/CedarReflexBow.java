package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CedarReflexBow {
  private CedarReflexBow() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cedar_reflex_bow",
        "${item.cedar_reflex_bow}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow",
        93023L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        53L,
        329L,
        0L,
        0L,
        1000.0d,
        false,
        true,
        false,
        41235,
        9,
        421,
        "1d50+113+3*arrow_dmg/2",
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
