package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AshwoodReflexBow3 {
  private AshwoodReflexBow3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ashwood_reflex_bow_3",
        "${item.ashwood_reflex_bow_3}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow",
        3148L,
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
        41183,
        9,
        421,
        "1d11+15+3*arrow_dmg/2",
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
        List.of(new ItemDefinition.ItemBoost(760, 10035, "true_skill(35)*50/100", 0, 0)),
        List.of(),
        false);
  }
}
