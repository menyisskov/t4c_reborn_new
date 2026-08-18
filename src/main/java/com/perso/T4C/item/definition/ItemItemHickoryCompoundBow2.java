package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemHickoryCompoundBow2 {
  private ItemItemHickoryCompoundBow2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.hickory_compound_bow_2",
        "${item.hickory_compound_bow_2}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow2",
        83191L,
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
        41211,
        9,
        448,
        "1d40+88+5*arrow_dmg/2",
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
        List.of(new ItemDefinition.ItemBoost(745, 10035, "true_skill(35)*30/100", 0, 0)),
        List.of(),
        false);
  }
}
