package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CedarCompoundBow {
  private CedarCompoundBow() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cedar_compound_bow",
        "${item.cedar_compound_bow}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow2",
        0L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        80L,
        358L,
        0L,
        0L,
        850.0d,
        false,
        true,
        false,
        41241,
        9,
        448,
        "1d56+123+5*arrow_dmg/2",
        "850",
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
