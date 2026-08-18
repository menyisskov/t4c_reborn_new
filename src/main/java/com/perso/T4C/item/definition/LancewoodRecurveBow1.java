package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class LancewoodRecurveBow1 {
  private LancewoodRecurveBow1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.lancewood_recurve_bow_1",
        "${item.lancewood_recurve_bow_1}",
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
        41252,
        9,
        421,
        "1d70+145+2*arrow_dmg",
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
        List.of(new ItemDefinition.ItemBoost(722, 10035, "true_skill(35)*15/100", 0, 0)),
        List.of(),
        false);
  }
}
