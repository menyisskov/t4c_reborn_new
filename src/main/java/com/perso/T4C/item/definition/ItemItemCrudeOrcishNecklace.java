package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemCrudeOrcishNecklace {
  private ItemItemCrudeOrcishNecklace() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.crude_orcish_necklace",
        "${item.crude_orcish_necklace}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 3",
        1981L,
        2L,
        2.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        29L,
        36L,
        0.0d,
        false,
        false,
        false,
        40719,
        2,
        174,
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
            new ItemDefinition.ItemBoost(442, 8, "25", 0, 0),
            new ItemDefinition.ItemBoost(877, 10035, "25", 0, 0)),
        List.of(),
        false);
  }
}
