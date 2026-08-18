package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AmuletOfIntelligence {
  private AmuletOfIntelligence() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.amulet_of_intelligence",
        "${item.amulet_of_intelligence}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 2",
        547L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        28L,
        15L,
        0.0d,
        false,
        false,
        false,
        40049,
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
        List.of(new ItemDefinition.ItemBoost(20, 1, "5", 0, 0)),
        List.of(),
        false);
  }
}
