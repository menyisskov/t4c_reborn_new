package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemOakCompoundBow1 {
  private ItemItemOakCompoundBow1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.oak_compound_bow_1",
        "${item.oak_compound_bow_1}",
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
        64L,
        271L,
        0L,
        0L,
        850.0d,
        false,
        true,
        false,
        41226,
        9,
        448,
        "1d51+118+5*arrow_dmg/2",
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
        List.of(new ItemDefinition.ItemBoost(725, 10035, "true_skill(35)*15/100", 0, 0)),
        List.of(),
        false);
  }
}
