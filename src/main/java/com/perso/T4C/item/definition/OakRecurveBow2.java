package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class OakRecurveBow2 {
  private OakRecurveBow2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.oak_recurve_bow_2",
        "${item.oak_recurve_bow_2}",
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
        256L,
        0L,
        0L,
        900.0d,
        false,
        true,
        false,
        41224,
        9,
        421,
        "1d53+120+2*arrow_dmg",
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
        List.of(new ItemDefinition.ItemBoost(758, 10035, "true_skill(35)*30/100", 0, 0)),
        List.of(),
        false);
  }
}
