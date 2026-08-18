package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemLancewoodCompositeBow2 {
  private ItemItemLancewoodCompositeBow2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.lancewood_composite_bow_2",
        "${item.lancewood_composite_bow_2}",
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
        109L,
        445L,
        0L,
        0L,
        750.0d,
        false,
        true,
        false,
        41259,
        9,
        448,
        "1d93+194+3*arrow_dmg",
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
        List.of(new ItemDefinition.ItemBoost(750, 10035, "true_skill(35)*30/100", 0, 0)),
        List.of(),
        false);
  }
}
