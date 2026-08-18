package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemSpiderShapedRing {
  private ItemItemSpiderShapedRing() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.spider_shaped_ring",
        "${item.spider_shaped_ring}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 2",
        50701L,
        1L,
        5.0d,
        0L,
        10L,
        0L,
        0L,
        0L,
        113L,
        30L,
        0.0d,
        false,
        false,
        false,
        41361,
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
            new ItemDefinition.ItemBoost(673, 1, "25", 0, 0),
            new ItemDefinition.ItemBoost(674, 24, "10", 0, 0)),
        List.of(),
        false);
  }
}
