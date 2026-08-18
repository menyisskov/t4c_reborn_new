package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class HickoryLongbow1 {
  private HickoryLongbow1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.hickory_longbow_1",
        "${item.hickory_longbow_1}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow",
        31240L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        27L,
        140L,
        0L,
        0L,
        1150.0d,
        false,
        true,
        false,
        41201,
        9,
        421,
        "1d27+58+5*arrow_dmg/4",
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
        List.of(new ItemDefinition.ItemBoost(717, 10035, "true_skill(35)*15/100", 0, 0)),
        List.of(),
        false);
  }
}
