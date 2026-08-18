package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemMinotaurClanRing {
  private ItemItemMinotaurClanRing() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.minotaur_clan_ring",
        "${item.minotaur_clan_ring}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 2",
        1067L,
        1L,
        1.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        27L,
        33L,
        0.0d,
        false,
        false,
        false,
        40725,
        2,
        177,
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
            new ItemDefinition.ItemBoost(450, 8, "25", 0, 0),
            new ItemDefinition.ItemBoost(882, 10035, "25", 0, 0)),
        List.of(),
        false);
  }
}
