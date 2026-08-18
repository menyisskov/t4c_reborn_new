package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ElmRecurveBow3 {
  private ElmRecurveBow3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.elm_recurve_bow_3",
        "${item.elm_recurve_bow_3}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow",
        27849L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        29L,
        97L,
        0L,
        0L,
        900.0d,
        false,
        true,
        false,
        41196,
        9,
        421,
        "1d24+48+2*arrow_dmg",
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
        List.of(new ItemDefinition.ItemBoost(763, 10035, "true_skill(35)*50/100", 0, 0)),
        List.of(),
        false);
  }
}
