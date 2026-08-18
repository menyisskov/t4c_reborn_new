package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class LancewoodCompoundBow2 {
  private LancewoodCompoundBow2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.lancewood_compound_bow_2",
        "${item.lancewood_compound_bow_2}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow2",
        0L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        92L,
        430L,
        0L,
        0L,
        850.0d,
        false,
        true,
        false,
        41256,
        9,
        448,
        "1d84+175+5*arrow_dmg/2",
        "850",
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
        List.of(new ItemDefinition.ItemBoost(751, 10035, "true_skill(35)*30/100", 0, 0)),
        List.of(),
        false);
  }
}
