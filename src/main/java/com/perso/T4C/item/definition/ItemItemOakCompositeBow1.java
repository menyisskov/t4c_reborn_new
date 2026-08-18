package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemOakCompositeBow1 {
  private ItemItemOakCompositeBow1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.oak_composite_bow_1",
        "${item.oak_composite_bow_1}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow2",
        0L,
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
        41229,
        9,
        448,
        "1d59+135+3*arrow_dmg",
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
        List.of(new ItemDefinition.ItemBoost(724, 10035, "true_skill(35)*15/100", 0, 0)),
        List.of(),
        false);
  }
}
