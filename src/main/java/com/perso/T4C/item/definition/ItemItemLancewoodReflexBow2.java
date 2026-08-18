package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemLancewoodReflexBow2 {
  private ItemItemLancewoodReflexBow2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.lancewood_reflex_bow_2",
        "${item.lancewood_reflex_bow_2}",
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
        62L,
        401L,
        0L,
        0L,
        1000.0d,
        false,
        true,
        false,
        41250,
        9,
        421,
        "1d76+162+3*arrow_dmg/2",
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
        List.of(new ItemDefinition.ItemBoost(753, 10035, "true_skill(35)*30/100", 0, 0)),
        List.of(),
        false);
  }
}
