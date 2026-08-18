package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemLancewoodRecurveBow2 {
  private ItemItemLancewoodRecurveBow2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.lancewood_recurve_bow_2",
        "${item.lancewood_recurve_bow_2}",
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
        77L,
        416L,
        0L,
        0L,
        900.0d,
        false,
        true,
        false,
        41253,
        9,
        421,
        "1d79+164+2*arrow_dmg",
        "900",
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
        List.of(new ItemDefinition.ItemBoost(752, 10035, "true_skill(35)*30/100", 0, 0)),
        List.of(),
        false);
  }
}
