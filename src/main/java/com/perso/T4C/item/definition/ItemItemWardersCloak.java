package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemWardersCloak {
  private ItemItemWardersCloak() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.warders_cloak",
        "${item.warders_cloak}",
        BodyPart.BACK,
        "PupRedCape",
        null,
        null,
        "64kInvRedCape",
        0L,
        1L,
        20.0d,
        0L,
        50L,
        250L,
        250L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        41711,
        2,
        287,
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
            new ItemDefinition.ItemBoost(989, 3, "50", 0, 0),
            new ItemDefinition.ItemBoost(990, 6, "50", 0, 0),
            new ItemDefinition.ItemBoost(991, 2, "50", 0, 0),
            new ItemDefinition.ItemBoost(992, 1, "-50", 0, 0),
            new ItemDefinition.ItemBoost(993, 4, "-50", 0, 0)),
        List.of(),
        false);
  }
}
