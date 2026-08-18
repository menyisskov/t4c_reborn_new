package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemGmRobesOfSorcery {
  private ItemItemGmRobesOfSorcery() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gm_robes_of_sorcery",
        "${item.gm_robes_of_sorcery}",
        BodyPart.BODY,
        "PupNecromanRobe",
        null,
        null,
        "64kInvNecromanRobe",
        500L,
        5L,
        2.0d,
        0L,
        30L,
        0L,
        0L,
        0L,
        40L,
        40L,
        0.0d,
        false,
        false,
        false,
        40208,
        2,
        278,
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
        List.of(new ItemDefinition.ItemBoost(112, 1, "1", 0, 0)),
        List.of(),
        false);
  }
}
