package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class OakLongbow1 {
  private OakLongbow1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.oak_longbow_1",
        "${item.oak_longbow_1}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow",
        86582L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        36L,
        227L,
        0L,
        0L,
        1150.0d,
        false,
        true,
        false,
        41217,
        9,
        421,
        "1d43+98+5*arrow_dmg/4",
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
        List.of(new ItemDefinition.ItemBoost(727, 10035, "true_skill(35)*15/100", 0, 0)),
        List.of(),
        false);
  }
}
