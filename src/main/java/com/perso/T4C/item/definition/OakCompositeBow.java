package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class OakCompositeBow {
  private OakCompositeBow() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.oak_composite_bow",
        "${item.oak_composite_bow}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow2",
        69408L,
        11L,
        0.0d,
        0L,
        0L,
        0L,
        77L,
        285L,
        0L,
        0L,
        750.0d,
        false,
        true,
        false,
        41228,
        9,
        448,
        "1d51+117+3*arrow_dmg",
        "750",
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
