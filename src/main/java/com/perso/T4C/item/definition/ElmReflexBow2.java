package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ElmReflexBow2 {
  private ElmReflexBow2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.elm_reflex_bow_2",
        "${item.elm_reflex_bow_2}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow",
        14529L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        22L,
        82L,
        0L,
        0L,
        1000.0d,
        false,
        true,
        false,
        41192,
        9,
        421,
        "1d18+34+3*arrow_dmg/2",
        "1000",
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
        List.of(new ItemDefinition.ItemBoost(744, 10035, "true_skill(35)*30/100", 0, 0)),
        List.of(),
        false);
  }
}
