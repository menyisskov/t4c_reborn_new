package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemLeatherHelmet {
  private ItemItemLeatherHelmet() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.leather_helmet",
        "${item.leather_helmet}",
        BodyPart.HEAD,
        "PupLeatherHelm",
        null,
        null,
        "64kInvLeatherArmorHelm",
        87L,
        3L,
        0.39d,
        1L,
        25L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40019,
        2,
        11,
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
