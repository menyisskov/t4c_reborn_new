package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemShamanHelmet {
  private ItemItemShamanHelmet() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.shaman_helmet",
        "${item.shaman_helmet}",
        BodyPart.HEAD,
        "PupShamanHelm",
        null,
        null,
        "64kInvShamanHelm",
        0L,
        0L,
        12.0d,
        10L,
        70L,
        0L,
        0L,
        185L,
        185L,
        0L,
        1.0d,
        false,
        false,
        true,
        3918,
        2,
        462,
        null,
        null,
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
