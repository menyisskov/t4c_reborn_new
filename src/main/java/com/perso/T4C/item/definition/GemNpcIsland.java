package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class GemNpcIsland {
  private GemNpcIsland() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gem_npc_island",
        "${item.gem_npc_island}",
        BodyPart.BODY,
        null,
        null,
        null,
        "64kInvRedGem",
        0L,
        0L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        3307,
        6,
        476,
        null,
        null,
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(),
        List.of(),
        List.of(),
        false);
  }
}
