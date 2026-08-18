package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class HickoryRecurveBow {
  private HickoryRecurveBow() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.hickory_recurve_bow",
        "${item.hickory_recurve_bow}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow",
        23310L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        40L,
        169L,
        0L,
        0L,
        900.0d,
        false,
        true,
        false,
        41206,
        9,
        421,
        "1d27+59+2*arrow_dmg",
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
        List.of(),
        List.of(),
        false);
  }
}
