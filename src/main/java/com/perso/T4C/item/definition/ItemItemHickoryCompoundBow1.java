package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemHickoryCompoundBow1 {
  private ItemItemHickoryCompoundBow1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.hickory_compound_bow_1",
        "${item.hickory_compound_bow_1}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow2",
        55460L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        49L,
        184L,
        0L,
        0L,
        850.0d,
        false,
        true,
        false,
        41210,
        9,
        448,
        "1d35+78+5*arrow_dmg/2",
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
        List.of(new ItemDefinition.ItemBoost(715, 10035, "true_skill(35)*15/100", 0, 0)),
        List.of(),
        false);
  }
}
