package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemVoyeurCloak {
  private ItemItemVoyeurCloak() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.voyeur_cloak",
        "${item.voyeur_cloak}",
        BodyPart.BODY,
        "PupNecromanRobe",
        null,
        null,
        "64kInvNecromanRobe",
        449L,
        5L,
        2.0d,
        25L,
        12L,
        0L,
        0L,
        0L,
        35L,
        20L,
        0.0d,
        false,
        false,
        false,
        41620,
        1,
        278,
        null,
        "0",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10646, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
