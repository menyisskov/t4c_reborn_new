package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemElmLongbow2 {
  private ItemItemElmLongbow2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.elm_longbow_2",
        "${item.elm_longbow_2}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow",
        9323L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        18L,
        68L,
        0L,
        0L,
        1150.0d,
        false,
        true,
        false,
        41189,
        9,
        421,
        "1d16+28+5*arrow_dmg/4",
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
        List.of(new ItemDefinition.ItemBoost(742, 10035, "true_skill(35)*30/100", 0, 0)),
        List.of(),
        false);
  }
}
