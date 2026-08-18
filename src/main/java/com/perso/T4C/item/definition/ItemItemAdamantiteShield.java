package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAdamantiteShield {
  private ItemItemAdamantiteShield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.adamantite_shield",
        "${item.adamantite_shield}",
        BodyPart.SHIELD,
        "PupCentaurShield",
        null,
        null,
        "64kInvCentaurShield1",
        55078L,
        12L,
        26.07d,
        38L,
        230L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40373,
        2,
        460,
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
