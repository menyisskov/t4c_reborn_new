package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemLichCrown {
  private ItemItemLichCrown() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.lich_crown",
        "${item.lich_crown}",
        BodyPart.HEAD,
        "PupGoldenCrown",
        null,
        null,
        "64kInvGoldenCrown",
        1000000L,
        2L,
        25.0d,
        0L,
        30L,
        0L,
        30L,
        30L,
        30L,
        30L,
        1.0d,
        false,
        false,
        true,
        3420,
        2,
        279,
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
