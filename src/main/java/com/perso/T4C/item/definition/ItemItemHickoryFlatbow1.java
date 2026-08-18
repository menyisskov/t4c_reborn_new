package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemHickoryFlatbow1 {
  private ItemItemHickoryFlatbow1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.hickory_flatbow_1",
        "${item.hickory_flatbow_1}",
        BodyPart.WEAPON,
        "PupShortBow",
        null,
        null,
        "64kInvShortBow",
        24702L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        22L,
        126L,
        0L,
        0L,
        1000.0d,
        false,
        true,
        false,
        41198,
        9,
        86,
        "1d21+45+arrow_dmg",
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
        List.of(new ItemDefinition.ItemBoost(716, 10035, "true_skill(35)*15/100", 0, 0)),
        List.of(),
        false);
  }
}
