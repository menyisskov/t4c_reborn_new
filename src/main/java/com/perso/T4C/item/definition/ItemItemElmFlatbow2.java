package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemElmFlatbow2 {
  private ItemItemElmFlatbow2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.elm_flatbow_2",
        "${item.elm_flatbow_2}",
        BodyPart.WEAPON,
        "PupShortBow",
        null,
        null,
        "64kInvShortBow",
        5266L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        15L,
        53L,
        0L,
        0L,
        1000.0d,
        false,
        true,
        false,
        41186,
        9,
        86,
        "1d11+18+arrow_dmg",
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
        List.of(new ItemDefinition.ItemBoost(741, 10035, "true_skill(35)*30/100", 0, 0)),
        List.of(),
        false);
  }
}
