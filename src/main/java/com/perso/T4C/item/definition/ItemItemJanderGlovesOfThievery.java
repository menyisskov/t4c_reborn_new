package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemJanderGlovesOfThievery {
  private ItemItemJanderGlovesOfThievery() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.jander_gloves_of_thievery",
        "${item.jander_gloves_of_thievery}",
        BodyPart.LEFT_HAND,
        "PupLeatherGloveL",
        BodyPart.RIGHT_HAND,
        "PupLeatherGloveR",
        "64kInvLeatherArmorGloves",
        4343L,
        3L,
        2.97d,
        0L,
        110L,
        0L,
        0L,
        30L,
        33L,
        25L,
        0.0d,
        false,
        false,
        false,
        41452,
        2,
        259,
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
            new ItemDefinition.ItemBoost(694, 10015, "10", 0, 0),
            new ItemDefinition.ItemBoost(695, 6, "15", 0, 0)),
        List.of(),
        false);
  }
}
