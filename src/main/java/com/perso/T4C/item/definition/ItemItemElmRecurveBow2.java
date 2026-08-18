package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemElmRecurveBow2 {
  private ItemItemElmRecurveBow2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.elm_recurve_bow_2",
        "${item.elm_recurve_bow_2}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow",
        20887L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        29L,
        97L,
        0L,
        0L,
        900.0d,
        false,
        true,
        false,
        41195,
        9,
        421,
        "1d21+41+2*arrow_dmg",
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
        List.of(new ItemDefinition.ItemBoost(743, 10035, "true_skill(35)*30/100", 0, 0)),
        List.of(),
        false);
  }
}
