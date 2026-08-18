package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AshwoodLongbow1 {
  private AshwoodLongbow1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ashwood_longbow_1",
        "${item.ashwood_longbow_1}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow",
        403L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        13L,
        24L,
        0L,
        0L,
        1150.0d,
        false,
        true,
        false,
        41178,
        9,
        421,
        "1d6+5+5*arrow_dmg/4",
        "1150",
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
        List.of(new ItemDefinition.ItemBoost(701, 10035, "true_skill(35)*15/100", 0, 0)),
        List.of(),
        false);
  }
}
