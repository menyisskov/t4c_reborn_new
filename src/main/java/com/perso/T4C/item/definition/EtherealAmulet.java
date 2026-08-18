package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class EtherealAmulet {
  private EtherealAmulet() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ethereal_amulet",
        "${item.ethereal_amulet}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 2",
        17495L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        75L,
        55L,
        0.0d,
        false,
        false,
        false,
        40603,
        2,
        173,
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
        List.of(new ItemDefinition.ItemBoost(472, 9, "100", 0, 0)),
        List.of(),
        false);
  }
}
