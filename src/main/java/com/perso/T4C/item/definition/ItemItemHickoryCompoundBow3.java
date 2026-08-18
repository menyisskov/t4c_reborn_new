package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemHickoryCompoundBow3 {
  private ItemItemHickoryCompoundBow3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.hickory_compound_bow_3",
        "${item.hickory_compound_bow_3}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow2",
        93191L,
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
        41212,
        9,
        448,
        "1d46+102+5*arrow_dmg/2",
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
        List.of(new ItemDefinition.ItemBoost(764, 10035, "true_skill(35)*50/100", 0, 0)),
        List.of(),
        false);
  }
}
