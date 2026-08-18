package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CedarRecurveBow2 {
  private CedarRecurveBow2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cedar_recurve_bow_2",
        "${item.cedar_recurve_bow_2}",
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
        66L,
        343L,
        0L,
        0L,
        900.0d,
        false,
        true,
        false,
        41240,
        9,
        421,
        "1d68+150+2*arrow_dmg",
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
        List.of(new ItemDefinition.ItemBoost(739, 10035, "true_skill(35)*30/100", 0, 0)),
        List.of(),
        false);
  }
}
