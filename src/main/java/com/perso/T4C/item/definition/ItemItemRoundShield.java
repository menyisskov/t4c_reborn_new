package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRoundShield {
  private ItemItemRoundShield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.round_shield",
        "${item.round_shield}",
        BodyPart.SHIELD,
        "PupOrcShield",
        null,
        null,
        "64kInvOrcShield",
        2463L,
        8L,
        2.97d,
        10L,
        60L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40177,
        2,
        282,
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
        List.of(),
        List.of(),
        false);
  }
}
