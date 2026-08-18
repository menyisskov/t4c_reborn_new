package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemOakCompositeBow3 {
  private ItemItemOakCompositeBow3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.oak_composite_bow_3",
        "${item.oak_composite_bow_3}",
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
        41231,
        9,
        448,
        "1d76+177+3*arrow_dmg",
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
        List.of(new ItemDefinition.ItemBoost(766, 10035, "true_skill(35)*50/100", 0, 0)),
        List.of(),
        false);
  }
}
