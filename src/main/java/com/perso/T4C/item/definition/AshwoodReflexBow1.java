package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AshwoodReflexBow1 {
  private AshwoodReflexBow1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ashwood_reflex_bow_1",
        "${item.ashwood_reflex_bow_1}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow",
        1574L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        17L,
        39L,
        0L,
        0L,
        1000.0d,
        false,
        true,
        false,
        41181,
        9,
        421,
        "1d9+11+3*arrow_dmg/2",
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
        List.of(new ItemDefinition.ItemBoost(702, 10035, "true_skill(35)*15/100", 0, 0)),
        List.of(),
        false);
  }
}
