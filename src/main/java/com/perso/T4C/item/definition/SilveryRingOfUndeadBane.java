package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class SilveryRingOfUndeadBane {
  private SilveryRingOfUndeadBane() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.silvery_ring_of_undead_bane",
        "${item.silvery_ring_of_undead_bane}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 3",
        0L,
        1L,
        1.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40766,
        2,
        178,
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
        List.of(new ItemDefinition.ItemSpell(10397, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
