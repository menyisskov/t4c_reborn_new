package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemCedarReflexBow1 {
  private ItemItemCedarReflexBow1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cedar_reflex_bow_1",
        "${item.cedar_reflex_bow_1}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow",
        0L,
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
        41236,
        9,
        421,
        "1d58+130+3*arrow_dmg/2",
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
        List.of(new ItemDefinition.ItemBoost(710, 10035, "true_skill(35)*15/100", 0, 0)),
        List.of(),
        false);
  }
}
