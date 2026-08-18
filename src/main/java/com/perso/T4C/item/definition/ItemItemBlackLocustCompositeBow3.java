package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemBlackLocustCompositeBow3 {
  private ItemItemBlackLocustCompositeBow3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.black_locust_composite_bow_3",
        "${item.black_locust_composite_bow_3}",
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
        138L,
        590L,
        0L,
        0L,
        750.0d,
        false,
        true,
        false,
        41270,
        9,
        448,
        "1d136+261+3*arrow_dmg",
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
        List.of(new ItemDefinition.ItemBoost(761, 10035, "true_skill(35)*50/100", 0, 0)),
        List.of(),
        false);
  }
}
