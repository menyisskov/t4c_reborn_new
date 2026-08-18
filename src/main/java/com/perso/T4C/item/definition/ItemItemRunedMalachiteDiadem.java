package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRunedMalachiteDiadem {
  private ItemItemRunedMalachiteDiadem() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.runed_malachite_diadem",
        "${item.runed_malachite_diadem}",
        BodyPart.HEAD,
        "PupGoldenCrown",
        null,
        null,
        "64kInvGoldenCrown",
        0L,
        3L,
        10.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        98L,
        108L,
        0.0d,
        false,
        false,
        false,
        41296,
        2,
        279,
        null,
        "0",
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
        List.of(
            new ItemDefinition.ItemBoost(641, 12, "10", 0, 0),
            new ItemDefinition.ItemBoost(642, 22, "10", 0, 0),
            new ItemDefinition.ItemBoost(643, 15, "10", 0, 0),
            new ItemDefinition.ItemBoost(644, 13, "10", 0, 0),
            new ItemDefinition.ItemBoost(645, 14, "10", 0, 0)),
        List.of(),
        false);
  }
}
